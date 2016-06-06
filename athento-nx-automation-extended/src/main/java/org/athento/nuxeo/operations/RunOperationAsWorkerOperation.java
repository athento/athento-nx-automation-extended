/**
 * 
 */
package org.athento.nuxeo.operations;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.workers.RunOperationAsWorkerWorker;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.work.api.Work.State;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;
/**
 * @author athento
 *
 */

@Operation(id = RunOperationAsWorkerOperation.ID, category = "Athento", 
label = "Run an operation as a worker", description = "Launch a worker to run the operation")
public class RunOperationAsWorkerOperation {

	public static final String ID = "Athento.RunOperationAsWorker";

	@Context
	protected OperationContext ctx;

	@Context
	protected AutomationService service;

	@Param(name = "id")
	protected String chainId;

	@Param(name = "isolate", required = false, values = "false")
	protected boolean isolate = false;

	@Param(name = "parameters", description = "Accessible in the subcontext ChainParameters. For instance, @{ChainParameters['parameterKey']}.", required = false)
	protected Properties chainParameters;

	@OperationMethod
	public String run() throws Exception {
		launchWorker();
		return "OK";
	}

	private void launchWorker () {
		RunOperationAsWorkerWorker work = new RunOperationAsWorkerWorker(
			chainId, chainParameters, service, ctx.getSubContext(isolate, ctx.getInput()));
		if (_log.isInfoEnabled()) {
			_log.info("Launching worker... " + work.getCategory() + ":" 
				+ work.getClass().getName());
		}
		WorkManager workManager = Framework.getLocalService(WorkManager.class);
		workManager.schedule(work, WorkManager.Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);
		String workId = work.getId();
		State workState = workManager.getWorkState(workId);
		if (_log.isInfoEnabled()) {
			_log.info("Work [" + workId + "] queued in state [" + workState + "]");
		}
	}
	private static final Log _log = LogFactory.getLog(RunOperationAsWorkerOperation.class);

}