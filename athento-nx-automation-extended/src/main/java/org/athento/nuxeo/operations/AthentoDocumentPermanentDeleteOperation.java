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
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.platform.tag.TagService;

import java.util.HashMap;
import java.util.Map;

import static org.nuxeo.runtime.transaction.TransactionHelper.commitOrRollbackTransaction;
import static org.nuxeo.runtime.transaction.TransactionHelper.startTransaction;

/**
 * @author athento
 */
@Operation(id = AthentoDocumentPermanentDeleteOperation.ID, category = "Athento", label = "Athento Document Permanent Delete", description = "Delete a document in Athento's way")
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

    @Param(name = "docId", required = false)
    protected String docId;

    /** Total deleted. */
    private long total = 0;

    /**
     * Run.
     *
     * @throws Exception
     */
    @OperationMethod()
    public void run() throws Exception {
        if (docId == null){
            throw new Exception("docId parameter is mandatory to remove the document");
        }
        IdRef docRef = new IdRef(docId);
        run(docRef);
    }

    /**
     * Run.
     *
     * @throws Exception
     */
    @OperationMethod()
    public void run(DocumentRef docRef) throws Exception {
        if (!session.canRemoveDocument(docRef)) {
            LOG.warn("Document " + docId + " cannot be removed.");
        } else {
            DocumentModel doc = session.getDocument(docRef);
            if (doc.isFolder()) {
                // Recursively
                deleteFolder(doc.getRef());
            }
            session.removeDocument(docRef);
            total++;
            LOG.info("Total removed = " + total);
        }
    }

    private void deleteFolder(DocumentRef docRef) {
        DocumentModelIterator it = session.getChildrenIterator(docRef);
        while (it.hasNext()) {
            DocumentModel document = it.next();
            if (document.isFolder()) {
                deleteFolder(document.getRef());
            }
            LOG.info("Removing permanent for " + document.getRef() + " (" + total + ")");
            session.removeDocument(document.getRef());
            commitOrRollbackTransaction();
            startTransaction();
            total++;
        }
    }

}