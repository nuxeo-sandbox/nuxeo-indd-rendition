package org.nuxeo.labs.indd.rendition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.*;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.pdf.PDFMerge;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class InddPreviewHelper {

    public static final String COMMAND_NAME = "inddpagepreview";
    public static final String INPUT_FILE_PATH_PARAMETER = "inputFilePath";


    public static void setThumbnailAndPreview(DocumentModel doc) {

        List<Blob> pages = getPagesAsImages((Blob) doc.getPropertyValue("file:content"));

        if (pages.size()<= 0) {
            return;
        }

        if (!doc.hasFacet("Thumbnail")) {
            doc.addFacet("Thumbnail");
            doc.setPropertyValue("thumb:thumbnail", (Serializable) pages.get(0));
        }

        if (!doc.hasFacet("Compound")) {
            doc.addFacet("Compound");
            Blob pdf = InddPreviewHelper.generatePdf(pages);
            List<Blob> renditions = new ArrayList<>();
            renditions.add(pdf);
            doc.setPropertyValue("compound:renditions", (Serializable) renditions);
        }
    }


    public static List<Blob> getPagesAsImages(Blob blob) {
        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        try {
            CloseableFile closeable = blob.getCloseableFile("." + FilenameUtils.getExtension(blob.getFilename()));
            CmdParameters params = cles.getDefaultCmdParameters();
            params.addNamedParameter(INPUT_FILE_PATH_PARAMETER, closeable.getFile());
            ExecResult result = Framework.getService(CommandLineExecutorService.class).execCommand(COMMAND_NAME, params);
            if (!result.isSuccessful()) {
                throw result.getError();
            }
            StringBuilder sb = new StringBuilder();
            for (String line : result.getOutput()) {
                sb.append(line);
            }
            String jsonOutput = sb.toString();
            ObjectMapper jacksonMapper = new ObjectMapper();
            List<Map<String, Object>> resultList = jacksonMapper.readValue(jsonOutput,
                    new TypeReference<List<HashMap<String, Object>>>() {
                    });
            Map<String, Object> resultMap = resultList.get(0);

            Object pageImage = resultMap.get("XMP:PageImage");

            if (pageImage== null || !(pageImage instanceof List)) {
                return new ArrayList<>();
            }

            List<String> pagesbase64 = (List)pageImage;

            List<Blob> pagesJpeg = new ArrayList<>();

            for(String pageBase64 : pagesbase64) {
                if (pageBase64!=null && pageBase64.startsWith("base64:")) {
                    pageBase64 = pageBase64.substring(7);
                }
                byte[] pageByte = Base64.getDecoder().decode(pageBase64);
                File tmp = Framework.createTempFile("nxindd",null);
                FileUtils.writeByteArrayToFile(tmp, pageByte);
                pagesJpeg.add(new FileBlob(tmp,"image/jpeg"));
            }

            return pagesJpeg;

        } catch (Exception e) {
            throw new NuxeoException(e);
        }
    }

    public static Blob generatePdf(List<Blob> jpegs) {
        ConversionService conversionService = Framework.getService(ConversionService.class);
        List<Blob> pdfPage = new ArrayList<>();
        Blob pdf = null;

        for(Blob jpeg: jpegs) {
            BlobHolder result = conversionService.convert("image2pdf", new SimpleBlobHolder(jpeg), new HashMap<>());
            pdfPage.add(result.getBlob());
        }

        PDFMerge merger = new PDFMerge(new BlobList(pdfPage));

        try {
            return merger.merge("preview.pdf");
        } catch (COSVisitorException | IOException e) {
            throw new NuxeoException(e);
        }
    }


}
