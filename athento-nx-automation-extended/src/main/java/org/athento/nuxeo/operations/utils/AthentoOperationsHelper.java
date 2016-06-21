/**
 * 
 */
package org.athento.nuxeo.operations.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
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

	public static Throwable getRootCause(Throwable e) {
		if (e == null) return null;
		Throwable cause = e.getCause();
		if (cause != null) {
			return AthentoOperationsHelper.getRootCause(cause);
		}
		return e;
	}

	public static Object runOperation(
		String operationId, Object input, Map<String,Object> params, CoreSession session) 
		throws OperationException {
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
			_log.error("Unable to run operation: " + operationId + " Exception: " 
				+ e.getMessage(), e);
			Throwable cause = e;
			if (!(e instanceof AthentoException)) {
				cause = AthentoOperationsHelper.getRootCause(e);
				_log.error("Retrieving cause: " + cause.getMessage());
			}
			AthentoException exc = new AthentoException(cause.getMessage(), cause);
			throw exc;
		}
		return o;
	}
	
	public static Map<String, Object> readConfig(CoreSession session) {
		Map<String, Object> config = new HashMap<String,Object>();
		DocumentModel conf = session.getDocument(new PathRef(AthentoOperationsHelper.CONFIG_PATH));
		for (String schemaName : conf.getSchemas()) {
			Map<String, Object> metadata = conf.getProperties(schemaName);
			for (String keyName : metadata.keySet()) {
				String key = keyName;
				Object val = conf.getPropertyValue(key);
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
