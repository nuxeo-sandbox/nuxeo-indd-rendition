package org.nuxeo.labs.indd.rendition.converter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.cache.SimpleCachableBlobHolder;
import org.nuxeo.ecm.core.convert.extension.Converter;
import org.nuxeo.ecm.core.convert.extension.ConverterDescriptor;
import org.nuxeo.labs.indd.rendition.service.InDesignRenditionService;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.Map;

public class InDesign2PdfConverter implements Converter {

    @Override
    public void init(ConverterDescriptor converterDescriptor) {
        //nothing to do
    }

    @Override
    public BlobHolder convert(BlobHolder blobHolder, Map<String, Serializable> map) throws ConversionException {
        Blob blob = blobHolder.getBlob();
        InDesignRenditionService inDesignRenditionService = Framework.getService(InDesignRenditionService.class);
        Blob pdf = inDesignRenditionService.generatePdf(blob);
        return pdf != null ? new SimpleCachableBlobHolder(pdf) : null;
    }
}
