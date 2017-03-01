/**
 *
 */
package org.athento.nuxeo.workers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

/**
 * Prepare update videos worker.
 *
 * @author athento
 */
public class PrepareUpdateVideosWorker extends AbstractWork {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(PrepareUpdateVideosWorker.class);

    public static final String CATEGORY = "AthentoUpdateVideos";

    /**
     * QUERY.
     */
    private static final String QUERY = "SELECT * FROM %s WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";


    private String docType;
    private int blockSize;
    private String condition;

    /**
     * Constructor.
     *
     * @param docType of document to update
     */
    public PrepareUpdateVideosWorker(String docType, int blockSize, String condition) {
        this.docType = docType;
        this.blockSize = blockSize;
        this.condition = condition;
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
    public void work() {
        initSession();
        // Making query
        String query = String.format(QUERY + (condition != null ? " AND " + condition : ""), this.docType);

        if (LOG.isInfoEnabled()) {
            LOG.info("Prepare update videos for document type= " + docType + " and query " + query);
        }

        DocumentModelList docList = null;

        int currentPage = 0;

        // Build and execute the ES query
        ElasticSearchService ess = Framework.getLocalService(ElasticSearchService.class);
        do {
            int offset = currentPage * blockSize;
            if (LOG.isInfoEnabled()) {
                LOG.info("Preparing block " + offset + " with size " + blockSize);
            }
            NxQueryBuilder nxQuery = new NxQueryBuilder(session).nxql(query)
                    .limit(blockSize).offset(offset);
            docList = ess.query(nxQuery);

            // Update list videos as worker
            UpdateVideosWorker worker = new UpdateVideosWorker(docList);
            startWorker(worker);

            currentPage++;
        } while (docList.size() > 0);

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
