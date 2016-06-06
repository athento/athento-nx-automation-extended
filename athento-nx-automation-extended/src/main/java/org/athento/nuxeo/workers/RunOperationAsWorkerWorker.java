/**
 * 
 */
package org.athento.nuxeo.workers;

import java.util.Map;

import javax.ws.rs.core.Context;

import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.work.AbstractWork;

/**
 * @author athento
 *
 */
public class RunOperationAsWorkerWorker extends AbstractWork {

	public RunOperationAsWorkerWorker(
		String _chainId, Properties _chainParameters, AutomationService _service, 
		OperationContext _ctx) {
		chainId = _chainId;
		chainParameters = _chainParameters;
		service = _service;
		subctx = _ctx;
	}
	
	@Override
	public String getTitle() {
		return getCategory();
	}
	@Override
	public String getCategory() {
		return CATEGORY;
	}

	@Override
	public void work() throws Exception {
		initSession();
		float percent = 0;
		setProgress(new Progress(percent));
		try {
			setStatus("Working on: " + chainId);
			service.run(subctx, chainId, (Map) chainParameters);
		} finally {
			commitOrRollbackTransaction();
			startTransaction();
			setProgress(new Progress(100));
			setStatus("Finished");
		}
	}

	private String chainId;

	private Properties chainParameters;
	
	private OperationContext subctx;
	
	private AutomationService service;

	private static final String CATEGORY = "AthentoOperations";
	
	private static final long serialVersionUID = -518077422027928515L;
}
