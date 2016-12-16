/**
 * 
 */
package org.athento.nuxeo.operations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
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
import org.nuxeo.ecm.core.utils.DocumentModelUtils;
import org.nuxeo.ecm.platform.tag.TagService;

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
        if (_log.isDebugEnabled()) {
            _log.debug(AthentoDocumentCreateOperation.ID
                + " BEGIN with params:");
            _log.debug(" - parentDoc: " + parentDoc);
            _log.debug(" - type: " + type);
            _log.debug(" - name: " + name);
            _log.debug(" - properties: " + properties);
        }
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
                _log.info("Document not watched: " + type
                    + ". Watched doctypes are: " + watchedDocumentTypes);
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
            // Check if document must have the blob #AT-1066
            if (blob != null) {
                // Set file:filename property
                newDoc.setPropertyValue("file:filename", blob.getFilename());
                // Add blob to property
                DocumentHelper.addBlob(newDoc.getProperty("file:content"), blob);
            }
            DocumentModel doc = session.createDocument(newDoc);
            if (_log.isDebugEnabled()) {
                _log.debug(AthentoDocumentCreateOperation.ID
                    + " Doc created : " + doc);
                _log.debug(AthentoDocumentCreateOperation.ID + " properties: ");
                Map<String, Object> props = DocumentModelUtils
                    .getProperties(doc);
                for (String k : props.keySet()) {
                    Object v = props.get(k);
                    if (v != null) {
                        _log.debug(" Prop [" + k + "] " + v);
                    } else {
                        _log.debug(" Prop [" + k + "] is NULL");
                    }
                }
            }
            // Add tags
            if (tags != null) {
                addTags(doc);
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
                } else {
                    _log.warn("No operation to execute Post Document Creation for doctype: "
                        + type);
                }
            } else {
                _log.info("Document not watched: " + type
                    + ". Watched doctypes are: " + watchedDocumentTypes);
            }
            // -- END Document.Create
            if (_log.isDebugEnabled()) {
                _log.debug(AthentoDocumentCreateOperation.ID
                    + " END return value: " + doc);
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

    private String getDestinationPath() {
        String val = destination;
        if (StringUtils.isNullOrEmpty(destination)) {
            val = AthentoOperationsHelper.readConfigValue(session,
                AthentoDocumentCreateOperation.CONFIG_DEFAULT_DESTINATION);
        }
        return val;
    }

    private static final Log _log = LogFactory
        .getLog(AthentoDocumentCreateOperation.class);

}