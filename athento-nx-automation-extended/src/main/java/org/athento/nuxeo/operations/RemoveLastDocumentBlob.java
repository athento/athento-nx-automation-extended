package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.util.List;

/**
 * Remove last blob.
 */
@Operation(id = RemoveLastDocumentBlob.ID, category = Constants.CAT_BLOB, label = "Remove last file", description = "Remove last blob", aliases = { "Blob.RemoveLast" })
public class RemoveLastDocumentBlob {

    public static final String ID = "Blob.RemoveLastBlob";

    /** Log. */
    private static final Log LOG = LogFactory.getLog(RemoveLastDocumentBlob.class);

    @Context
    protected CoreSession session;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) {
        List files = (List) doc.getPropertyValue("files:files");
        LOG.info("Files: " + files.size());
        if (files.size() > 0) {
            DocumentHelper.removeProperty(doc, "files/" + (files.size() - 2) + "/file");
            doc = session.saveDocument(doc);
        }
        return doc;
    }

}
