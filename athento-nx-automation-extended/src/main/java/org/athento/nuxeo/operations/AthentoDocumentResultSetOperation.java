/**
 * 
 */
package org.athento.nuxeo.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.ConflictOperationException;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.RecordSet;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentResultSetOperation.ID, category = "Athento", label = "Athento Document ResultSet", description = "ResultSets a document in Athento's way")
public class AthentoDocumentResultSetOperation {

    public static final String ID = "Athento.Document.ResultSet";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentResultSetWatchedDocumentTypes";
    public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentResultSetOperationId";
    public static final String DESC = "DESC";

    public static final String ASC = "ASC";

    @Context
    protected CoreSession session;

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
    public RecordSet run() throws OperationException {
        if (_log.isDebugEnabled()) {
            _log.debug(AthentoDocumentResultSetOperation.ID
                + " BEGIN with params:");
            _log.debug(" query: " + query);
            _log.debug(" maxResults: " + maxResults);
            _log.debug(" page: " + page);
            _log.debug(" pageSize: " + pageSize);
            _log.debug(" providerName: " + providerName);
            _log.debug(" sortBy: " + sortBy);
            _log.debug(" sortOrder: " + sortOrder);
        }
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
            Object retValue = AthentoOperationsHelper.runOperation(operationId,
                input, params, session);
            if (_log.isDebugEnabled()) {
                _log.debug(AthentoDocumentResultSetOperation.ID
                    + " END return value: " + retValue);
            }
            if (retValue instanceof RecordSet) {
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
            String subquery = query.substring(idx1 + StringUtils.FROM.length(), idx2);
            subquery = subquery.trim();
            return StringUtils.asList(subquery, StringUtils.COMMA);
        } catch (Throwable t) {
            _log.error("Error looking for document Type in query: " + query, t);
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
            if (_log.isDebugEnabled()) {
                _log.debug(" checking docType: " + docType);
            }
            isIncluded = StringUtils.isIncludedIn(docType,
                watchedDocumentTypes, StringUtils.COMMA);
            if (_log.isDebugEnabled()) {
                _log.debug(" ... isIncluded: " + isIncluded);
            }
            if (isIncluded) {
                break;
            }
        }
        return isIncluded;
    }

    private static final Log _log = LogFactory
        .getLog(AthentoDocumentResultSetOperation.class);

}
