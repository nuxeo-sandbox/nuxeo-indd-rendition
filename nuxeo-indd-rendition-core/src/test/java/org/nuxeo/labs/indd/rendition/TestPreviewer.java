package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.preview.adapter.MimeTypePreviewer;
import org.nuxeo.ecm.platform.preview.adapter.PreviewAdapterManager;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert",
        "org.nuxeo.ecm.platform.preview"
})
public class TestPreviewer {

    @Inject
    PreviewAdapterManager previewAdapterManager;

    @Inject
    CoreSession session;

    @Test
    public void isLoaded() {
        assertNotNull(previewAdapterManager.getPreviewer("application/x-indesign"));
    }

    @Test
    public void testPreviewWithOnePage() {
        File file = new File(getClass().getResource("/files/test_1_page.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "indd", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);

        MimeTypePreviewer previewer = previewAdapterManager.getPreviewer("application/x-indesign");
        List<Blob> previews = previewer.getPreview(blob,doc);

        Assert.assertEquals(2,previews.size());
    }

    @Test
    public void testNoPreview() {
        File file = new File(getClass().getResource("/files/test_no_preview.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "indd", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);

        MimeTypePreviewer previewer = previewAdapterManager.getPreviewer("application/x-indesign");
        List<Blob> previews = previewer.getPreview(blob,doc);

        Assert.assertEquals(0,previews.size());
    }

}