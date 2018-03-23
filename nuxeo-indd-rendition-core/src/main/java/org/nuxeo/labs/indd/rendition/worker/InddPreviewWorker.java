package org.nuxeo.labs.indd.rendition.worker;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.labs.indd.rendition.InddPreviewHelper;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class InddPreviewWorker extends AbstractWork {

    private static final long serialVersionUID = 1L;

    public InddPreviewWorker(String repositoryName, String docId, String xpath) {
        super(repositoryName + ':' + docId + ':' + xpath + ":inddView");
        setDocument(repositoryName, docId);
    }

    @Override
    public String getTitle() {
        return "indd views generation";
    }

    @Override
    public void work() {
        setProgress(Progress.PROGRESS_INDETERMINATE);
        setStatus("Extracting");

        openSystemSession();
        if (!session.exists(new IdRef(docId))) {
            setStatus("Document does not exist");
            return;
        }

        DocumentModel workingDocument = session.getDocument(new IdRef(docId));
        Blob blob = (Blob) workingDocument.getPropertyValue("file:content");
        if (blob == null) {
            // do nothing
            return;
        }

        setStatus("Running conversions");

        InddPreviewHelper.setThumbnailAndPreview(workingDocument);

        setStatus("Saving");
        session.saveDocument(workingDocument);
        TransactionHelper.commitOrRollbackTransaction();
        closeSession();

        setStatus("Done");
    }

}