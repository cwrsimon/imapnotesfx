package de.wesim.imapnotes.mainview.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.EditorTab;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Component
@Scope("prototype")
public class SaveNoteTask extends AbstractNoteTask<Void> {

    private final EditorTab editorInstance;

    @Autowired
    private ApplicationContext context;

    
    public SaveNoteTask(EditorTab editorInstance) {
        super();
        this.editorInstance = editorInstance;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Platform.runLater(() -> {
            editorInstance.getQe().setContentUpdate(false);
            var indexTask = context.getBean(Send2LuceneTask.class, this.editorInstance.getNote());
            indexTask.run();
        });

    }

    @Override
    public String getActionName() {
        return "Delete Message";
    }

    @Override
    public String getSuccessMessage() {
        return i18N.getMessageAndTranslation("user_message_finished_saving",
                this.editorInstance.getNote().getSubject());
    }

    @Override
    public String getRunningMessage() {
        return i18N.getMessageAndTranslation("user_message_start_saving",
                this.editorInstance.getNote().getSubject());
    }

    @Override
    protected Void call() throws Exception {
        mainViewController.getBackend().update(this.editorInstance.getNote());
        return null;
    }
}
