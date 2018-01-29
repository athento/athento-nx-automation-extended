package org.athento.nuxeo;

import com.google.inject.Inject;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.api.DocumentModelListPageProvider;
import org.athento.nuxeo.operations.CopyFileOperation;
import org.athento.nuxeo.operations.PackageToZipOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.ZipUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.jaxrs.io.documents.PaginableDocumentModelListImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
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
public class PackageToZipOperationTest {

    private static final Log LOG = LogFactory.getLog(PackageToZipOperationTest.class);

    protected DocumentModel src, dst, abc;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        FileBlob blob = new FileBlob(getClass().getClassLoader().getResourceAsStream("test.pdf"));
        src = session.createDocumentModel("/", "file1", "File");
        src.setPropertyValue("dc:title", "Document 1");
        src.setPropertyValue("dc:description", "Description 1");
        src.setPropertyValue("file:content", blob);
        src = session.createDocument(src);
        session.save();
        src = session.getDocument(src.getRef());

        dst = session.createDocumentModel("/", "file2", "File");
        dst.setPropertyValue("dc:title", "Document 2");
        dst.setPropertyValue("dc:description", "Description 2");
        dst.setPropertyValue("file:content", blob);
        dst = session.createDocument(dst);
        session.save();
        dst = session.getDocument(dst.getRef());

        abc = session.createDocumentModel("/", "file3", "File");
        abc.setPropertyValue("dc:title", "Document 3");
        abc.setPropertyValue("dc:description", "Description 3");
        abc.setPropertyValue("file:content", blob);
        abc = session.createDocument(abc);
        session.save();
        abc = session.getDocument(abc.getRef());

    }

    @Test
    public void testPackageToZip() throws Exception {

        DocumentModelList list = new DocumentModelListImpl();
        list.add(src);
        list.add(dst);
        list.add(abc);

        PaginableDocumentModelListImpl paginableDocumentModelList
                = new PaginableDocumentModelListImpl(new DocumentModelListPageProvider(list));

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(paginableDocumentModelList);
        OperationChain chain = new OperationChain("testPackageToZipWithSize80K");
        chain.add(PackageToZipOperation.ID).set("packageSize", 2).set("fileMaxSize", 81920); // 80Kb
        BlobList blobList = (BlobList) service.run(ctx, chain);
        Assert.assertTrue(blobList.size() == 2);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList.get(0).getStream()).size() == 1);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList.get(1).getStream()).size() == 1);

        OperationContext ctx2 = new OperationContext(session);
        ctx2.setInput(paginableDocumentModelList);
        OperationChain chain2 = new OperationChain("testPackageToZipWitSize100K");
        chain2.add(PackageToZipOperation.ID).set("packageSize", 2).set("fileMaxSize", 102400); // 100Kb
        BlobList blobList2 = (BlobList) service.run(ctx2, chain2);
        Assert.assertTrue(blobList2.size() == 2);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList2.get(0).getStream()).size() == 2);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList2.get(1).getStream()).size() == 1);

        OperationContext ctx3 = new OperationContext(session);
        ctx3.setInput(paginableDocumentModelList);
        OperationChain chain3 = new OperationChain("testPackageToZipWitNoSize");
        chain3.add(PackageToZipOperation.ID).set("packageSize", 1).set("fileMaxSize", -1); // 100Kb
        BlobList blobList3 = (BlobList) service.run(ctx3, chain3);
        Assert.assertTrue(blobList3.size() == 3);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList3.get(0).getStream()).size() == 1);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList3.get(1).getStream()).size() == 1);
        Assert.assertTrue(ZipUtils.getEntryNames(blobList3.get(2).getStream()).size() == 1);
    }

}
