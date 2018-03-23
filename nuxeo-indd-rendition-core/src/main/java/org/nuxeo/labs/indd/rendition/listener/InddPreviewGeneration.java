package org.nuxeo.labs.indd.rendition.listener;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.indd.rendition.worker.InddPreviewWorker;
import org.nuxeo.runtime.api.Framework;

import java.util.Arrays;
import java.util.List;

public class InddPreviewGeneration implements PostCommitFilteringEventListener {
  
  protected final List<String> handled = Arrays.asList("aboutToCreate", "beforeDocumentModification");

    @Override
    public void handleEvent(EventBundle events) {
        for (Event event : events) {
            if (acceptEvent(event)) {
                handleEvent(event);
            }
        }
    }

    @Override
    public boolean acceptEvent(Event event) {
        return handled.contains(event.getName());
    }

    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        boolean fileIsDirty = doc.getProperty("file:content").isDirty();

        Blob blob = (Blob) doc.getPropertyValue("file:content");

        boolean isIndd = blob!= null && blob.getFilename().toLowerCase().endsWith(".indd");

        if ("File".equals(doc.getType()) && fileIsDirty && isIndd) {
            InddPreviewWorker work = new InddPreviewWorker(doc.getRepositoryName(), doc.getId(), "file:content");
            WorkManager workManager = Framework.getService(WorkManager.class);
            workManager.schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED, true);
        }
    }
}
