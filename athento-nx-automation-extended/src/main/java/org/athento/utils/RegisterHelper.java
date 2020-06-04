package org.athento.utils;

import org.athento.nuxeo.service.AutomationRegistryService;
import org.nuxeo.runtime.api.Framework;

import javax.servlet.http.HttpServletRequest;

/**
 * Register request helper.
 */
public final class RegisterHelper {

    /**
     * Register the query information.
     *
     * @param query
     * @param author
     * @param ip
     * @param pageSize
     * @param page
     * @param startTimeMillis
     * @param endTimeMillis
     */
    public static final void registerQuery(String query, String author, String ip, Integer pageSize, Integer page, long startTimeMillis, long endTimeMillis) {
        AutomationRegistryService registryService = Framework.getService(AutomationRegistryService.class);
        // Add to registry
        registryService.registerQueryRequest(query, author, ip, pageSize, page, startTimeMillis, endTimeMillis);
    }

    /**
     * Register the request information.
     *
     * @param payload
     * @param author
     * @param ip
     */
    public static final void registerRequest(String payload, String author, String ip) {
        AutomationRegistryService registryService = Framework.getService(AutomationRegistryService.class);
        // Add to registry
        registryService.registerRequest(payload, author, ip);
    }

}
