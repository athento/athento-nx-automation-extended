package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.api.DocumentModelListPageProvider;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = ListToProviderOperation.ID, category = "Athento", label = "Transform document list to provider", description = "Transform a document list to a paginable provider.")
public class ListToProviderOperation {

    private static final Log LOG = LogFactory.getLog(ListToProviderOperation.class);

    public static final String ID = "Athento.ListToPaginableProvider";

    @Context
    protected CoreSession session;

    @Param(name = "pageSize", required = false)
    protected int pageSize = 20;

    @Param(name = "page", required = false)
    protected Integer page;

    /**
     * Run method.
     *
     * @param docList
     * @return
     */
    @OperationMethod
    public PaginableDocumentModelListImpl run(DocumentModelList docList) throws Exception {
        DocumentModelListPageProvider provider = new DocumentModelListPageProvider();
        provider.setPageSize(this.pageSize);
        if (page != null) {
            provider.setCurrentPageIndex(page);
        }
        provider.setDocumentModelList(docList);
        return new PaginableDocumentModelListImpl(provider);
    }


}
