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
import org.nuxeo.ecm.core.api.IdRef;
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

    @Context
    protected String docId;

    /**
     * Run.
     *
     * @throws Exception
     */
    public void run() throws Exception {
        IdRef docRef = new IdRef(docId);
        if (!session.canRemoveDocument(docRef)) {
            LOG.warn("Document " + docId + " cannot be removed.");
        } else {
            LOG.info("Removing permanent for " + docId);
            session.removeDocument(docRef);
        }
    }

}