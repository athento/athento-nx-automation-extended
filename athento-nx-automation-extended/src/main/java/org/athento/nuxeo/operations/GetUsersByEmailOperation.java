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
import java.util.*;

@Operation(id = GetUsersByEmailOperation.ID, category = "Athento", label = "Search users by email", description = "Gets a list of users matching an email")
public class GetUsersByEmailOperation extends AbstractAthentoOperation {

    private static final Log _log = LogFactory
            .getLog(AthentoDocumentQueryOperation.class);

    public final static String ID = "Athento.GetUsersByEmail";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "userEmail")
    protected String userEmail;

    @Param(name = "output")
    protected String outputName;


    @OperationMethod
    public void run() throws AthentoException {
        getUsersByEmail(ctx, userEmail, outputName);
    }

    private void getUsersByEmail(OperationContext ctx, String userEmail, String outputName) {
        UserManager userManager = Framework.getService(UserManager.class);
        Map<String, Serializable> filter = new HashMap<>();
        filter.put("email", userEmail);
        DocumentModelList users = userManager.searchUsers(filter, null);
        ctx.put(outputName,users);
    }

}