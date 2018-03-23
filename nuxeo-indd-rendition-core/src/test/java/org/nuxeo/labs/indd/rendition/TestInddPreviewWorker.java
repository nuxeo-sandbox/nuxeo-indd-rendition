package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.indd.rendition.worker.InddPreviewWorker;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert",
        "nuxeo-indd-compound-asset-core",
        "org.nuxeo.ecm.platform.thumbnail"})
public class TestInddPreviewWorker {

    @Inject
    CoreSession session;

    @Test
    public void TestWorker() {
        File file = new File(getClass().getResource("/files/test.indd").getPath());
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"file","File");
        doc.setPropertyValue("file:content",new FileBlob(file));
        doc = session.createDocument(doc);

        InddPreviewWorker worker = new InddPreviewWorker(doc.getRepositoryName(),doc.getId(),"file:content");
        worker.work();

        TransactionHelper.startTransaction();

        doc = session.getDocument(doc.getRef());

        Assert.assertTrue(doc.hasFacet("Compound"));
        Assert.assertTrue(doc.hasFacet("Thumbnail"));
        Assert.assertNotNull(doc.getPropertyValue("compound:renditions"));
        Assert.assertNotNull(doc.getPropertyValue("thumb:thumbnail"));
    }
}
