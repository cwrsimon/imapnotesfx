package de.wesim.imapnotes.mainview.services;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Scope("prototype")
public class Send2LuceneTask extends AbstractNoteTask<Void> {

    @Autowired
    private LuceneService luceneService;
    
    @Autowired
    private OutlinerWidget outlinerWidget;

    
    private final Note note;

    public Send2LuceneTask(Note note) {
        super();
        this.note = note;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }

    @Override
    public String getActionName() {
        return "Update Lucene index";
    }

    @Override
    public String getSuccessMessage() {
        // TODO
        return i18N.getMessageAndTranslation("user_message_finished_saving",
                this.note.getSubject());
    }

    @Override
    public String getRunningMessage() {
        // TODO
        return i18N.getMessageAndTranslation("user_message_start_saving",
                this.note.getSubject());
    }

    @Override
    protected Void call() throws Exception {
                     // TODO Lieber woanders hin
        var path = OutlinerWidget.determinePath(this.note, this.outlinerWidget.getRoot(), "");
        getLogger().info("path: {}", path);
//        String path = mainViewController.getBackend().getPathForNote(this.note);
        String account = mainViewController.getCurrentAccount();
        this.luceneService.indexNote(this.note, account, path);
        
        return null;
    }
}
