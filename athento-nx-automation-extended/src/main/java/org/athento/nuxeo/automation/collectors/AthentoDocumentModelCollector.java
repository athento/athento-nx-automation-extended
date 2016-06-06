package org.athento.nuxeo.automation.collectors;

import java.util.ArrayList;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.OutputCollector;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

public class AthentoDocumentModelCollector extends ArrayList<DocumentModel>
		implements DocumentModelList,
		OutputCollector<DocumentModel, DocumentModelList> {

	private static final long serialVersionUID = -1388721290125584993L;

	@Override
	public long totalSize() {
		return size();
	}

	@Override
	public void collect(OperationContext ctx, DocumentModel obj)
			throws OperationException {
		if (obj != null) {
			add(obj);
		}
	}

	@Override
	public DocumentModelList getOutput() {
		return this;
	}
}