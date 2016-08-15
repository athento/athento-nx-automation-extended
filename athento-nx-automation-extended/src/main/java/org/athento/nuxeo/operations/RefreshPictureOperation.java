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
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.AbstractProperty;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

/**
 * Run an operation to refresh a pictures given a document type. This operation uses Document.ElasticQuery
 * operation to query by document type.
 *
 * @author victorsanchez
 */
@Operation(id = RefreshPictureOperation.ID, label = "Refresh pictures", description = "Refresh all document pictures filter by document type")
public class RefreshPictureOperation {

    public static final String ID = "Athento.RefreshPicture";

    private static final Log LOG = LogFactory.getLog(RefreshPictureOperation.class);

    /** QUERY. */
    private static final String QUERY = "SELECT * FROM %s WHERE ecm:mixinType != 'HiddenInNavigation' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";

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

    private long updated = 0;

    @OperationMethod
    public void run() throws Exception {

        // Making query
        String query = String.format(QUERY, documentType);

        LOG.info("Updating pictures for document type= " + documentType);

        DocumentModelList docList = null;

        int currentPage = 0;

        // Build and execute the ES query
        ElasticSearchService ess = Framework.getLocalService(ElasticSearchService.class);
        try {
            do {
                int offset = currentPage * blockSize;
                LOG.info("Getting block " + offset + " with size " + blockSize);
                NxQueryBuilder nxQuery = new NxQueryBuilder(session).nxql(query)
                        .limit(blockSize).offset(offset);
                docList = ess.query(nxQuery);

                // Update list picture
                updatePictureForList(docList);

                currentPage++;
            } while (docList.size() > 0);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }

        LOG.info("Documents picture updated " + updated);
    }

    /**
     * Update content for document list.
     *
     * @param docList list of documents to update
     */
    private void updatePictureForList(DocumentModelList docList) {
        for (DocumentModel doc: docList) {
            try {
                LOG.info("Updating picture for " + doc.getId() + "(" + updated + ")");
                Property content = doc.getProperty("file:content");
                ((AbstractProperty) content).setFlags(Property.IS_MODIFIED);
                session.saveDocument(doc);
                session.save();
                updated++;
            } catch (ClientException e) {
                LOG.error("Problems updating picture for " + doc.getId());
            }
        }
    }

}
