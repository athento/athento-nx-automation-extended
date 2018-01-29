package org.athento.nuxeo;

import com.google.inject.Inject;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.athento.nuxeo.operations.DocumentsFeedOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.FetchContextDocument;
import org.nuxeo.ecm.automation.core.rendering.Renderer;
import org.nuxeo.ecm.automation.core.rendering.operations.RenderDocument;
import org.nuxeo.ecm.automation.core.rendering.operations.RenderDocumentFeed;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.resource.ResourceService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.theme.models.Feed;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Feed operation test.
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.automation.core")
@LocalDeploy("org.athento.nuxeo.automation-extended:OSGI-INF/Automation/operations-contrib.xml")
public class FeedOperationTest {

    private static final Log LOG = LogFactory.getLog(FeedOperationTest.class);

    protected DocumentModel src;

    protected DocumentModel dst;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        src = session.createDocumentModel("/", "file1", "File");
        src.setPropertyValue("dc:title", "Document 1");
        src.setPropertyValue("dc:description", "Description 1");
        src = session.createDocument(src);
        session.save();
        src = session.getDocument(src.getRef());

        dst = session.createDocumentModel("/", "file2", "File");
        dst.setPropertyValue("dc:title", "Document 2");
        dst.setPropertyValue("dc:description", "Description 2");
        dst = session.createDocument(dst);
        session.save();
        dst = session.getDocument(dst.getRef());

    }

    @Test
    public void testDocumentsFeed() throws Exception {
        URL url = getClass().getClassLoader().getResource("csv-test.ftl");
        Framework.getLocalService(ResourceService.class).addResource("csv-test.ftl", url);

        DocumentModelList list = new DocumentModelListImpl();
        list.add(src);
        list.add(dst);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(list);
        OperationChain chain = new OperationChain("testDocumentsFeed");
        chain.add(DocumentsFeedOperation.ID).set("headers", "\"Name\",\"Description\",\"Lifecycle\"")
                .set("columns", "dc:title,dc:description,lifecycle")
                .set("template", Renderer.TEMPLATE_PREFIX + "csv-test.ftl");
        Blob blob = (Blob) service.run(ctx, chain);
        String r = blob.getString();
        System.out.println(r);
    }

}
