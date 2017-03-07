/**
 * 
 */
package org.athento.nuxeo.workers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.StreamBlob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.storage.StorageBlob;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

/**
 * Prepare update pictures worker.
 *
 * @author athento
 *
 */
public class PrepareUpdatePicturesWorker extends AbstractWork {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(PrepareUpdatePicturesWorker.class);

	public static final String CATEGORY = "AthentoUpdatePictures";

	/** QUERY. */
	private static final String QUERY = "SELECT * FROM %s WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";


	private String docType;
    private int blockSize;
    private int iters;

	/**
	 * Constructor.
	 *
	 * @param docType of document to update
     */
	public PrepareUpdatePicturesWorker(String docType, int blockSize, int iters) {
		this.docType = docType;
        this.blockSize = blockSize;
        this.iters = iters;
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
		// Making query
		String query = String.format(QUERY, this.docType);

		if (LOG.isInfoEnabled()) {
			LOG.info("Prepare update pictures for document type= " + docType);
		}

        DocumentModelList docList = null;

        int currentPage = 0;
        int tmpIters = 0;

        // Build and execute the ES query
        ElasticSearchService ess = Framework.getLocalService(ElasticSearchService.class);
        try {
            do {
                int offset = currentPage * blockSize;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Preparing block " + offset + " with size " + blockSize);
                }
                NxQueryBuilder nxQuery = new NxQueryBuilder(session).nxql(query)
                        .limit(blockSize).offset(offset);
                docList = ess.query(nxQuery);

                // Update list picture as worker
                UpdatePicturesWorker worker = new UpdatePicturesWorker(docList);
                startWorker(worker);

                currentPage++;
                if (tmpIters == iters) {
                    break;
                }
                tmpIters++;
            } while (docList.size() > 0);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }

	}

	/**
	 * Start worker.
	 *
	 * @param worker
	 */
	private void startWorker(Work worker) {
		WorkManager workManager = Framework.getLocalService(WorkManager.class);
		workManager.schedule(worker, WorkManager.Scheduling.ENQUEUE);
	}

}
