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
import org.athento.nuxeo.api.model.BatchResult;
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobListCollector;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.*;

import java.util.*;

/**
 * Multi Attach Blob.
 *
 * @author <a href="vs@athento.com">Victor Sanchez</a>
 */
@Operation(id = AthentoMultiAttachBlobOperation.ID, category = Constants.CAT_BLOB, label = "Multi Attach File", description = "Multi attach the input file to the document given as a parameter. If the xpath points to a blob list then the blob is appended to the list, otherwise the xpath should point to a blob property. If the save parameter is set the document modification will be automatically saved. Return the blob.")
public class AthentoMultiAttachBlobOperation extends AbstractAthentoOperation {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(AthentoMultiAttachBlobOperation.class);

    public static final String ID = "Athento.Blob.MultiAttach";

    @Context
    protected CoreSession session;

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "xpath", required = false, values = "file:content")
    protected String xpath = "file:content";

    @Param(name = "documents")
    protected List<String> documents;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @OperationMethod(collector = BlobListCollector.class)
    public BlobList run(Blob blob) throws Exception {
        // Check access
        checkAllowedAccess(ctx);

        List<LinkedHashMap<String, Object>> propertiesList = new ArrayList<>();

        // Make properties for each document
        for (String document : documents) {
            try {
                // Check if document exists
                DocumentModel doc = session.getDocument(new IdRef(document));
                // Prepare properties and add to list
                LinkedHashMap<String, Object> properties = new LinkedHashMap();
                properties.put("xpath", xpath);
                properties.put("save", String.valueOf(save));
                properties.put("document", doc.getId());
                propertiesList.add(properties);
            } catch (ClientException e) {
                LOG.error("Document '" + document + "' is not found.", e);
            }
        }

        // Add properties to param
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("properties", propertiesList);
        params.put("chainId", "Blob.Attach");

        // Execute batch operation
        BatchResult<Blob> result = (BatchResult) AthentoOperationsHelper
                .runOperation(RunOperationAsBatch.ID, blob, params, session);

        return new BlobList(result.getItems());
    }

}
