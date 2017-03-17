/**
 *
 */
package org.athento.nuxeo.operations.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.utils.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author athento
 */
public class AthentoOperationsHelper {

    public static final String CONFIG_PATH = "/ExtendedConfig";

    public static boolean isWatchedDocumentType(DocumentModel doc,
                                                String documentType, String watchedDocumentTypes) {
        if (StringUtils.isNullOrEmpty(watchedDocumentTypes)) {
            return false;
        }
        String docType = documentType;
        if (StringUtils.isNullOrEmpty(documentType)) {
            docType = doc.getType();
        }
        return StringUtils.isIncludedIn(docType, watchedDocumentTypes,
                StringUtils.COMMA);
    }

    public static Throwable getRootCause(Throwable e) {
        if (e == null)
            return null;
        Throwable cause = e.getCause();
        if (cause != null) {
            return AthentoOperationsHelper.getRootCause(cause);
        }
        return e;
    }

    public static Object runOperation(String operationId, Object input,
                                      Map<String, Object> params, CoreSession session)
            throws OperationException {
        AutomationService automationManager = Framework
                .getLocalService(AutomationService.class);
        // Input setting
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(input);
        Object o = null;
        // Setting parameters of the chain
        try {
            // Run Automation service
            if (_log.isDebugEnabled()) {
                _log.debug("## Running operation: " + operationId);
                _log.debug(" params: " + params);
            }
            o = automationManager.run(ctx, operationId, params);
            if (_log.isDebugEnabled()) {
                _log.debug("## Result: " + o);
            }
        } catch (Exception e) {
            _log.error("Unable to run operation: " + operationId
                    + " Exception: " + e.getMessage(), e);
            Throwable cause = e;
            if (!(e instanceof AthentoException)) {
                cause = AthentoOperationsHelper.getRootCause(e);
                _log.error("Retrieving cause: " + cause.getMessage());
            }
            AthentoException exc = new AthentoException(cause.getMessage(),
                    cause);
            throw exc;
        }
        return o;
    }

    public static Map<String, Object> readConfig(CoreSession session) {
        Map<String, Object> config = new HashMap<String, Object>();
        DocumentModel conf = session.getDocument(new PathRef(
                AthentoOperationsHelper.CONFIG_PATH));
        for (String schemaName : conf.getSchemas()) {
            Map<String, Object> metadata = conf.getProperties(schemaName);
            for (String keyName : metadata.keySet()) {
                String key = keyName;
                Object val = conf.getPropertyValue(key);
                config.put(key, val);
            }
        }
        return config;
    }

    public static String readConfigValue(CoreSession session, String key) {
        DocumentModel conf = session.getDocument(new PathRef(
                AthentoOperationsHelper.CONFIG_PATH));
        return String.valueOf(conf.getPropertyValue(key));
    }

    private static final Log _log = LogFactory
            .getLog(AthentoOperationsHelper.class);

    /**
     * Transform properties to {@link Map} of parameters.
     *
     * @param properties are the properties
     * @return map
     */
    public static Map<String, Object> transformPropertiesToParams(Map<String, String> properties) {
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        return params;
    }

    /**
     * Transform properties as string.
     *
     * @param properties
     * @return properties as string
     */
    public static String transformPropertiesAsString(LinkedHashMap<String, Object> properties) {
        StringBuffer propertiesString = new StringBuffer();
        for (Map.Entry<String, Object> props : properties.entrySet()) {
            propertiesString.append(props.getKey()).append("=").append(props.getValue()).append("\n");
        }
        return propertiesString.toString();
    }

    /**
     * Transform properties as string.
     *
     * @param node
     * @return properties as string
     */
    public static String transformPropertiesAsString(ObjectNode node) {
        StringBuffer propertiesString = new StringBuffer();
        for (Iterator<JsonNode> it = node.iterator(); it.hasNext();) {
            JsonNode n = it.next();
            _log.info("node " + n);
            for (Iterator<String> itFields = n.getFieldNames(); itFields.hasNext();) {
                String fieldName = itFields.next();
                propertiesString.append(fieldName).append("=").append(n.get(fieldName)).append("\n");
            }
        }
        return propertiesString.toString();
    }

    /**
     * Check total operators.
     *
     * @param session
     * @param query
     * @return
     */
    public static boolean isValidQueryOperators(CoreSession session, String query) {
        int maxOperators = -1;
        try {
            maxOperators = Integer.valueOf(AthentoOperationsHelper.readConfigValue(session, "maxQueryOperators"));
        } catch (NumberFormatException e) {
            _log.warn("Error in extended config value for max query operators");
        }
        if (maxOperators <= 0) {
            return true;
        }
        int ands = org.apache.commons.lang.StringUtils.countMatches(query.toLowerCase(), " and ");
        int ors = org.apache.commons.lang.StringUtils.countMatches(query.toLowerCase(), " or ");
        return ands + ors < maxOperators;
    }
}
