/**
 * 
 */
package org.athento.nuxeo.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.trackers.files.FileEventTracker;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentQueryOperation.ID, category = "Athento", label = "Athento Document Query", description = "Query for documents in Athento's way")
public class AthentoDocumentQueryOperation extends AbstractAthentoOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentQueryOperation.class);

    /** Operation ID. */
    public static final String ID = "Athento.Document.Query";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentQueryWatchedDocumentTypes";
    public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentQueryOperationId";
    public static final String DESC = "DESC";

    public static final String ASC = "ASC";

    /**
     * Relations fetch modes.
     */
    public enum RELATION_FETCHMODE {
        all, sources, destinies
    }

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "query", required = true)
    protected String query;

    @Param(name = "maxResults", required = false)
    protected String maxResults = "20";

    @Param(name = "currentPageIndex", required = false)
    protected Integer currentPageIndex = 0;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize = 20;

    @Param(name = "providerName", required = false)
    protected String providerName;

    @Param(name = "sortBy", required = false, description = "Sort by "
        + "properties (separated by comma)")
    protected String sortBy;

    @Param(name = "sortOrder", required = false, description = "Sort order, "
        + "ASC or DESC", widget = Constants.W_OPTION, values = {
        AthentoDocumentQueryOperation.ASC, AthentoDocumentQueryOperation.DESC })
    protected String sortOrder;

    @Param(name = "relationFetchMode", required = false, description = "It is the fetch mode of relation", values = { "all", "sources", "destinies" })
    protected String relationFetchMode = RELATION_FETCHMODE.all.name();

    @OperationMethod
    public DocumentModelList run() throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        ArrayList<String> docTypes = getDocumentTypesFromQuery();
        try {
            String modifiedQuery = query;
            Map<String, Object> config = AthentoOperationsHelper
                .readConfig(session);
            String watchedDocumentTypes = String.valueOf(config
                .get(AthentoDocumentQueryOperation.CONFIG_WATCHED_DOCTYPE));
            String operationId = String.valueOf(config
                .get(AthentoDocumentQueryOperation.CONFIG_OPERATION_ID));
            if (isWatchedDocumentType(docTypes, watchedDocumentTypes)) {
                Object input = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("query", query);
                Object retValue = AthentoOperationsHelper.runOperation(
                    operationId, input, params, session);
                modifiedQuery = String.valueOf(retValue);
            }
            // Check relation fetchMode
            if (relationFetchMode != null) {
                if (RELATION_FETCHMODE.destinies.name().equals(relationFetchMode)) {
                    modifiedQuery += " AND ((athentoRelation:sourceDoc is null AND athentoRelation:destinyDoc is null) " +
                            "OR (athentoRelation:sourceDoc is not null AND athentoRelation:destinyDoc is null))";
                } else if (RELATION_FETCHMODE.sources.name().equals(relationFetchMode)) {
                    modifiedQuery += " AND (athentoRelation:sourceDoc is null AND athentoRelation:destinyDoc is not null)";
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Query " + query);
            }
            // Execute query
            Object input = null;
            operationId = "Document.ElasticQuery";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("query", modifiedQuery);
            params.put("offset", getOffset());
            params.put("limit", pageSize);
            Object retValue = AthentoOperationsHelper.runOperation(operationId,
                input, params, session);
            if (retValue instanceof DocumentModelList) {
                return (DocumentModelList) retValue;
            } else {
                LOG.error("Unexpected return type [" + retValue.getClass()
                    + "] for operation: " + operationId);
                return null;
            }
        } catch (Exception e) {
            LOG.error(
                "Unable to complete operation: "
                    + AthentoDocumentQueryOperation.ID + " due to: "
                    + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }

    private int getOffset() {
        return currentPageIndex * pageSize;
    }

    private ArrayList<String> getDocumentTypesFromQuery() {
        String upperQuery = query.toUpperCase();
        try {
            int idx1 = upperQuery.indexOf(StringUtils.FROM);
            int idx2 = upperQuery.indexOf(StringUtils.WHERE);
            String subquery = query.substring(idx1 + StringUtils.FROM.length(),
                idx2);
            subquery = subquery.trim();
            return StringUtils.asList(subquery, StringUtils.COMMA);
        } catch (Throwable t) {
            LOG.error("Error looking for document Type in query: " + query, t);
        }
        return new ArrayList<String>();
    }

    private boolean isWatchedDocumentType(ArrayList<String> docTypes,
        String watchedDocumentTypes) {
        if (StringUtils.isNullOrEmpty(watchedDocumentTypes)) {
            return false;
        }
        boolean isIncluded = false;
        for (String docType : docTypes) {
            isIncluded = StringUtils.isIncludedIn(docType,
                watchedDocumentTypes, StringUtils.COMMA);
            if (isIncluded) {
                break;
            }
        }
        return isIncluded;
    }
}
