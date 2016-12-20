package org.athento.nuxeo.operations;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = PushListOperation.ID, category = "Athento", label = "Push a list of documents", description = "Push a document list to context")
public class PushListOperation {

    public static final String ID = "Athento.PushList";

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
    public DocumentModelList run() throws Exception {
        DocumentModelList list = (DocumentModelList) ctx.get(name);
        if (list == null) {
            throw new Exception("List " + name + " is not found as context param.");
        }
        ctx.push(name, list);
        return list;
    }


}
