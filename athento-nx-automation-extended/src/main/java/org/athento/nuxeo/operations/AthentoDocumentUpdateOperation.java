/**
 * 
 */
package org.athento.nuxeo.operations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.ConflictOperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentUpdateOperation.ID, category = "Athento", label = "Athento Document Update", description = "Updates a document in Athento's way")
public class AthentoDocumentUpdateOperation {

    public static final String ID = "Athento.Document.Update";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentUpdateWatchedDocumentType";
    public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentUpdateOperationId";

    @Context
    protected CoreSession session;

    @Param(name = "changeToken", required = false)
    protected String changeToken = null;

    @Param(name = "documentType", required = false)
    protected String documentType;

    @Param(name = "properties")
    protected Properties properties;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @OperationMethod()
    public DocumentModel run() throws Exception {
        return run(null);
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug(AthentoDocumentUpdateOperation.ID
                    + " BEGIN with params:");
            _log.debug(" documentType: " + documentType);
            _log.debug(" save: " + save);
            _log.debug(" changeToken: " + changeToken);
            _log.debug(" properties: " + properties);
        }
        try {
            Map<String, Object> config = AthentoOperationsHelper
                    .readConfig(session);
            String watchedDocumentType = String
                    .valueOf(config
                            .get(AthentoDocumentUpdateOperation.CONFIG_WATCHED_DOCTYPE));
            String operationId = String.valueOf(config
                    .get(AthentoDocumentUpdateOperation.CONFIG_OPERATION_ID));
            if (doc == null && watchedDocumentType != null
                    && watchedDocumentType.equals(documentType)) {
                Object input = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("documentType", documentType);
                params.put("save", save);
                params.put("changeToken", changeToken);
                params.put("properties", properties);
                Object retValue = AthentoOperationsHelper.runOperation(
                        operationId, input, params, session);
                doc = (DocumentModel) retValue;
            } else {
                if (_log.isDebugEnabled()) {
                    _log.debug(AthentoDocumentUpdateOperation.ID
                            + " Updating document in the standard way");
                }
                if (changeToken != null) {
                    // Check for dirty update
                    String repoToken = doc.getChangeToken();
                    if (!changeToken.equals(repoToken)) {
                        throw new ConflictOperationException(doc);
                    }
                }
                DocumentHelper.setProperties(session, doc, properties);
                if (save) {
                    doc = session.saveDocument(doc);
                }
            }
            if (_log.isDebugEnabled()) {
                _log.debug(AthentoDocumentUpdateOperation.ID
                        + " END return value: " + doc);
            }
            return doc;
        } catch (Exception e) {
            _log.error(
                    "Unable to complete operation: "
                            + AthentoDocumentUpdateOperation.ID + " due to: "
                            + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }

    private static final Log _log = LogFactory
            .getLog(AthentoDocumentUpdateOperation.class);

}