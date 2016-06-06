/**
 * 
 */
package org.athento.nuxeo.operations;


import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

import java.util.*;

/**
 * @author athento
 *
 */
@Operation(id = GetPreviewIFrameOperation.ID, category = "Athento", label = "Get iframe for document preview", description = "Return an HTML code into iframe component to show a document preview.")
public class GetPreviewIFrameOperation {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(GetPreviewIFrameOperation.class);

    /** Operation ID. */
	public static final String ID = "Athento.Preview";

    /** Base url. */
    @Param(name = "url", required = true)
    protected String baseUrl;

    /** Xpath of preivew content. */
    @Param(name = "xpath", required = false)
    protected String xpath;

    /** Width in pixels. */
    @Param(name = "width", required = false)
    protected Integer width;

    /** Height in pixels. */
    @Param(name = "height", required = false)
    protected Integer height;

    /**
     * Operation method.
     *
     * @return
     * @throws Exception
     */
	@OperationMethod
	public String run(DocumentModel doc) throws Exception {

        // Generate token
        String token = generatePreviewToken(doc);

        // Check dimensions
        if (width == null) {
            width = 938;
        }
        if (height == null) {
            height = 900;
        }

        // Check xpath of preview content
        if (xpath == null) {
            xpath = "file:content";
        }

        // Make iframe
        String iframe = String.format("<iframe style=\"border: 1px solid #777; height:%dpx; width:%dpx\" src=\"%s/nuxeo/restAPI/athpreview/default/%s/%s/?token=%s\"></iframe>",
                height, width, baseUrl, doc.getId(), xpath, token);

        return iframe;
	}

    /**
     * Generate a simple preview token based on dublincore:modified metadata.
     *
     * @param doc document
     * @return token
     */
    private String generatePreviewToken(DocumentModel doc) {
        // Encoding token
        return Base64.encodeBase64String(String.format("%s#control", doc.getChangeToken()).getBytes());
    }

}