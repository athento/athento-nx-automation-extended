package org.athento.nuxeo.operations;


import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * Fetch or create document.
 * 
 * @author victorsanchez
 * 
 */
@Operation(id = DocumentFetchOrCreateOperation.ID, category = "Athento", label = "Fetch or Create Document", description = "Fetch or create a document from the repository given its reference (path or UID). The document will become the input of the next operation.")
public class DocumentFetchOrCreateOperation extends AbstractAthentoOperation {

	public static final String ID = "Athento.Document.FetchOrCreate";

	@Context
	protected CoreSession session;

	/** Operation context. */
	@Context
	protected OperationContext ctx;

	@Param(name = "type")
	protected String type;

	@Param(name = "name")
	protected String name;

	@Param(name = "properties", required = false)
	protected Properties content;
	
	@Param(name = "path", required = true)
	protected String path;

	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws Exception {
		// Check access
		checkAllowedAccess(ctx);
		try {
			return session.getDocument(new PathRef(path));
		} catch (ClientException e) {
			if (name == null) {
				name = "Untitled";
			}
			DocumentModel newDoc = session.createDocumentModel(
					doc.getPathAsString(), name, type);
			if (content != null) {
				DocumentHelper.setProperties(session, newDoc, content);
			}
			return session.createDocument(newDoc);
		}
	}

}