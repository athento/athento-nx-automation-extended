package org.athento.nuxeo;

import com.google.inject.Inject;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.CopyFileOperation;
import org.athento.nuxeo.operations.ExtractLinesOperation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import java.io.File;
import java.net.URL;

/**
 * Feed operation test.
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.automation.core")
@LocalDeploy("org.athento.nuxeo.automation-extended:OSGI-INF/Automation/operations-contrib.xml")
public class ExtractLinesOperationTest {

    private static final Log LOG = LogFactory.getLog(ExtractLinesOperationTest.class);

    protected DocumentModel src;

    protected DocumentModel dst;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Test
    public void testExtractLines() throws Exception {
        URL url = getClass().getClassLoader().getResource("sample.csv");
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(new FileBlob(new File(url.toURI())));
        OperationChain chain = new OperationChain("testExtractLinesChain");
        chain.add(ExtractLinesOperation.ID);
        BlobList list = (BlobList) service.run(ctx, chain);
        Assert.assertTrue(list.size() == 11318);
    }

}
