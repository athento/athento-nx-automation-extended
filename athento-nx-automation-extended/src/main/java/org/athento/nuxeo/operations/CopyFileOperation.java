package org.athento.nuxeo.operations;

import org.athento.nuxeo.operations.security.AbstractAthentoOperation;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;

import java.io.File;

/**
 * Created by victorsanchez on 19/7/16.
 */
@Operation(id = CopyFileOperation.ID, category = "Athento", label = "Copy a file", description = "Copy a file to destiny")
public class CopyFileOperation extends AbstractAthentoOperation {

    public static final String ID = "Athento.CopyFile";

    /** Operation context. */
    @Context
    protected OperationContext ctx;

    @Param(name = "sourceFile")
    protected String sourceFile = "";

    @Param(name = "destinyFile")
    protected String destinyFile;

    @Param(name = "overwrite", required = false)
    protected boolean overwrite;

    /**
     * Run method.
     *
     * @return
     */
    @OperationMethod
    public void run() throws Exception {

        // Check access
        checkAllowedAccess(ctx);

        // Get source file
        File source = new File(sourceFile);
        if (!source.exists()) {
            throw new Exception("Source file is not found");
        }

        File destiny = new File(destinyFile);
        if (destiny.exists() && overwrite) {
            destiny.delete();
        }
        if (!destiny.createNewFile()) {
            throw new Exception("Destiny file can not be created!");
        }

        // Copy file
        FileUtils.copy(source, destiny);

    }


}
