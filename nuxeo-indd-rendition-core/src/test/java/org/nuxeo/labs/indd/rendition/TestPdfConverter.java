package org.nuxeo.labs.indd.rendition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-indd-rendition-core",
        "org.nuxeo.ecm.platform.convert"
})
public class TestPdfConverter {

    @Inject
    ConversionService conversionService;

    @Test
    public void isLoaded() {
        assertTrue(conversionService.getRegistredConverters().contains("InddToPDF"));
    }

    @Test
    public void testConverterWithOnePage() {
        File file = new File(getClass().getResource("/files/test_1_page.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        Map<String, Serializable> parameters = new HashMap<>();
        BlobHolder result = conversionService.convert("InddToPDF", new SimpleBlobHolder(blob), parameters);
        Assert.assertEquals(1, result.getBlobs().size());
    }

    @Test
    public void testConverterWithNoPreview() {
        File file = new File(getClass().getResource("/files/test_no_preview.indd").getPath());
        Blob blob = new FileBlob(file,"application/x-indesign");
        Map<String, Serializable> parameters = new HashMap<>();
        BlobHolder result = conversionService.convert("InddToPDF", new SimpleBlobHolder(blob), parameters);
        Assert.assertNull(result);
    }

}