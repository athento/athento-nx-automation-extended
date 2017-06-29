package org.athento.nuxeo.operations;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * Generate a public URL to download a document content.
 *
 * @author victorsanchez
 *
 */
@Operation(id = GetPublicURLOperation.ID, category = "Athento", label = "Get public URL to download", description = "Return a public URL to download a document content")
public class GetPublicURLOperation {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(GetPublicURLOperation.class);

    /** Operation ID. */
	public static final String ID = "Athento.GetPublicURL";

    @Context
    protected CoreSession session;

    @Param(name = "document", description = "It is ")
    protected String document;

    /**
     * Operation method.
     *
     * @return
     * @throws Exception
     */
	@OperationMethod
	public String run() throws Exception {
	    DocumentModel doc;
	    if (document.startsWith("/")) {
	        doc = session.getDocument(new PathRef(document));
        } else {
            doc = session.getDocument(new IdRef(document));
        }
        return run(doc);
	}

    /**
     * Operation method.
     *
     * @return
     * @throws Exception
     */
    @OperationMethod
    public String run(DocumentModel doc) throws Exception {
        // Generate token
        String token = generateDocumentToken(doc);
        // It is INCOMPLETE:
        return "";
    }

    /** COMPLETE!. */
    private String generateDocumentToken(DocumentModel doc) {
	    return "";
    }


}