/**
 * 
 */
package org.athento.nuxeo.operations;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.scripting.Expression;
import org.nuxeo.ecm.automation.core.scripting.Scripting;

/**
 * @author athento
 *
 */
@Operation(id = RunScriptAsVarOperation.ID, category = "Athento", label = "Run Script As Var", description = "Run a script which content is specified as text in the 'script' parameter and puts its value in var specified by 'name' parameter")
public class RunScriptAsVarOperation {

	public static final String ID = "Athento.RunScriptAsVar";

	@Context
	protected OperationContext ctx;

	@Param(name = "name", widget = Constants.W_TEXT)
	protected String name;

	@Param(name = "script", widget = Constants.W_MULTILINE_TEXT)
	protected String script;

	private volatile Expression expr;

	@OperationMethod
	public Object run() throws Exception {
		if (_log.isDebugEnabled()) {
			_log.debug("Running script: " + script);
		}
		if (_log.isDebugEnabled()) {
			_log.debug("Context is: " + ctx);
		}

		if (expr == null) {
			String text = script.replaceAll("&lt;", "<");
			text = text.replaceAll("&gt;", ">");
			text = text.replaceAll("&amp;", "&");
			if (_log.isDebugEnabled()) {
				_log.debug("replaced text: " + text);
			}
			expr = Scripting.newExpression(text);
		}
		Object result = null;
		if (expr == null) {
			_log.warn("Ignoring invalid expression [" + expr + "] to set var: " + name);
		} else {
			result = expr.eval(ctx);
		}
		if (_log.isDebugEnabled()) {
			_log.debug("Script result: " + result);
		}
		ctx.put(name, result);
		return result;
	}
	
	private static final Log _log = LogFactory.getLog(RunScriptAsVarOperation.class);

}