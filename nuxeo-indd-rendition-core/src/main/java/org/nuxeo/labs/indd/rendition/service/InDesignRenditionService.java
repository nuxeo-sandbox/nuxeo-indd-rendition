package org.nuxeo.labs.indd.rendition.service;

import org.nuxeo.ecm.core.api.Blob;

import java.util.List;

public interface InDesignRenditionService {

    List<Blob> getPagesAsImages(Blob blob);

    Blob generatePdf(Blob blob);

}
