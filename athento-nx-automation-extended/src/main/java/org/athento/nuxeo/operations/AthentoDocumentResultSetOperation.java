/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.nuxeo.service.AutomationRegistryService;
import org.athento.utils.RegisterHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.RecordSet;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

import java.util.*;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentResultSetOperation.ID, category = "Athento", label = "Athento Document ResultSet", description = "ResultSets a document in Athento's way")
public class AthentoDocumentResultSetOperation extends AbstractAthentoOperation {

    public static final String ID = "Athento.Document.ResultSet";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentResultSetWatchedDocumentTypes";
    public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentResultSetOperationId";
    public static final String DESC = "DESC";

    public static final String ASC = "ASC";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "query", required = false)
    protected String query;

    @Param(name = "maxResults", required = false)
    protected String maxResults = "20";

    @Param(name = "page", required = false)
    protected Integer page = 0;

    @Param(name = "pageSize", required = false)
    protected Integer pageSize = 20;

    @Param(name = "providerName", required = false)
    protected String providerName;

    @Param(name = "sortBy", required = false, description = "Sort by "
        + "properties (separated by comma)")
    protected String sortBy;

    @Param(name = "sortOrder", required = false, description = "Sort order, "
        + "ASC or DESC", widget = Constants.W_OPTION, values = {
        AthentoDocumentResultSetOperation.ASC,
        AthentoDocumentResultSetOperation.DESC })
    protected String sortOrder;

    @OperationMethod
    public RecordSet run() throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        ArrayList<String> docTypes = getDocumentTypesFromQuery();
        try {
            String modifiedQuery = query;
            Map<String, Object> config = AthentoOperationsHelper
                .readConfig(session);
            String watchedDocumentTypes = String.valueOf(config
                .get(AthentoDocumentResultSetOperation.CONFIG_WATCHED_DOCTYPE));
            String operationId = String.valueOf(config
                .get(AthentoDocumentResultSetOperation.CONFIG_OPERATION_ID));
            if (isWatchedDocumentType(docTypes, watchedDocumentTypes)) {
                Object input = null;
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("query", query);
                Object retValue = AthentoOperationsHelper.runOperation(
                    operationId, input, params, session);
                modifiedQuery = String.valueOf(retValue);
            }
            Object input = null;
            operationId = "Resultset.PageProvider";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("query", modifiedQuery);
            params.put("page", page);
            params.put("pageSize", pageSize);
            if (!StringUtils.isNullOrEmpty(sortBy)) {
                params.put("sortBy", sortBy);
                if (!StringUtils.isNullOrEmpty(sortOrder)) {
                    params.put("sortOrder", sortOrder);
                }
            }
            long startTime = System.currentTimeMillis();
            Object retValue = AthentoOperationsHelper.runOperation(operationId,
                input, params, session);
            if (retValue instanceof RecordSet) {
                long endTime = System.currentTimeMillis();
                // Register an entry into queryRequest registry
                RegisterHelper.registerQuery(modifiedQuery, pageSize, page, startTime, endTime);
                return (RecordSet) retValue;
            } else {
                _log.error("Unexpected return type for operation: "
                    + operationId);
                return null;
            }
        } catch (Exception e) {
            _log.error(
                "Unable to complete operation: "
                    + AthentoDocumentResultSetOperation.ID + " due to: "
                    + e.getMessage(), e);
            if (e instanceof AthentoException) {
                throw e;
            }
            AthentoException exc = new AthentoException(e.getMessage(), e);
            throw exc;
        }
    }

    private ArrayList<String> getDocumentTypesFromQuery() {
        String upperQuery = query.toUpperCase();
        try {
            int idx1 = upperQuery.indexOf(StringUtils.FROM);
            int idx2 = upperQuery.indexOf(StringUtils.WHERE);
            String subquery;
            if (idx2 > idx1) {
                subquery = query.substring(idx1 + StringUtils.FROM.length(),
                    idx2);
            } else {
                subquery = query.substring(idx1 + StringUtils.FROM.length());
            }
            subquery = subquery.trim();
            return StringUtils.asList(subquery, StringUtils.COMMA);
        } catch (Throwable t) {
            _log.error("Error looking for document Type in query: " + query, t);
        }
        return new ArrayList<>();
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

    private static final Log _log = LogFactory
        .getLog(AthentoDocumentResultSetOperation.class);

}
