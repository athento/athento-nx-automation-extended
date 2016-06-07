/**
 * 
 */
package org.athento.nuxeo.operations.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

/**
 * @author athento
 *
 */
public class AthentoOperationsHelper {
	
	public static final String CONFIG_PATH = "/ExtendedConfig";
	public static final String DOCUMENT_TYPE_ATHENTO_EXCEPTION = "AthentoException";
	public static final String KEY_ATHENTO_EXCEPTION_CODE = "athentoException:exception_code";
	public static final String KEY_ATHENTO_EXCEPTION_MESSAGE = "athentoException:exception_message";
	
	public static Object runOperation(String operationId, Object input, Map<String,Object> params, CoreSession session) {
		AutomationService automationManager = Framework.getLocalService(AutomationService.class);
		// Input setting
		OperationContext ctx = new OperationContext(session);
		ctx.setInput(input);
		Object o = null;
		// Setting parameters of the chain
		try {
			// Run Automation service
			if (_log.isDebugEnabled()) {
				_log.debug("Running operation: " + operationId);
			}
			o = automationManager.run(ctx,operationId, params);
			if (_log.isDebugEnabled()) {
				_log.debug("Result: " + o);
			}
		} catch (Exception e) {
			Throwable cause = AthentoOperationsHelper.getRootCause(e);
			String message = cause.getMessage();
			_log.error("Unable to run operation: " + operationId + " cause: " + message, e);
			DocumentModel athentoException = session.createDocumentModel("/", message,
				AthentoOperationsHelper.DOCUMENT_TYPE_ATHENTO_EXCEPTION);
			athentoException.setPropertyValue(KEY_ATHENTO_EXCEPTION_CODE, String.valueOf(cause.getClass()));
			athentoException.setPropertyValue(KEY_ATHENTO_EXCEPTION_MESSAGE, message);
			o = session.createDocument(athentoException);
//			throw new AthentoException(message, cause.getClass().getName());
		}
		return o;
	}
	
	private static Throwable getRootCause(Throwable e) {
		Throwable cause = e.getCause();
		if (cause != null) {
			return AthentoOperationsHelper.getRootCause(cause);
		}
		return e;
	}

	public static Map<String, Object> readConfig(CoreSession session) {
		Map<String, Object> config = new HashMap<String,Object>();
		DocumentModel conf = session.getDocument(new PathRef(AthentoOperationsHelper.CONFIG_PATH));
		for (String schemaName : conf.getSchemas()) {
			Map<String, Object> metadata = conf.getProperties(schemaName);
			for (String keyName : metadata.keySet()) {
				String key = keyName;
				Object val = conf.getPropertyValue(key);
				if (_log.isDebugEnabled()) {
					_log.debug("key ["+key+"] : " + val);
				}
				config.put(key,val);
			}
		}
		return config;
	}
	public static String readConfigValue(CoreSession session, String key) {
		DocumentModel conf = session.getDocument(new PathRef(AthentoOperationsHelper.CONFIG_PATH));
		return String.valueOf(conf.getPropertyValue(key));
	}

	private static final Log _log = LogFactory
		.getLog(AthentoOperationsHelper.class);


}
