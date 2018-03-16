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
@Operation(id = AthentoDocumentPermanentDeleteOperation.ID, category = "Athento", label = "Athento Document Update", description = "Updates a document in Athento's way")
public class AthentoDocumentPermanentDeleteOperation extends AbstractAthentoOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentPermanentDeleteOperation.class);

    /**
     * Operation ID.
     */
    public static final String ID = "Athento.DocumentPermanentDelete";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @OperationMethod(collector = DocumentModelCollector.class)
    public void run(DocumentModel doc) throws Exception {
        if (!session.canRemoveDocument(doc.getRef())) {
            LOG.warn("Document " + doc.getRef() + " cannot be removed.");
        }
        session.removeDocument(doc.getRef());
    }


}