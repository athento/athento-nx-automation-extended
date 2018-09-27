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
import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.operations.blob.AttachBlob;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Override Blob.Attach filling file:filename metadata.
 */
@Operation(id = AttachBlob.ID, category = Constants.CAT_BLOB, label = "Attach File", description = "Attach the input file to the document given as a parameter. If the xpath points to a blob list then the blob is appended to the list, otherwise the xpath should point to a blob property. If the save parameter is set the document modification will be automatically saved. Return the blob.")
public class AthentoAttachBlobOperation extends AbstractAthentoOperation {

    /** Log. */
    private static final Log LOG = LogFactory.getLog(AthentoAttachBlobOperation.class);

    public static final String ID = "Blob.Attach";

    private static final String FILENAME_FIELD = "file:filename";
    private static final String CONTENT_FIELD = "file:content";

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "xpath", required = false, values = FILENAME_FIELD)
    protected String xpath = CONTENT_FIELD;

    @Param(name = "document")
    protected DocumentModel doc;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob blob) throws Exception {
        // Check access
        checkAllowedAccess(ctx);
        DocumentHelper.addBlob(doc.getProperty(xpath), blob);
        if (save) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attaching Blob " + blob.getFilename() + " into " + doc.getId());
            }
            if (FILENAME_FIELD.equals(xpath)) {
                doc.setPropertyValue(FILENAME_FIELD, blob.getFilename());
            }
            doc = session.saveDocument(doc);
        }
        return blob;
    }

}
