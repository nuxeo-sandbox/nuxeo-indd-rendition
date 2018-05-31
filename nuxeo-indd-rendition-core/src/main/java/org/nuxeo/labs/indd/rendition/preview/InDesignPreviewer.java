package org.nuxeo.labs.indd.rendition.preview;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.preview.adapter.AbstractPreviewer;
import org.nuxeo.ecm.platform.preview.adapter.MimeTypePreviewer;
import org.nuxeo.ecm.platform.preview.adapter.PdfPreviewer;
import org.nuxeo.runtime.api.Framework;

import java.util.ArrayList;
import java.util.List;

public class InDesignPreviewer extends AbstractPreviewer implements MimeTypePreviewer {

    public InDesignPreviewer() {

    }

    public List<Blob> getPreview(Blob blob, DocumentModel dm) {
        ConversionService conversionService = Framework.getService(ConversionService.class);
        BlobHolder result = conversionService.convert("InddToPDF", new SimpleBlobHolder(blob), null);

        if (result != null) {
            PdfPreviewer pdfPreviewer = new PdfPreviewer();
            return pdfPreviewer.getPreview(result.getBlob(), dm);
        } else {
            return new ArrayList<>();
        }
    }
}