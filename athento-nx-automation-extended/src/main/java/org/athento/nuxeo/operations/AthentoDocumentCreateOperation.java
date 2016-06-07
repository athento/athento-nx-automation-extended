/**
 * 
 */
package org.athento.nuxeo.operations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.utils.AthentoOperationsHelper;
import org.athento.utils.StringUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * @author athento
 *
 */
@Operation(id = AthentoDocumentCreateOperation.ID, category = "Athento", label = "Athento Document Create", description = "Creates a document in Athento's way")
public class AthentoDocumentCreateOperation {

	public static final String ID = "Athento.Document.Create";

	public static final String CONFIG_OPERATION_ID = "automationExtendedConfig:documentCreateOperationId";
	public static final String CONFIG_DEFAULT_DESTINATION = "automationExtendedConfig:defaultDestination";

	@Context
	protected CoreSession session;

	@Param(name = "destination", required = false)
	protected String destination;

	@Param(name = "name", required = false)
	protected String name;

	@Param(name = "properties", required = false)
	protected Properties properties;

	@Param(name = "type")
	protected String type;

	@OperationMethod()
	public DocumentModel run() throws Exception {
		Map<String, Object> config = AthentoOperationsHelper.readConfig(session);
		String parentFolderPath = String.valueOf(config
			.get(AthentoDocumentCreateOperation.CONFIG_DEFAULT_DESTINATION));
		return run(new PathRef(parentFolderPath));
	}

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(DocumentRef doc) throws Exception {
		return run(session.getDocument(doc));
	}

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(DocumentModel doc) throws Exception {
		if (_log.isDebugEnabled()) {
			_log.debug(AthentoDocumentCreateOperation.ID
					+ " BEGIN with params:");
			_log.debug(" - parentDoc: " + doc);
			_log.debug(" - type: " + type);
			_log.debug(" - name: " + name);
			_log.debug(" - properties: " + properties);
		}
		Map<String, Object> config = AthentoOperationsHelper
			.readConfig(session);
		String operationId = String.valueOf(config
			.get(AthentoDocumentCreateOperation.CONFIG_OPERATION_ID));
		String defaultPath = String.valueOf(config
			.get(AthentoDocumentCreateOperation.CONFIG_DEFAULT_DESTINATION));
		DocumentModel parentFolder = doc;
		if (StringUtils.isNullOrEmpty(destination)) {
			if (!StringUtils.isNullOrEmpty(operationId)) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("basePath", defaultPath);
				params.put("name", name);
				params.put("properties", properties);
				params.put("type", type);
				Object input = null;
				parentFolder = (DocumentModel)AthentoOperationsHelper.runOperation(
					operationId, input , params, session);
				parentFolder = (DocumentModel) parentFolder;
				if (AthentoOperationsHelper.DOCUMENT_TYPE_ATHENTO_EXCEPTION.equals(
					parentFolder.getType())) {
					return parentFolder;
				}
			} else {
				_log.warn("No operation to get basePath and no destination set. Using default: " + defaultPath);
				parentFolder = session.getDocument(new PathRef(defaultPath)); 
			}
		} else {
			parentFolder = session.getDocument(new PathRef(destination));
		}

		if (name == null) {
			name = "Untitled";
		}
		String parentPath = parentFolder.getPathAsString();

		if (_log.isDebugEnabled()) {
			_log.debug(AthentoDocumentCreateOperation.ID
				+ " Creating document in parentPath: " + parentPath);
		}
		DocumentModel newDoc = session.createDocumentModel(parentPath, name, type);
		if (properties != null) {
			DocumentHelper.setProperties(session, newDoc, properties);
		}
		doc = session.createDocument(newDoc);
// -- END Document.Create
		if (_log.isDebugEnabled()) {
			_log.debug(AthentoDocumentCreateOperation.ID
					+ " END return value: " + doc);
		}
		return doc;
	}

	private static final Log _log = LogFactory
			.getLog(AthentoDocumentCreateOperation.class);

}