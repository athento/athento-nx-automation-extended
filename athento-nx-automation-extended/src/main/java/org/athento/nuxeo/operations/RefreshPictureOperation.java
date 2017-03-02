/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.workers.PrepareUpdatePicturesWorker;
import org.athento.nuxeo.workers.UpdatePicturesWorker;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

/**
 * Run an operation to refresh a pictures given a document type. This operation uses Document.ElasticQuery
 * operation to query by document type.
 *
 * @author victorsanchez
 */
@Operation(id = RefreshPictureOperation.ID, label = "Refresh pictures", description = "Refresh all picture documents filter by document type")
public class RefreshPictureOperation {

    public static final String ID = "Athento.RefreshPicture";

    private static final Log LOG = LogFactory.getLog(RefreshPictureOperation.class);

    /** Core session. */
    @Context
    protected CoreSession session;

    /**
     * Is the document type.
     */
    @Param(name = "documentType", required = false)
    protected String documentType = "File";

    /** Block size. */
    @Param(name = "blockSize", required = false)
    private int blockSize = 100;

    /** Init Page. */
    @Param(name = "initPage", required = false)
    private int initPage = 0;

    /**
     * Condition.
     */
    @Param(name = "condition", required = false)
    private String condition;

    /** Max iterations. */
    @Param(name = "iters", required = false)
    private int iters = -1;


    /**
     * Run operation for a document.
     *
     * @param doc
     * @throws Exception
     */
    @OperationMethod
    public void run(DocumentModel doc) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Update picture for only one document " + doc.getId());
        }
        DocumentModelList list = new DocumentModelListImpl();
        list.add(doc);
        // Init update worker
        UpdatePicturesWorker worker = new UpdatePicturesWorker(list);
        startWorker(worker);
    }

    /**
     * Run operation for document list.
     *
     * @throws Exception
     */
    @OperationMethod
    public void run() throws Exception {
        // Start preparing update pictures worker
        PrepareUpdatePicturesWorker prepareUpdatePictures = new PrepareUpdatePicturesWorker(documentType, blockSize, condition, iters, initPage);
        startWorker(prepareUpdatePictures);
    }

    /**
     * Start worker.
     *
     * @param worker
     */
    private void startWorker(Work worker) {
        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        workManager.schedule(worker, WorkManager.Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);
    }

}
