package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.nuxeo.labs.indd.rendition.InddPreviewHelper.COMPOUND_DOCUMENT_FACET;
import static org.nuxeo.labs.indd.rendition.InddPreviewHelper.COMPOUND_DOCUMENT_RENDITION_PROPERTY;

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

    @Inject
    WorkManager wm;

    @Test
    public void TestWorkerWithMultiPage() throws InterruptedException {
        File file = new File(getClass().getResource("/files/test_multi_page.indd").getPath());
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"file","File");
        doc.setPropertyValue("file:content",new FileBlob(file));
        doc = session.createDocument(doc);
        TransactionHelper.commitOrRollbackTransaction();

        wm.awaitCompletion(60, TimeUnit.SECONDS);

        TransactionHelper.startTransaction();

        doc = session.getDocument(doc.getRef());

        Assert.assertTrue(doc.hasFacet(COMPOUND_DOCUMENT_FACET));
        Assert.assertTrue(doc.hasFacet("Thumbnail"));
        Assert.assertNotNull(doc.getPropertyValue(COMPOUND_DOCUMENT_RENDITION_PROPERTY));
        Assert.assertNotNull(doc.getPropertyValue("thumb:thumbnail"));
    }

    @Test
    public void TestWorkerWithNoPreview() throws InterruptedException {
        File file = new File(getClass().getResource("/files/test_no_preview.indd").getPath());
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"file","File");
        doc.setPropertyValue("file:content",new FileBlob(file));
        doc = session.createDocument(doc);
        TransactionHelper.commitOrRollbackTransaction();

        wm.awaitCompletion(60, TimeUnit.SECONDS);

        TransactionHelper.startTransaction();

        doc = session.getDocument(doc.getRef());

        Assert.assertFalse(doc.hasFacet(COMPOUND_DOCUMENT_FACET));
    }


}
