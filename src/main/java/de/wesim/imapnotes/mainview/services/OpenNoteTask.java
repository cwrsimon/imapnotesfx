package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.I18NService;
import javafx.application.Platform;

@Component
@Scope("prototype")
public class OpenNoteTask extends AbstractNoteTask<Note> {

	
    @Autowired
    protected MainViewController mainViewController;

    @Autowired
    protected I18NService i18N;

    private final Note note;

    public OpenNoteTask(Note note) {
        this.note = note;
    }

    @Override
    protected Note call() throws Exception {
        mainViewController.getBackend().load(this.note);
        return note;
    }


    @Override
    protected void succeeded() {
        super.succeeded();
        Platform.runLater(() -> mainViewController.openEditor(getValue()));
    }

    @Override
    public String getActionName() {
        return "Open Message";
    }

    @Override
    public String getRunningMessage() {
        return i18N.getMessageAndTranslation("user_message_start_opening",
                note.getSubject());
    }

    @Override
    public String getSuccessMessage() {
        return i18N.getMessageAndTranslation("user_message_finished_opening",
                this.note.getSubject());
    }

}
