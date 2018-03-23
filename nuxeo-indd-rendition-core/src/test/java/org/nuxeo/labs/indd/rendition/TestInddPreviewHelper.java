package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert"})
public class TestInddPreviewHelper {

    @Inject
    CoreSession session;

    @Test
    public void TestHelper() {
        File file = new File(getClass().getResource("/files/test.indd").getPath());
        List<Blob> pages = InddPreviewHelper.getPagesAsImages(new FileBlob(file));
        Assert.assertEquals(2, pages.size());
        Blob pdf = InddPreviewHelper.generatePdf(pages);
        Assert.assertNotNull(pdf);
    }
}
