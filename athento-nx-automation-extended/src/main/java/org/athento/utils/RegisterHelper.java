package org.athento.utils;

import org.athento.nuxeo.service.AutomationRegistryService;
import org.nuxeo.runtime.api.Framework;

import java.util.Calendar;

/**
 * Register request helper.
 */
public final class RegisterHelper {

    /**
     * Register the query information.
     *
     * @param query
     * @param pageSize
     * @param page
     * @param startTimeMillis
     * @param endTimeMillis
     */
    public static final void registerQuery(String query, Integer pageSize, Integer page, long startTimeMillis, long endTimeMillis) {
        AutomationRegistryService registryService = Framework.getService(AutomationRegistryService.class);
        // Add to registry
        registryService.registerQueryRequest(query, pageSize, page, startTimeMillis, endTimeMillis);
    }

}