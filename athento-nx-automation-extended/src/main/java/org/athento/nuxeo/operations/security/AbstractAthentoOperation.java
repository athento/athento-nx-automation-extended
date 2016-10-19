package org.athento.nuxeo.operations.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by victorsanchez on 17/10/16.
 *
 * @since #AT-987
 */
public abstract class AbstractAthentoOperation {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(AbstractAthentoOperation.class);

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
        DocumentModel extendedConfigDoc = ctx.getCoreSession().getDocument(new PathRef("/ExtendedConfig"));

        // Get ips from value
        String enabledIpsValue =
                (String) extendedConfigDoc.getPropertyValue("automationExtendedConfig:enabledIPs");

        // Check ip in list
        if (enabledIpsValue != null && !enabledIpsValue.isEmpty()) {
            String [] ips = enabledIpsValue.split(",");
            for (String ipp : ips) {
                if (ipp.equals(remoteIp)) {
                    return;
                }
            }
            throw new RestrictionException(remoteIp + " has not allowed access.");
        }

    }

}
