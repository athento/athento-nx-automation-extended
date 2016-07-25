package org.athento.nuxeo.operations;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = ExtractLinesOperation.ID, category = "Athento", label = "Extract lines from blob", description = "Extract a list of lines from a blob")
public class ExtractLinesOperation {

    public static final String ID = "Athento.ExtractLines";

    @Param(name = "ignoreEmpty", required = false)
    protected boolean ignoreEmpty = true;

    @Param(name = "mimetype", required = false)
    protected String mimetype = "text/plain";

    @Param(name = "encoding", required = false)
    protected String encoding = "UTF-8";

    @Param(name = "offset", required = false)
    protected int offset = 0;

    /**
     * Run method.
     *
     * @return
     */
    @OperationMethod
    public BlobList run(Blob blob) throws Exception {
        BlobList result = new BlobList();
        BufferedReader reader = null;
        int i = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(blob.getStream()));
            String line = "";
            do {
                if (i > offset) {
                    if (!ignoreEmpty || !line.isEmpty()) {
                        result.add(new StringBlob(line, mimetype, encoding));
                    }
                }
                i++;
            } while ((line = reader.readLine()) != null);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return result;
    }


}
