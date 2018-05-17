/**
 * 
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.exception.AthentoException;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.operations.RestOperationContext;
import org.nuxeo.ecm.core.api.CoreSession;


/**
 * @author athento
 *
 */
@Operation(id = AthentoExceptionCreateOperation.ID, category = "Athento", 
label = "Athento Exception Create", description = "Throws an AthentoException")
public class AthentoExceptionCreateOperation {

	public static final String ID = "Athento.Exception.Create";

	@Context
	protected OperationContext ctx;
	
	@Context
	protected CoreSession session;

	@Param(name = "message", required = false)
	protected String message;

	@OperationMethod()
	public OperationException run() throws OperationException{
		AthentoException retVal = null;
		if (_log.isDebugEnabled()) {
		    _log.debug("Running operation: " + AthentoExceptionCreateOperation.ID);
		}
		Object excName = ctx.get("exceptionName");
		Object excObject = ctx.get("exceptionObject");
		_log.error("Registering new AthentoException: " + excObject, (Throwable)excObject);
		int returnCode = 500;
		if (excObject instanceof AthentoException) {
			retVal = (AthentoException) excObject;
		} else if (excObject instanceof Throwable) {
			Throwable t = (Throwable)excObject;
			t = AthentoOperationsHelper.getRootCause(t);
			retVal = new AthentoException(t.getMessage());
		} else {
			if (StringUtils.isNullOrEmpty(message)) {
				message = "Unexpected exception [" + excName + "]: " 
					+ excObject.getClass().getName();
			}
			retVal = new AthentoException(message);
		}
		if (ctx instanceof RestOperationContext) {
			((RestOperationContext) ctx).setHttpStatus(returnCode);
		}
		retVal.setStatus(returnCode);
		return retVal;
	}

	private static final Log _log = LogFactory
			.getLog(AthentoExceptionCreateOperation.class);

}