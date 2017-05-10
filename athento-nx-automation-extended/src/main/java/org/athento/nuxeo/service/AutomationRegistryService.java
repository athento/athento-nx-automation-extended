package org.athento.nuxeo.service;

import java.io.Serializable;
import java.util.Date;

/**
 * Automation Registry Service.
 */
public interface AutomationRegistryService extends Serializable {

    /**
     * Register a new query request.
     *
     * @param query
     * @param pageSize
     * @param firstPage
     * @param startDate
     * @param endDate
     */
    void registerQueryRequest(String query, int pageSize, int firstPage, long startDate, long endDate);

    /**
     * Get information about query.
     *
     * @param query
     */
    void getInformationAboutQuery(String query);
}