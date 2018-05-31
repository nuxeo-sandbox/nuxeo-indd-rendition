package org.nuxeo.labs.indd.rendition.thumbnail;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailFactory;
import org.nuxeo.ecm.platform.thumbnail.ThumbnailConstants;
import org.nuxeo.labs.indd.rendition.service.InDesignRenditionService;
import org.nuxeo.runtime.api.Framework;

import java.util.List;

public class InddThumbnailFactory implements ThumbnailFactory {

    @Override
    public Blob getThumbnail(DocumentModel doc, CoreSession session) {
        Blob thumbnailBlob = null;
        if (doc.hasFacet(ThumbnailConstants.THUMBNAIL_FACET)) {
            thumbnailBlob = (Blob) doc.getPropertyValue(ThumbnailConstants.THUMBNAIL_PROPERTY_NAME);
        }
        return thumbnailBlob;
    }

    @Override
    public Blob computeThumbnail(DocumentModel doc, CoreSession session) {
        Blob blob = (Blob) doc.getPropertyValue("file:content");

        if (blob == null || !"application/x-indesign".equals(blob.getMimeType())) {
            return null;
        }

        InDesignRenditionService inDesignRenditionService = Framework.getService(InDesignRenditionService.class);
        List<Blob> pages = inDesignRenditionService.getPagesAsImages(blob);
        return pages.size()>0 ? pages.get(0) : null;
    }
}
