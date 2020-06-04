package org.athento.nuxeo.service;

import java.io.Serializable;


/**
 * Automation Registry Service.
 */
public interface AutomationRegistryService extends Serializable {

    /**
     * Register a new query request.
     *
     * @param query
     * @param author
     * @param ip
     * @param pageSize
     * @param firstPage
     * @param startDate
     * @param endDate
     */
    void registerQueryRequest(String query, String author, String ip, int pageSize, int firstPage, long startDate, long endDate);

    /**
     * Register a new request.
     *
     * @param payload
     * @param author
     * @param ip
     */
    void registerRequest(String payload, String author, String ip);

    /**
     * Get information about query.
     *
     * @param query
     */
    void getInformationAboutQuery(String query);
}