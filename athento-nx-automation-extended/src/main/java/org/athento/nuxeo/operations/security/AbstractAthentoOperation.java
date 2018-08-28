package org.athento.nuxeo.operations.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.SecurityUtil;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.features.PrincipalHelper;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.platform.ui.web.auth.NuxeoAuthenticationFilter;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by victorsanchez on 17/10/16.
 *
 * @since #AT-987
 */
public abstract class AbstractAthentoOperation {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(AbstractAthentoOperation.class);

    /** Void chain id. */
    public static final String VOIDCHAIN = "voidchain";

    /**
     * Check if IP has allowed access to operation execution.
     *
     * @param ctx context
     * @throws RestrictionException on error
     */
    protected void checkAllowedAccess(OperationContext ctx) throws RestrictionException {

        // Get remote ip
        HttpServletRequest request = (HttpServletRequest) ctx.get("request");
        if (request == null) {
            LOG.debug("Request info was null to manage controlled access.");
            return;
        }

        String remoteIp = request.getRemoteAddr();

        // Get extended config document
        String enabledIpsValue = AthentoOperationsHelper
                .readConfigValue(ctx.getCoreSession(), "automationExtendedConfig:enabledIPs");

        LOG.info("Enabled IPs " + enabledIpsValue);

        // Check ip in list
        if (enabledIpsValue != null && !enabledIpsValue.isEmpty()) {
            if (!"*".equals(enabledIpsValue)) {
                String[] ips = enabledIpsValue.split(",");
                if (ips.length > 0) {
                    for (String ipp : ips) {
                        if (ipp.trim().equals(remoteIp)) {
                            return;
                        }
                    }
                    throw new RestrictionException(remoteIp + " has not allowed access.");
                }
            }
        }

        // Check login for user with loginAs context var
        NuxeoPrincipal nxPrincipal = (NuxeoPrincipal) ctx.getPrincipal();
        if (SecurityUtil.isLoginAsEnabled() && nxPrincipal.isAdministrator()) {
            // Check if context has loginAs
            String loginAs = (String) ctx.get("loginAs");
            if (loginAs != null) {
                UserManager userManager = Framework.getService(UserManager.class);
                DocumentModel userAs = userManager.getUserModel(loginAs);
                try {
                    Framework.loginAs(loginAs);
                    LoginContext loginContext = NuxeoAuthenticationFilter.loginAs(loginAs);
                    loginContext.login();
                } catch (LoginException e) {
                    throw new RestrictionException("Unable to login as with " + loginAs);
                }
                NuxeoPrincipal loginAsPrincipal = new NuxeoPrincipalImpl(loginAs);
                loginAsPrincipal.setModel(userAs);
                CoreSession session = CoreInstance.openCoreSession(ctx.getCoreSession().getRepositoryName(), loginAsPrincipal);
                ctx.setCoreSession(session);
                if (LOG.isInfoEnabled()) {
                    LOG.info("The user requester " + nxPrincipal.getName() + " now is logged with " + ((NuxeoPrincipal) ctx.getPrincipal()).getGroups());
                }
            }
        }

    }

    /**
     * Check if operationId void chain.
     *
     * @param operationId
     * @return true if operationId is a void chain
     */
    protected boolean isVoidChain(String operationId) {
        return VOIDCHAIN.equalsIgnoreCase(operationId);
    }

}
