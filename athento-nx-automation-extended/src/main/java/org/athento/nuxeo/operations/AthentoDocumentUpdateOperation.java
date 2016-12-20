/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.ConflictOperationException;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.tag.TagService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author athento
 */
@Operation(id = AthentoDocumentUpdateOperation.ID, category = "Athento", label = "Athento Document Update", description = "Updates a document in Athento's way")
public class AthentoDocumentUpdateOperation extends AbstractAthentoOperation {

    /**
     * Log.
     */
    private static final Log _log = LogFactory
            .getLog(AthentoDocumentUpdateOperation.class);

    /**
     * Operation ID.
     */
    public static final String ID = "Athento.Document.Update";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentUpdateWatchedDocumentTypes";
    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentUpdateOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentUpdateOperationIdPost";

    /* Default PRE operation to find document to update. */
    private static final String FIND_DOC_OPERATION_ID_PRE = "Athento.DocumentFind";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Context
    protected TagService tagService;

    @Param(name = "changeToken", required = false)
    protected String changeToken = null;

    @Param(name = "documentType", required = false)
    protected String documentType;

    @Param(name = "properties")
    protected Properties properties;

    @Param(name = "old_properties", required = false, description = "Properties used to find the document for update")
    protected Properties oldProperties;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @Param(name = "tags", required = false, description = "Tags for the document")
    protected StringList tags;

    @OperationMethod()
    public DocumentModel run() throws Exception {
        return run(null);
    }

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        if (_log.isDebugEnabled()) {
            _log.debug(AthentoDocumentUpdateOperation.ID
                    + " BEGIN with params:");
            _log.debug(" documentType: " + documentType);
            _log.debug(" input: " + doc);
            _log.debug(" save: " + save);
            _log.debug(" changeToken: " + changeToken);
            _log.debug(" properties: " + properties);
        }
        try {
            Map<String, Object> config = AthentoOperationsHelper
                    .readConfig(session);
            String watchedDocumentTypes = String.valueOf(config
                    .get(AthentoDocumentUpdateOperation.CONFIG_WATCHED_DOCTYPE));
            if (AthentoOperationsHelper.isWatchedDocumentType(doc,
                    documentType, watchedDocumentTypes)) {
                Map<String, Object> params = new HashMap<>();
                params.put("documentType", documentType);
                params.put("save", save);
                params.put("changeToken", changeToken);
                params.put("properties", properties);
                params.put("old_properties", oldProperties == null ? new Properties(0) : oldProperties);

                // Execute preOperation
                doc = executePreOperation(doc, params, config);

                // Save document
                if (doc != null && save) {
                    DocumentHelper.setProperties(session, doc, properties);
                    // After intercept pre-operation, saving doc is mandatory.
                    // It shouldn't delegate to pre or post operation.
                    this.session.saveDocument(doc);
                }

                // Execute postOperation
                doc = executePostOperation(doc, params, config);

            } else {
                // Input is necessary here
                if (doc == null) {
                    throw new AthentoException("Document input is mandatory to update");
                }
                if (_log.isDebugEnabled()) {
                    _log.debug("Document not watched: " + documentType
                            + ". Watched doctypes are: " + watchedDocumentTypes);
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
            // Update tags
            if (tags != null) {
                updateTags(doc);
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

    /**
     * Execute pre-operation.
     *
     * @param doc
     * @param params
     * @param config
     * @return documentModel returned from pre-operation
     */
    private DocumentModel executePreOperation(DocumentModel doc, Map<String, Object> params, Map<String, Object> config)
            throws OperationException {
        String operationIdPre = String.valueOf(config
                .get(AthentoDocumentUpdateOperation.CONFIG_OPERATION_ID_PRE));
        Object retValue;
        if (StringUtils.isNullOrEmpty(operationIdPre) || isVoidChain(operationIdPre)) {
            Map<String, Object> findParams = new HashMap<>(params);
            // Replace properties to old_properties to find document
            findParams.put("properties", oldProperties);
            retValue = AthentoOperationsHelper.runOperation(
                    FIND_DOC_OPERATION_ID_PRE, doc, findParams, session);
        } else {
            retValue = AthentoOperationsHelper.runOperation(
                    operationIdPre, doc, params, session);
        }
        // Check found doc
        if (retValue == null) {
            retValue = doc;
        }
        return (DocumentModel) retValue;
    }

    /**
     * Update tags to document. If tag starts with "-", the tag will be removed from document.
     *
     * @param doc is the document
     */
    private void updateTags(DocumentModel doc) {
        if (tagService.isEnabled()) {
            for (String tag : tags) {
                if (tag == null || tag.isEmpty()) {
                    continue;
                }
                tag = tag.trim();
                if (tag.startsWith("-")) {
                    tag = tag.substring(1).trim();
                    tagService.untag(session, doc.getId(), tag, session.getPrincipal().getName());
                } else {
                    tagService.tag(session, doc.getId(), tag, session.getPrincipal().getName());
                }
            }
        }
    }

    /**
     * Execute post-operation.
     *
     * @param doc
     * @param config
     * @return documentModel returned from post-operation
     */
    private DocumentModel executePostOperation(DocumentModel doc, Map<String, Object> params, Map<String, Object> config) throws OperationException {
        String operationIdPost = String.valueOf(config
                .get(AthentoDocumentUpdateOperation.CONFIG_OPERATION_ID_POST));
        if (!StringUtils.isNullOrEmpty(operationIdPost)) {
            if (_log.isDebugEnabled()) {
                _log.debug("Executing post operation " + operationIdPost);
            }
            Object retValue = AthentoOperationsHelper.runOperation(
                    operationIdPost, doc, params, session);
            doc = (DocumentModel) retValue;
        }
        return doc;
    }

}