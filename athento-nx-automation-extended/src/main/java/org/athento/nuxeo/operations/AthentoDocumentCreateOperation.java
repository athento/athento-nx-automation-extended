/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.FTPException;
import org.athento.utils.FTPUtils;
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

    @Param(name = "xpath", required = false, description = "xpath for document content")
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
                    _log.warn("No operation to get basePath and no destination set. Using default basePath: "
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
            if (_log.isDebugEnabled()) {
                _log.debug(AthentoDocumentCreateOperation.ID
                    + " Creating document in parentPath: " + parentPath);
            }
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
                TemplateBasedDocument renderable = doc.getAdapter(TemplateBasedDocument.class);
                if (renderable == null) {
                    associateTemplate(template, doc);
                    renderable = doc.getAdapter(TemplateBasedDocument.class);
                }
                if (renderable != null) {
                    Blob renderedBlob = renderable.renderWithTemplate(template);
                    doc.setPropertyValue("file:content", (Serializable) renderedBlob);
                    session.saveDocument(doc);
                } else {
                    throw new Exception("Unable to associate template " + template + " to document, please check your template!");
                }
            }

            if (AthentoOperationsHelper.isWatchedDocumentType(null, type,
                watchedDocumentTypes)) {
                String postOperationId = String
                    .valueOf(config
                        .get(AthentoDocumentCreateOperation.CONFIG_OPERATION_ID_POST));
                if (!StringUtils.isNullOrEmpty(postOperationId)) {
                    Object input = doc;
                    Object result = AthentoOperationsHelper.runOperation(
                        postOperationId, input, params, session);
                    if (_log.isInfoEnabled()) {
                        _log.info(AthentoDocumentCreateOperation.ID
                            + " Post operation [: " + postOperationId
                            + "] executed with result: " + result);
                    }
                }
            }
            return doc;
        } catch (Exception e) {
            _log.error(
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
     * Add content to document.
     * @param newDoc
     */
    private void addContent(DocumentModel newDoc) {
        // Check external Content
        if (externalContent != null) {
            if (externalContent.startsWith("sftp:")) {
                // Connect to SFTP Server to get content
                try {
                    File remoteFile = FTPUtils.getFile(externalContent);
                    if (remoteFile != null) {
                        blob = new FileBlob(remoteFile);
                        blob.setFilename(remoteFile.getName());
                        // Add dc:source metadata
                        if (!FTPUtils.hasPassword(externalContent)) {
                            newDoc.setPropertyValue("dc:source", externalContent);
                        }
                    }
                } catch (FTPException e) {
                    _log.error("Unable to set blob from external content SFTP", e);
                }
            }
            // TODO: Include other external content implementations
        }
        // Check if document must have the blob #AT-1066
        if (blob != null) {
            if (xpath != null) {
                // Add blob to property
                DocumentHelper.addBlob(newDoc.getProperty(xpath), blob);
            } else if (newDoc.hasSchema("file")) {
                // Set file:filename property
                newDoc.setPropertyValue("file:filename", blob.getFilename());
                // Add blob to property
                DocumentHelper.addBlob(newDoc.getProperty("file:content"), blob);
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
        TemplateProcessorService tps = Framework.getLocalService(TemplateProcessorService.class);
        List<DocumentModel> templates = tps.getAvailableTemplateDocs(session, doc.getType());
        for (DocumentModel d : templates) {
            TemplateSourceDocument source = d.getAdapter(TemplateSourceDocument.class);
            if (source.getName().equals(template)) {
                try {
                    tps.makeTemplateBasedDocument(doc, d, true);
                } catch (NuxeoException e) {
                    _log.error("Unable to associaate template to document");
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

    private static final Log _log = LogFactory
        .getLog(AthentoDocumentCreateOperation.class);

}