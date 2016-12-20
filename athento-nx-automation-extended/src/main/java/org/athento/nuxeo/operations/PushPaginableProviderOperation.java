package org.athento.nuxeo.operations;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = PushPaginableProviderOperation.ID, category = "Athento", label = "Push a provider", description = "Push a paginable provider")
public class PushPaginableProviderOperation {

    public static final String ID = "Athento.PushProvider";

    @Context
    protected OperationContext ctx;

    @Param(name = "name")
    protected String name;

    @Param(name = "page", required = false, description = "Set current page to provider")
    protected Integer page;

    @Param(name = "pageSize", required = false, description = "Set current page size to provider")
    protected Integer pageSize;

    /**
     * Run method.
     *
     * @return
     */
    @OperationMethod
    public PaginableDocumentModelListImpl run() throws Exception {
        PaginableDocumentModelListImpl list = (PaginableDocumentModelListImpl) ctx.get(name);
        if (list == null) {
            throw new Exception("List " + name + " is not found as context param.");
        }
        if (page != null) {
            list.getProvider().setCurrentPageIndex(page);
        }
        if (pageSize != null) {
            list.getProvider().setPageSize(pageSize);
        }
        ctx.push(name, list);
        return list;
    }


}
