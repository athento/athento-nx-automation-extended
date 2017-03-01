/**
 * 
 */
package org.athento.nuxeo.workers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.Serializable;

/**
 * Update videos worker.
 *
 * @author athento
 *
 */
public class UpdateVideosWorker extends AbstractWork {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(UpdateVideosWorker.class);

	public static final String CATEGORY = "AthentoUpdateVideos";

	private DocumentModelList list;

	/**
	 * Constructor.
	 *
	 * @param docList of document to update
     */
	public UpdateVideosWorker(DocumentModelList docList) {
		this.list = docList;
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
		float percent = 0;
		long updated = 0L;
		long total = this.list.size();
		setProgress(new Progress(percent));
		for (DocumentModel doc : this.list) {
			try {
				if (LOG.isInfoEnabled()) {
					LOG.info("Updating video for " + doc.getId() + "(" + updated + ")");
				}
                Framework.login();
                Blob content = (Blob) doc.getPropertyValue("file:content");
				if (content != null) {
					doc.setPropertyValue("file:content", (Serializable) content);
					session.saveDocument(doc);
                    session.save();
				}
				updated++;
				percent = ((float) updated / total) * 100;
				setProgress(new Progress(percent));
			} catch (Exception e) {
				LOG.error("Problems updating video for " + doc.getId(), e);
			} finally {
                commitOrRollbackTransaction();
                startTransaction();
            }
		}
	}

}
