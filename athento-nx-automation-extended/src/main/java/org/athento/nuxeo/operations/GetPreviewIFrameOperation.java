package org.athento.nuxeo.operations;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 *
 * Generate iframe with Athento Preview.
 *
 * @author victorsanchez
 *
 */
@Operation(id = GetPreviewIFrameOperation.ID, category = "Athento", label = "Get iframe for document preview", description = "Return an HTML code into iframe component to show a document preview.")
public class GetPreviewIFrameOperation {

	/** Log. */
	private static final Log LOG = LogFactory.getLog(GetPreviewIFrameOperation.class);

    /** Operation ID. */
	public static final String ID = "Athento.Preview";

    /** Default sizes. */
    private static final int DEFAULT_WIDTH = 938;
    private static final int DEFAULT_HEIGHT = 902;

    /** Default styles. */
    private static final String DEFAULT_STYLE = "border: 1px solid #999; ";


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

    /** Style. */
    @Param(name = "style", required = false)
    protected String style = DEFAULT_STYLE;

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
            width = DEFAULT_WIDTH;
        }
        if (height == null) {
            height = DEFAULT_HEIGHT;
        }

        // Check xpath of preview content
        if (xpath == null) {
            xpath = "file:content";
        }

        // Make iframe
        String iframe = String.format("<iframe style=\"%s; height:%dpx; width:%dpx\" src=\"%s/nuxeo/restAPI/athpreview/default/%s/%s/?token=%s\"></iframe>",
                style, height, width, baseUrl, doc.getId(), xpath, token);

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