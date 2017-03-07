/**
 * 
 */
package org.athento.nuxeo.workers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.client.model.StreamBlob;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.storage.StorageBlob;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.platform.picture.api.adapters.AbstractPictureAdapter;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Update pictures worker.
 *
 * @author athento
 *
 */
public class UpdatePicturesWorker extends AbstractWork {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(UpdatePicturesWorker.class);

	public static final String CATEGORY = "AthentoUpdatePictures";

	private DocumentModelList list;

	/**
	 * Constructor.
	 *
	 * @param docList of document to update
     */
	public UpdatePicturesWorker(DocumentModelList docList) {
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
	public void work() throws Exception {
		initSession();
		float percent = 0;
		long updated = 0L;
		long total = this.list.size();
		setProgress(new Progress(percent));
		for (DocumentModel doc : this.list) {
			try {
				if (LOG.isInfoEnabled()) {
					LOG.info("Updating picture for " + doc.getId() + "(" + updated + "/" + total + ") [" + percent + "%]");
				}
                Framework.login();
                StorageBlob content = (StorageBlob) doc.getPropertyValue("file:content");
				if (content != null) {
					File tmpFile = File.createTempFile("update-picture-athento", "tmp");
					tmpFile.deleteOnExit();
					FileUtils.copyInputStreamToFile(content.getStream(), tmpFile);
					FileBlob blob = new FileBlob(tmpFile);
					blob.setMimeType(content.getMimeType());
					blob.setFilename(content.getFilename());
					doc.setPropertyValue("file:content", blob);
					session.saveDocument(doc);
                    session.save();
				}
				updated++;
				percent = ((float) updated / total) * 100;
				setProgress(new Progress(percent));
			} catch (ClientException e) {
				LOG.error("Problems updating picture for " + doc.getId(), e);
			} finally {
                commitOrRollbackTransaction();
                startTransaction();
            }
		}
	}

}
