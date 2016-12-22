/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Unlink a document.
 *
 * @author athento
 *
 */
@Operation(id = AthentoDocumentUnlinkOperation.ID, category = "Athento", label = "Athento Document Unlink", description = "Remove a document link for a source document")
public class AthentoDocumentUnlinkOperation extends AbstractAthentoOperation {

    public static final String ID = "Athento.Document.Unlink";

    public static final String CONFIG_OPERATION_ID_PRE = "automationExtendedConfig:documentUnlinkOperationIdPre";
    public static final String CONFIG_OPERATION_ID_POST = "automationExtendedConfig:documentUnlinkOperationIdPost";

    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentUnlinkOperation.class);

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "properties", required = false)
    protected Properties properties;

    /**
     * Run operation.
     *
     * @param doc
     * @return
     * @throws Exception
     */
    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        LOG.info("Unlinking a document " + doc.getId());
        if ("default".equals(doc.getLifeCyclePolicy())) {
            session.followTransition(doc.getRef(), "delete");
        }
        return doc;
    }


}