/**
 * 
 */
package org.athento.nuxeo.operations;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author athento
 *
 */
@Operation(id = VocabularyGetChildrenOperation.ID, category = "Athento", label = "Vocabulary Get Children", description = "Returns a list of children vocabularies of specified vocabulary by param 'name'")
public class VocabularyGetChildrenOperation {

	public static final String ID = "Athento.VocabularyGetChildrenOperation";

	@Context
	protected OperationContext ctx;

	@Param(name = "expanded", widget = Constants.W_CHECK, required=false)
	protected Boolean expanded = false;
	@Param(name = "languageId", widget = Constants.W_TEXT, required=false)
	protected String languageId;
	@Param(name = "parentValue", widget = Constants.W_TEXT)
	protected String parentValue;
	@Param(name = "vocabularyName", widget = Constants.W_TEXT)
	protected String vocabularyName;

	/*
	 * vocabularyName = 'country'
	 * parentValue = 'africa'
	 */
	
	@OperationMethod
	public Object run() throws Exception {
		if (_log.isDebugEnabled()) {
			_log.debug("Getting children of vocabulary: " + vocabularyName + " for value " + parentValue);
			_log.debug("    languageId: " + languageId + " expanded:" + expanded);
		}
		List<JSONObject> children = getValuesForParentValue();
		if (_log.isDebugEnabled()) {
			_log.debug("Children vocabularies of [" + vocabularyName + "]: " + children);
		}

		return children;
	}
	
	private List<JSONObject> getValuesForParentValue() {
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		Session directorySession = getDirectoryService().open(vocabularyName);

		Map queryFilter = new HashMap();
		queryFilter.put("parent", parentValue);
		if (_log.isDebugEnabled()) {
			_log.debug("Querying directory [" + vocabularyName + "] with query: " + queryFilter);
		}

		DocumentModelList queryResult = directorySession.query(queryFilter);
		Locale locale = null;
		if (languageId  != null) {
			locale = new Locale(languageId);
		}
		Iterator<DocumentModel> it = queryResult.iterator();
		while (it.hasNext()) {
			DocumentModel entry = it.next();
			String label = (String) entry.getPropertyValue("xvocabulary:label");
			if (_log.isDebugEnabled()) {
				_log.debug("- entry Id: " + entry.getId());
				_log.debug("- entry Title: " + entry.getTitle());
				_log.debug("- entry Label: " + label);
			}

			if (locale != null) {
				if (label != null) {
					label = I18NUtils.getMessageString(
						"messages", label, new String[0], locale);
				}
			}
			JSONObject val = new JSONObject();
			if (!expanded) {
				val.put(entry.getId(), label);
			} else {
				val.put("id", entry.getId());
				val.put("label", label);
			}
			if (_log.isDebugEnabled()) {
				_log.debug(">> adding value: " + val);
			}
			result.add(val);
		}
		directorySession.close();

		return result;
	}

//	private List<Directory> getChildren(String name2) {
//		List<String> directories = getDirectoryService().getDirectoryNames();
//
//		List<Directory> children = new ArrayList<Directory>();
//		for (String dir: directories) {
//			String parentDir = getDirectoryService().getParentDirectoryName(dir);
//
//			if (_log.isDebugEnabled()) {
//				_log.debug("directory: " + dir + " parent: " + parentDir + " parentId: -");
//			}
//			if (name.equals(parentDir)) {
//				_log.debug(" >> adding child: " + dir);
//				children.add(getDirectoryService().getDirectory(dir));
//			}
//		}
//		return children;
//	}

	private DirectoryService getDirectoryService() {
		if (directoryService == null) {
			directoryService = Framework.getService(DirectoryService.class);
		}
		return directoryService;
	}
	
	private DirectoryService directoryService;

	
	private static final Log _log = LogFactory.getLog(VocabularyGetChildrenOperation.class);

}