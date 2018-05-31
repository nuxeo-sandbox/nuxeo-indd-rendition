package org.nuxeo.labs.indd.rendition;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.indd.rendition.service.InDesignRenditionService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert"
})
public class TestInDesignRenditionService {

    @Inject
    protected InDesignRenditionService indesignrenditionservice;

    @Test
    public void testService() {
        assertNotNull(indesignrenditionservice);
    }

    @Test
    public void TestWithOnePage() {
        File file = new File(getClass().getResource("/files/test_1_page.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        List<Blob> pages = indesignrenditionservice.getPagesAsImages(blob);
        Assert.assertEquals(1, pages.size());
        Blob pdf = indesignrenditionservice.generatePdf(blob);
        Assert.assertNotNull(pdf);
    }


    @Test
    public void TestWithMultiPage() {
        File file = new File(getClass().getResource("/files/test_multi_page.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        List<Blob> pages = indesignrenditionservice.getPagesAsImages(blob);
        Assert.assertEquals(2, pages.size());
        Blob pdf = indesignrenditionservice.generatePdf(blob);
        Assert.assertNotNull(pdf);
    }

    @Test
    public void TestWithNoPreview() {
        File file = new File(getClass().getResource("/files/test_no_preview.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        List<Blob> pages = indesignrenditionservice.getPagesAsImages(blob);
        Assert.assertEquals(0, pages.size());
        Blob pdf = indesignrenditionservice.generatePdf(blob);
        Assert.assertNull(pdf);
    }

}
