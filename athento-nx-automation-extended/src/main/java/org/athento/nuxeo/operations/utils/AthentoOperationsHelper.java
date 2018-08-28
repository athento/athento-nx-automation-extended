    /**
 *
 */
package org.athento.nuxeo.operations.utils;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.utils.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;

/**
 * @author athento
 */
public class AthentoOperationsHelper {

    private static final Log _log = LogFactory
            .getLog(AthentoOperationsHelper.class);

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
        Map<String, Object> config = new HashMap<>();
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

    /**
     * Read config value.
     *
     * @param session
     * @param key
     * @return
     */
    public static String readConfigValue(CoreSession session, final String key) {
        final List<String> values = new ArrayList<>();
        new UnrestrictedSessionRunner(session) {
            @Override
            public void run() {
                DocumentModel conf = session.getDocument(new PathRef(
                        AthentoOperationsHelper.CONFIG_PATH));
                Serializable value = conf.getPropertyValue(key);
                if (value != null) {
                    values.add(String.valueOf(value));
                }
            }
        }.runUnrestricted();
        if (!values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

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
}
