package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Operation(id = GetUsersByFilter.ID, category = "Athento", label = "Search users using specified filters", description = "Gets a list of users matching a filter criteria")
public class GetUsersByFilter extends AbstractAthentoOperation {

    private static final Log _log = LogFactory
            .getLog(AthentoDocumentQueryOperation.class);

    public final static String ID = "Athento.GetUsersByFilter";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "filtername")
    protected String filterName;

    @Param(name = "filtervalue")
    protected String filterValue;

    @Param(name = "output")
    protected String outputName;


    @OperationMethod
    public void run() throws AthentoException {
        getUsersByFilter(ctx, filterName, filterValue, outputName);
    }

    private void getUsersByFilter(OperationContext ctx, String filterName, String filterValue, String outputName) {
        UserManager userManager = Framework.getService(UserManager.class);
        Map<String, Serializable> filter = new HashMap<>();
        filter.put(filterName, filterValue);
        DocumentModelList users = userManager.searchUsers(filter, null);
        ctx.put(outputName,users);
    }

}