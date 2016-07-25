/**
 * 
 */
package org.athento.nuxeo.operations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentCreateOperation.ID, category = "Athento", label = "Athento Document Create", description = "Creates a document in Athento's way")
public class AthentoDocumentCreateOperation {

    public static final String ID = "Athento.Document.Create";

    public static final String CONFIG_DEFAULT_DESTINATION = "automationExtendedConfig:defaultDestination";
    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentCreateOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentCreateOperationIdPost";
    public static final String CONFIG_OPERATION_ID_WATCHED_DOCUMENT_TYPES = "automationExtendedConfig:documentCreateWatchedDocumentTypes";

    @Context
    protected CoreSession session;

    @Param(name = "destination", required = false)
    protected String destination;

    @Param(name = "name", required = false)
    protected String name;

    @Param(name = "properties", required = false)
    protected Properties properties;

    @Param(name = "type")
    protected String type;

    @OperationMethod()
    public DocumentModel run() throws Exception {
        String parentFolderPath = getDestinationPath();
        return run(new PathRef(parentFolderPath));
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentRef doc) throws Exception {
        return run(session.getDocument(doc));
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug(AthentoDocumentCreateOperation.ID
                + " BEGIN with params:");
            _log.debug(" - parentDoc: " + doc);
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
            if (AthentoOperationsHelper.isWatchedDocumentType(doc, type,
                watchedDocumentTypes)) {
                if (!StringUtils.isNullOrEmpty(operationId)) {
                    Object input = null;
                    parentFolder = (DocumentModel) AthentoOperationsHelper
                        .runOperation(operationId, input, params, session);
                    parentFolder = (DocumentModel) parentFolder;
                } else {
                    _log.warn("No operation to get basePath and no destination set. Using default basePath: "
                        + basePath);
                    parentFolder = session.getDocument(new PathRef(
                        basePath));
                }
            } else {
                _log.info("Document not watched: " + type
                    + ". Watched doctypes are: " + watchedDocumentTypes);
                parentFolder = doc;
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
            doc = session.createDocument(newDoc);
            if (AthentoOperationsHelper.isWatchedDocumentType(doc, type,
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