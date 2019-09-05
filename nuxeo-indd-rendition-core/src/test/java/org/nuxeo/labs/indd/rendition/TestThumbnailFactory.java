package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert",
        "org.nuxeo.ecm.platform.thumbnail"
})
public class TestThumbnailFactory {

    @Inject
    ThumbnailService thumbnailService;

    @Inject
    CoreSession session;

    @Test
    public void testFactoryWithOnePage() {
        File file = new File(getClass().getResource("/files/test_1_page.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "indd", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        Blob thumbnail = thumbnailService.computeThumbnail(doc, session);
        Assert.assertNotNull(thumbnail);
    }

    @Test
    public void testFactoryWithNoPage() {
        File file = new File(getClass().getResource("/files/test_no_preview.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "inddnopreview", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        Blob thumbnail = thumbnailService.computeThumbnail(doc, session);
        Assert.assertNull(thumbnail);
    }
}
