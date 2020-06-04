/**
 *
 */
package org.athento.nuxeo.operations;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.RegisterHelper;
import org.athento.utils.RelationFetchMode;
import org.athento.utils.SecurityUtil;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.RecordSet;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author athento
 */
@Operation(id = AthentoDocumentResultSetOperation.ID, category = "Athento", label = "Athento Document ResultSet", description = "ResultSets a document in Athento's way")
public class AthentoDocumentResultSetOperation extends AbstractAthentoOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(AthentoDocumentResultSetOperation.class);

    public static final String ID = "Athento.Document.ResultSet";

    public static final String CONFIG_WATCHED_DOCTYPE = "automationExtendedConfig:documentResultSetWatchedDocumentTypes";
    public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentResultSetOperationId";
    public static final String DESC = "DESC";

    public static final String ASC = "ASC";

    @Context
    protected CoreSession session;

    /**
     * Operation context.
     */
    @Context
    protected OperationContext ctx;

    @Param(name = "query", required = false)
    protected String query;

    @Param(name = "maxResults", required = false)
    protected String maxResults = "20";

    @Param(name = "page", required = false)
    protected Integer page = 0;

    @Param(name = "offset", required = false)
    protected Integer offset = 0;

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
            AthentoDocumentResultSetOperation.DESC})
    protected String sortOrder;

    @Param(name = "relationFetchMode", required = false, description = "It is the fetch mode of relation", values = {"all", "sources", "destinies"})
    protected String relationFetchMode = RelationFetchMode.all.name();

    /**
     * Field list params.
     */
    @Param(name = "fieldList", required = false)
    protected StringList fieldList;

    @OperationMethod
    public RecordSet run() throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        // Get session from context
        session = ctx.getCoreSession();
        ArrayList<String> docTypes = getDocumentTypesFromQuery();
        try {
            String modifiedQuery;
            // Check if query is ciphered
            if (!query.startsWith("SELECT")) {
                String secret = Framework.getProperty("athento.cipher.secret", null);
                modifiedQuery = SecurityUtil.decrypt_data(secret, query);
            } else {
                modifiedQuery = query;
            }
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
            // Check relation fetchMode
            if (relationFetchMode != null) {
                if (RelationFetchMode.destinies.name().equals(relationFetchMode)) {
                    modifiedQuery = modifiedQuery.replace("WHERE", "WHERE ((athentoRelation:sourceDoc is null AND athentoRelation:destinyDoc is null) " +
                            "OR (athentoRelation:sourceDoc is not null AND athentoRelation:destinyDoc is null)) AND ");
                } else if (RelationFetchMode.sources.name().equals(relationFetchMode)) {
                    modifiedQuery = modifiedQuery.replace("WHERE", "WHERE (athentoRelation:sourceDoc is null AND athentoRelation:destinyDoc is not null) AND ");
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Fetch mode changes: " + modifiedQuery);
                }
            }
            // Check max operators
            if (!AthentoOperationsHelper.isValidQueryOperators(session, modifiedQuery)) {
                throw new Exception("Query is too big");
            }
            Object input = null;
            operationId = "Resultset.PageProvider";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("query", modifiedQuery);
            params.put("page", offset != 0 ? offset : page);
            params.put("pageSize", pageSize);
            params.put("fieldList", fieldList);
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
                RegisterHelper.registerQuery(modifiedQuery, session.getPrincipal().getName(),
                        ((HttpServletRequest) ctx.get("request")).getRemoteAddr(),
                        pageSize, page, startTime, endTime);
                return (RecordSet) retValue;
            } else {
                LOG.error("Unexpected return type for operation: "
                        + operationId);
                return null;
            }
        } catch (Exception e) {
            LOG.error(
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
            LOG.error("Error looking for document Type in query: " + query, t);
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

}
