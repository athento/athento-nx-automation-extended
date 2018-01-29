package org.athento.nuxeo;

import com.google.inject.Inject;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.CopyFileOperation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import java.io.File;
import java.net.URL;

/**
 * Copy file operation test.
 */
@RunWith(FeaturesRunner.class)
@Deploy("org.nuxeo.ecm.automation.core")
@LocalDeploy("org.athento.nuxeo.automation-extended:OSGI-INF/Automation/operations-contrib.xml")
public class CopyFileOperationTest {

    private static final Log LOG = LogFactory.getLog(CopyFileOperationTest.class);

    protected DocumentModel src;

    protected DocumentModel dst;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Test
    public void testCopyFile() throws Exception {
        URL url = getClass().getClassLoader().getResource("test.pdf");
        File source = File.createTempFile("file-source", ".pdf");
        File f = new File(url.toURI());
        File destiny = new File("/tmp/destiny.pdf");
        try {
            FileUtils.copy(f, source);
            OperationContext ctx = new OperationContext(session);
            OperationChain chain = new OperationChain("testCopyFileChain");
            chain.add(CopyFileOperation.ID).set("sourceFile", source.getAbsolutePath()).set("destinyFile", "/tmp/destiny.pdf").set("overwrite", true);
            service.run(ctx, chain);
            Assert.assertTrue(destiny.exists());
            Assert.assertEquals(new FileBlob(f).getLength(), new FileBlob(destiny).getLength());
        } finally {
            source.delete();
            destiny.delete();
        }
    }

}
