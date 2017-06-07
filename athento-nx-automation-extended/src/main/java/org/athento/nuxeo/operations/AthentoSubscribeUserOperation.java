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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ec.notification.NotificationConstants;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.platform.notification.api.NotificationManager;
import org.nuxeo.runtime.api.Framework;


@Operation(id = AthentoSubscribeUserOperation.ID, category = "Athento", label = "Subscribe user to actual document", description = "Subscribe user to actual document")
public class AthentoSubscribeUserOperation extends AbstractAthentoOperation {

    private static final Log _log = LogFactory
            .getLog(AthentoDocumentQueryOperation.class);

    public final static String ID = "Athento.SubscribeUser";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "username")
    protected String username;


    @OperationMethod
    public void run(DocumentModel doc) throws AthentoException {
        subscribeUser(ctx, username, doc);
    }

    private void subscribeUser(OperationContext ctx, String username, DocumentModel doc) {
        if(doc != null){
            UserManager userManager = Framework.getService(UserManager.class);
            NuxeoPrincipal principal = userManager.getPrincipal(username);
            if(principal != null){
                NotificationManager notificationManager = Framework.getService(NotificationManager.class);
                notificationManager.addSubscriptions(NotificationConstants.USER_PREFIX + principal.getName(),doc,true,principal);
            }
        }
    }

}