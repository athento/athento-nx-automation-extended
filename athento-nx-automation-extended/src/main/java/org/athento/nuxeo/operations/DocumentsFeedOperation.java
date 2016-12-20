package org.athento.nuxeo.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.athento.utils.DocumentFunctions;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.rendering.RenderingService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.services.resource.ResourceService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Documents Feed operation.
 *
 * @author victorsanchez
 */
@Operation(id = DocumentsFeedOperation.ID, category = "Athento", label = "Documents feed", description = "Documents feed")
public class DocumentsFeedOperation {

    /**
     * Log.
     */
    private static final Log LOG = LogFactory.getLog(DocumentsFeedOperation.class);

    @Context
    protected ResourceService rs;

    @Context
    protected OperationContext ctx;

    /**
     * Operation ID.
     */
    public static final String ID = "Athento.DocumentsFeedOperation";

    @Param(name = "template", widget = Constants.W_TEMPLATE_RESOURCE)
    protected String template;

    @Param(name = "filename", required = false, values = "output.ftl")
    protected String name = "output.ftl";

    @Param(name = "type", widget = Constants.W_OPTION, required = false, values = { "ftl", "mvel" })
    protected String type = "ftl";

    @Param(name = "mimetype", required = false, values = "text/xml")
    protected String mimeType = "text/xml";

    @Param(name = "charset", required = false)
    protected String charset = "UTF-8";

    /** Headers to feed. */
    @Param(name = "headers", required = false)
    protected String headers;

    /** Columns to feed are the document metadata. */
    @Param(name = "columns", required = false)
    protected String columns;

    /** Separator. */
    @Param(name = "separator", required = false, values = ",")
    protected String separator = ",";

    /**
     * Operation method.
     *
     * @param docs
     * @return feed blob
     * @throws Exception on error
     */
    @OperationMethod
    public Blob run(DocumentModelList docs) throws Exception {
        // Add selected headers and columns
        ctx.put("headers", getSplittedString(headers, false));
        ctx.put("columns", getSplittedString(columns, false));
        // Set function util
        ctx.put("Func", new DocumentFunctions());
        // Separator
        ctx.put("separator", separator);
        // Render template
        String content = RenderingService.getInstance().render(type, template, ctx);
        StringBlob blob = new StringBlob(content);
        blob.setFilename(name);
        blob.setMimeType(mimeType);
        blob.setEncoding(charset);
        return blob;
    }

    /**
     * Spplit string to list.
     *
     * @param headers
     * @return
     */
    private List<String> getSplittedString(String headers, boolean separated) {
        if (headers != null) {
            List<String> result = Arrays.asList(headers.split(","));
            for (ListIterator<String> it = result.listIterator(); it.hasNext();) {
                String res = it.next();
                if (separated && it.hasNext()) {
                    it.set(res + ",");
                }
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

}