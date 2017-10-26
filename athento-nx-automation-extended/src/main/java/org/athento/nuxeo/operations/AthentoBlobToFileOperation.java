package org.athento.nuxeo.operations;

import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;

import java.io.File;
import java.io.IOException;

/**
 * Athento Blob to File using a filename parameter.
 */
@Operation(id = AthentoBlobToFileOperation.ID, category = "Athento", label = "Export to File", description = "Save blob into file.")
public class AthentoBlobToFileOperation {

    public static final String ID = "Athento.BlobToFile";

    @Param(name = "directory")
    protected String directory;

    @Param(name = "filename", required = false)
    protected String filename;

    protected File root;

    protected void init() {
        root = new File(directory);
        root.mkdirs();
    }

    protected File getFile(String name) {
        return new File(root, name);
    }

    protected void writeFile(Blob blob) throws IOException {
        String name = blob.getFilename();
        if (name.length() == 0) {
            name = "blob#" + Integer.toHexString(System.identityHashCode(blob));
        }
        if (filename == null) {
            filename = name;
        }
        File file = getFile(filename);
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        blob.transferTo(tmp);
        tmp.renameTo(file);
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob blob) throws Exception {
        init();
        writeFile(blob);
        return blob;
    }

}
