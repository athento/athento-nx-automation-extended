/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.PropertyUtils;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Delete document.
 *
 * @author athento
 */
@Operation(id = AthentoDocumentDeleteOperation.ID, category = "Athento", label = "Athento Document Delete", description = "Delete a document in Athento's way")
public class AthentoDocumentDeleteOperation extends AbstractAthentoOperation {


    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentDeleteOperation.class);


    public static final String ID = "Athento.Document.Delete";

    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentDeleteOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentDeleteOperationIdPost";

    @Context
    protected CoreSession session;

    /**
     * Operation context.
     */
    @Context
    protected OperationContext ctx;

    @Param(name = "permanent", required = false)
    protected boolean permanent = false;

    /**
     * Run.
     *
     * @param doc
     * @return
     * @throws Exception
     */
    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        try {
            Map<String, Object> config = AthentoOperationsHelper
                    .readConfig(session);
            String preOperationId = String.valueOf(config
                    .get(AthentoDocumentDeleteOperation.CONFIG_OPERATION_ID_PRE));
            Map<String, Object> params = new HashMap<String, Object>();
            if (!StringUtils.isNullOrEmpty(preOperationId)) {
                try {
                    Object result = AthentoOperationsHelper
                            .runOperation(preOperationId, doc, params, session);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(AthentoDocumentDeleteOperation.ID
                                + " Pre operation [: " + preOperationId
                                + "] executed with result: " + result);
                    }
                } catch (Exception e) {
                    LOG.error("Unable to execute operation PRE for delete document", e);
                }
            }
            // Delete document
            if (permanent) {
                session.removeDocument(doc.getRef());
            } else {
                // Default transite to deleted
                session.followTransition(doc, "delete");
            }
            String postOperationId = String
                    .valueOf(config
                            .get(AthentoDocumentDeleteOperation.CONFIG_OPERATION_ID_POST));
            if (!StringUtils.isNullOrEmpty(postOperationId)) {
                try {
                    Object input = doc;
                    Object result = AthentoOperationsHelper.runOperation(
                            postOperationId, input, params, session);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(AthentoDocumentDeleteOperation.ID
                                + " Post operation [: " + postOperationId
                                + "] executed with result: " + result);
                    }
                } catch (Exception e) {
                    LOG.error("Unable to execute operation POST for delete document", e);
                }
            }
            return doc;
        } catch (Exception e) {
            LOG.error(
                    "Unable to complete operation: "
                            + AthentoDocumentDeleteOperation.ID + " due to: "
                            + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }
}