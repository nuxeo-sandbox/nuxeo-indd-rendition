package org.nuxeo.labs.indd.rendition.listener;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.indd.rendition.worker.InddPreviewWorker;
import org.nuxeo.runtime.api.Framework;

public class InddPreviewGeneration implements EventListener {

    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        if (!"File".equals(doc.getType())) {
            return;
        }

        boolean fileIsDirty = doc.getProperty("file:content").isDirty();

        Blob blob = (Blob) doc.getPropertyValue("file:content");

        boolean isIndd = blob != null && blob.getFilename().toLowerCase().endsWith(".indd");

        boolean isNotVersion = !doc.isVersion() && !doc.isProxy();

        if (isNotVersion && isIndd && (event.getName().equals("documentCreated") || fileIsDirty)) {
            InddPreviewWorker work = new InddPreviewWorker(doc.getRepositoryName(), doc.getId(), "file:content");
            WorkManager workManager = Framework.getService(WorkManager.class);
            workManager.schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED, true);
        }
    }
}
