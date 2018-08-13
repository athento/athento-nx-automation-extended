/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.nuxeo.report.api.ReportException;
import org.athento.nuxeo.report.api.ReportManager;
import org.athento.nuxeo.report.api.model.OutputReport;
import org.athento.nuxeo.report.api.model.Report;
import org.athento.nuxeo.report.api.model.ReportEngine;
import org.athento.nuxeo.report.api.model.ReportHandler;
import org.athento.nuxeo.report.api.xpoint.ReportDescriptor;
import org.athento.utils.FTPException;
import org.athento.utils.FTPUtils;
import org.athento.utils.ReportInfo;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.template.api.TemplateProcessorService;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.api.adapters.TemplateSourceDocument;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentCreateOperation.ID, category = "Athento", label = "Athento Document Create", description = "Creates a document in Athento's way")
public class AthentoDocumentCreateOperation extends AbstractAthentoOperation {

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentCreateOperation.class);


    public static final String ID = "Athento.Document.Create";

    public static final String CONFIG_DEFAULT_DESTINATION = "automationExtendedConfig:defaultDestination";
    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentCreateOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentCreateOperationIdPost";
    public static final String CONFIG_OPERATION_ID_WATCHED_DOCUMENT_TYPES = "automationExtendedConfig:documentCreateWatchedDocumentTypes";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Context
    protected TagService tagService;

    @Param(name = "destination", required = false)
    protected String destination;

    @Param(name = "name", required = false)
    protected String name;

    @Param(name = "properties", required = false)
    protected Properties properties;

    @Param(name = "type")
    protected String type;

    @Param(name = "tags", required = false, description = "Tags for the new document")
    protected StringList tags;

    @Param(name = "template", required = false, description = "Template to generate the document content")
    protected String template;

    @Param(name = "report", required = false, description = "Report to generate the document content using a report string format", values = { "jr:reportalias:pdf" })
    protected String report;

    @Param(name = "xpath", required = false, description = "xpath for document content metadata")
    protected String xpath;

    @Param(name = "externalContent", required = false, description = "External content for document content")
    protected String externalContent;


    /** Blob to save into new document. */
    private Blob blob;

    @OperationMethod()
    public DocumentModel run() throws Exception {
        String parentFolderPath = getDestinationPath();
        return run(new PathRef(parentFolderPath));
    }

    /**
     * Create document with blob.
     *
     * @since #AT-1066
     * @param blob is the document blob
     * @return document created
     * @throws Exception on error
     */
    @OperationMethod()
    public DocumentModel run(Blob blob) throws Exception {
        String parentFolderPath = getDestinationPath();
        // Set blob
        this.blob = blob;
        return run(new PathRef(parentFolderPath));
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentRef doc) throws Exception {
        return run(session.getDocument(doc));
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel parentDoc) throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        try {
            Map<String, Object> config = AthentoOperationsHelper
                .readConfig(session);
            String watchedDocumentTypes = String
                .valueOf(config
                    .get(AthentoDocumentCreateOperation.CONFIG_OPERATION_ID_WATCHED_DOCUMENT_TYPES));
            String operationId = String.valueOf(config
                .get(AthentoDocumentCreateOperation.CONFIG_OPERATION_ID_PRE));
            String basePath = getDestinationPath();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("basePath", basePath);
            params.put("name", name);
            params.put("properties", properties);
            params.put("type", type);
            DocumentModel parentFolder = null;
            if (AthentoOperationsHelper.isWatchedDocumentType(null, type,
                watchedDocumentTypes)) {
                if (!StringUtils.isNullOrEmpty(operationId)) {
                    Object input = null;
                    parentFolder = (DocumentModel) AthentoOperationsHelper
                        .runOperation(operationId, input, params, session);
                } else {
                    LOG.warn("No operation to get basePath and no destination set. Using default basePath: "
                        + basePath);
                    parentFolder = session.getDocument(new PathRef(basePath));
                }
            } else {
                parentFolder = parentDoc;
            }
            if (name == null) {
                name = "Untitled";
            }
            String parentPath = parentFolder.getPathAsString();
            DocumentModel newDoc = session.createDocumentModel(parentPath,
                name, type);
            if (properties != null) {
                DocumentHelper.setProperties(session, newDoc, properties);
            }
            // Add content to doc
            addContent(newDoc);
            // Create document
            DocumentModel doc = session.createDocument(newDoc);
            // Add tags
            if (tags != null) {
                addTags(doc);
            }
            // Check template (overwrite blob always)
            if (template != null) {
                addContentFromTemplate(template, doc);
            }
            // Check report (overwrite blob always too)
            if (report != null) {
                addContentFromReport(report, doc);
            }
            // Saving document
            session.saveDocument(doc);
            if (AthentoOperationsHelper.isWatchedDocumentType(null, type,
                watchedDocumentTypes)) {
                String postOperationId = String
                    .valueOf(config
                        .get(AthentoDocumentCreateOperation.CONFIG_OPERATION_ID_POST));
                if (!StringUtils.isNullOrEmpty(postOperationId)) {
                    Object input = doc;
                    Object result = AthentoOperationsHelper.runOperation(
                        postOperationId, input, params, session);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(AthentoDocumentCreateOperation.ID
                            + " Post operation [: " + postOperationId
                            + "] executed with result: " + result);
                    }
                }
            }
            return doc;
        } catch (Exception e) {
            LOG.error(
                "Unable to complete operation: "
                    + AthentoDocumentCreateOperation.ID + " due to: "
                    + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }

    /**
     * Add content from report.
     *
     * @param report
     * @param doc
     */
    private void addContentFromReport(String report, DocumentModel doc) throws ReportException {
        ReportManager reportManager = Framework.getService(ReportManager.class);
        ReportInfo reportInfo = new ReportInfo(report);
        Report reportContent = reportManager.getReportByAlias(report);
        if (reportContent == null) {
            LOG.warn("Report " + report + " is not found");
        } else {
            // Get report engine
            ReportEngine reportEngine = reportManager.getReportEngineById(reportInfo.getReportEngine());
            // Add doc as parameters
            reportContent.getParameters().put("docIds", doc.getId());
            Properties properties = new Properties();
            if (reportContent.getDescriptor().getHandler() != null) {
                // Load handler for report
                loadHandler(reportContent, properties);
            }
            OutputReport outputReport = reportManager.getOutputReportByReqParam(reportInfo.getReportOutput());
            Map<String, Object> reportParams = new HashMap<>();
            reportParams.put("doc", doc);
            byte [] reportPrinted = reportEngine.print(reportContent, outputReport, reportParams);
            if (reportPrinted != null) {
                ByteArrayBlob blob = new ByteArrayBlob(reportPrinted, reportInfo.getMimetype());
                blob.setFilename(doc.getTitle());
                if (xpath == null || xpath.isEmpty()) {
                    xpath = "file:content";
                }
                // Add blob to property
                DocumentHelper.addBlob(doc.getProperty(xpath), blob);
                session.saveDocument(doc);
            }
        }
    }

    /**
     * Load handler for report.
     *
     * @param report
     * @param params
     */
    private void loadHandler(Report report, Properties params) {
        ReportDescriptor descriptor = report.getDescriptor();
        if (descriptor.isUseSeam()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading handler " + descriptor.getId());
            }
            if (descriptor.getHandler() != null) {
                try {
                    ReportHandler handler = descriptor.getHandler()
                            .newInstance();
                    Map<String, Object> handleParams = new HashMap<>();
                    handleParams.put("documentManager", this.session);
                    handleParams.putAll(params);
                    handler.handle(report, handleParams);
                } catch (InstantiationException | IllegalAccessException
                        | ReportException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Add content from template.
     *
     * @param template
     * @param doc
     * @throws Exception
     */
    private void addContentFromTemplate(String template, DocumentModel doc) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Add content from template " + template);
        }
        TemplateBasedDocument renderable = doc.getAdapter(TemplateBasedDocument.class);
        if (renderable == null) {
            associateTemplate(template, doc);
            renderable = doc.getAdapter(TemplateBasedDocument.class);
        }
        if (renderable != null) {
            Blob renderedBlob = renderable.renderWithTemplate(template);
            if (xpath == null || xpath.isEmpty()) {
                xpath = "file:content";
            }
            doc.setPropertyValue(xpath, (Serializable) renderedBlob);
        } else {
            throw new Exception("Unable to associate template " + template + " to document, please check your template!");
        }
    }

    /**
     * Add content to document.
     * @param newDoc
     */
    private void addContent(DocumentModel newDoc) {
        // Check external Content
        if (externalContent != null) {
            if (externalContent.startsWith("sftp:") || externalContent.startsWith("ftp:")) {
                // Connect to SFTP Server to get content
                try {
                    boolean remove = FTPUtils.checkRemoveRemoteFile(externalContent);
                    String remoteFilePath = FTPUtils.getRemoteFilePath(externalContent);
                    File remoteFile = FTPUtils.getFile(remoteFilePath, remove);
                    if (remoteFile != null) {
                        blob = new FileBlob(remoteFile);
                        blob.setFilename(remoteFile.getName());
                        // Add dc:source metadata
                        if (!FTPUtils.hasPassword(externalContent)) {
                            newDoc.setPropertyValue("dc:source", "Content from " + externalContent);
                        } else {
                            newDoc.setPropertyValue("dc:source", "Content from sftp");
                        }
                    }
                } catch (FTPException e) {
                    LOG.error("Unable to set blob from external content SFTP", e);
                }
            }
            // TODO: Include other external content implementations
        }
        // Check if document must have the blob #AT-1066
        if (blob != null) {
            if (newDoc.hasSchema("file")) {
                // Set file:filename property
                newDoc.setPropertyValue("file:filename", blob.getFilename());
                // Add blob to property
                DocumentHelper.addBlob(newDoc.getProperty("file:content"), blob);
            } else if (xpath != null && !xpath.isEmpty()) {
                // Add blob to property
                DocumentHelper.addBlob(newDoc.getProperty(xpath), blob);
            }
        }
    }

    /**
     * Associate template to document.
     *
     * @param template
     * @param doc
     */
    private void associateTemplate(String template, DocumentModel doc) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Associating template " + template + " for doc " + doc.getTitle());
        }
        TemplateProcessorService tps = Framework.getLocalService(TemplateProcessorService.class);
        List<DocumentModel> templates = tps.getAvailableTemplateDocs(session, doc.getType());
        for (DocumentModel d : templates) {
            TemplateSourceDocument source = d.getAdapter(TemplateSourceDocument.class);
            if (source.getName().equals(template)) {
                try {
                    tps.makeTemplateBasedDocument(doc, d, true);
                } catch (NuxeoException e) {
                    LOG.error("Unable to associate template to document");
                }
            }
        }
    }

    /**
     * Add tags to document.
     *
     * @param doc is the document
     */
    private void addTags(DocumentModel doc) {
        if (tagService.isEnabled()) {
            for (String tag : tags) {
                if (tag == null || tag.isEmpty()) {
                    continue;
                }
                tagService.tag(session, doc.getId(), tag, session.getPrincipal().getName());
            }
        }
    }

    /**
     * Get destination path.
     *
     * @return
     */
    private String getDestinationPath() {
        String val = destination;
        if (StringUtils.isNullOrEmpty(destination)) {
            val = AthentoOperationsHelper.readConfigValue(session,
                AthentoDocumentCreateOperation.CONFIG_DEFAULT_DESTINATION);
        }
        if (!val.startsWith("/")) {
            DocumentModel pathDoc = session.getDocument(new IdRef(val));
            val = pathDoc.getPathAsString();
        }
        return val;
    }

}