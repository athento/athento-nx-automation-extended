package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.contexts.Contexts;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;

import java.util.List;

/**
 * Get selected documents override.
 */
@Operation(id = GetSelectedDocumentsOperation.ID, category = Constants.CAT_FETCH, requires = Constants.SEAM_CONTEXT, label = "UI Selected documents", description = "Fetch the documents selected in the current folder listing")
public class GetSelectedDocumentsOperation {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(GetSelectedDocumentsOperation.class);

    public static final String ID = "Seam.GetSelectedDocuments";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @OperationMethod
    public DocumentModelList run() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Getting selected documents from Athento...");
        }
        DocumentModelList docFinal = new DocumentModelListImpl();
        List<DocumentModel> res = getDocumentListManager().getWorkingList(
                DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
        for (DocumentModel documentModel : res) {
            try {
                DocumentModel doc = session.getDocument(documentModel.getRef());
                docFinal.add(doc);
            } catch (Exception e) {
                LOG.warn("Unable to get selected document " + documentModel.getRef(), e);
            }
        }
        return docFinal;
    }

    public static DocumentsListsManager getDocumentListManager() {
        return (DocumentsListsManager) Contexts.getSessionContext().get("documentsListsManager");
    }

}
