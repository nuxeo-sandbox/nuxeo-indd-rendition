package org.nuxeo.labs.indd.rendition.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.pdf.PDFMerge;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InDesignRenditionServiceImpl extends DefaultComponent implements InDesignRenditionService {

    public static final String COMMAND_NAME = "inddpagepreview";
    public static final String INPUT_FILE_PATH_PARAMETER = "inputFilePath";


    /**
     * Component activated notification.
     * Called when the component is activated. All component dependencies are resolved at that moment.
     * Use this method to initialize the component.
     *
     * @param context the component context.
     */
    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    /**
     * Component deactivated notification.
     * Called before a component is unregistered.
     * Use this method to do cleanup if any and free any resources held by the component.
     *
     * @param context the component context.
     */
    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        // Add some logic here to handle contributions
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        // Logic to do when unregistering any contribution
    }

    @Override
    public List<Blob> getPagesAsImages(Blob blob) {
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

            List<String> pagesbase64 = new ArrayList<>();

            if (pageImage== null) {
                return new ArrayList<>();
            } else if (pageImage instanceof String) {
                pagesbase64.add((String) pageImage);
            } else if (pageImage instanceof List) {
                pagesbase64 = (List<String>) pageImage;
            }

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

    @Override
    public Blob generatePdf(Blob blob) {

        List<Blob> jpegs = getPagesAsImages(blob);

        ConversionService conversionService = Framework.getService(ConversionService.class);
        List<Blob> pdfPage = new ArrayList<>();

        for(Blob jpeg: jpegs) {
            BlobHolder result = conversionService.convert("image2pdf", new SimpleBlobHolder(jpeg), new HashMap<>());
            pdfPage.add(result.getBlob());
        }

        PDFMerge merger = new PDFMerge(new BlobList(pdfPage));

        try {
            return merger.merge("preview.pdf");
        } catch (IOException e) {
            throw new NuxeoException(e);
        }
    }
}
