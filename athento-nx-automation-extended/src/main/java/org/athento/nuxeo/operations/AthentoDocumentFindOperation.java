/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import java.util.HashMap;
import java.util.Map;

/**
 * Athento document find operation.
 *
 * @author victorsanchez
 *
 */
@Operation(id = AthentoDocumentFindOperation.ID, category = "Athento", label = "Athento Document Find", description = "Find a document given a properties")
public class AthentoDocumentFindOperation extends AbstractAthentoOperation {

    /** LOG. */
    private static final Log LOG = LogFactory
            .getLog(AthentoDocumentFindOperation.class);

    /** Operation ID. */
    public static final String ID = "Athento.DocumentFind";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "properties", required = true)
    protected Properties properties;

    @Param(name = "onlyOne", required = false)
    protected boolean onlyOne = true;

    @Param(name = "includeDeleted", required = false)
    protected boolean includeDeleted = false;

    @OperationMethod()
    public DocumentModel run() throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        // Make query
        String findQuery = makeQuery();
        if (LOG.isInfoEnabled()) {
            LOG.info("Finding with query: " + findQuery);
        }
        // Params for query
        Map<String, Object> params = new HashMap<>();
        params.put("query", findQuery);
        params.put("offset", -1);
        params.put("limit", onlyOne ? 1 : -1);
        // Execute query
        DocumentModelList list = (DocumentModelList) AthentoOperationsHelper.runOperation(
                "Document.ElasticQuery", null, params, session);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**f
     * Make the NXQL query from properties.
     *
     * @return query
     */
    private String makeQuery() {
        String query = "SELECT * FROM Document WHERE " +
                "ecm:mixinType != 'HiddenInNavigation' AND " +
                "ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0";
        if (!includeDeleted) {
            query += " AND ecm:currentLifeCycleState != 'deleted' ";
        }
        Properties props = (Properties) properties.clone();
        for (Map.Entry<String, String> property : props.entrySet()) {
            String metadata = property.getKey();
            String value = property.getValue();
            boolean like = false;
            boolean ilike = false;
            if (metadata.startsWith("%")) {
                like = true;
                metadata = metadata.substring(1);
            } else if (metadata.startsWith("i%")) {
                ilike = true;
                metadata = metadata.substring(2);
            }
            if (value instanceof String) {
                value = "'" + value + "'";
            }
            query += " AND " + metadata + (like ? " LIKE " : (ilike ? " ILIKE " : " = ")) + value;

        }
        return query;
    }

}