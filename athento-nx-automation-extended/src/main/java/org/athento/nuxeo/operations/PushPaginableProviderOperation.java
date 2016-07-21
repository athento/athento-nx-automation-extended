package org.athento.nuxeo.operations;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.DocumentModelList;

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

    /**
     * Run method.
     *
     * @return
     */
    @OperationMethod
    public PaginableDocumentModelListImpl run() throws Exception {
        PaginableDocumentModelListImpl list = (PaginableDocumentModelListImpl) ctx.get(name);
        ctx.push(name, list);
        return list;
    }


}
