package de.wesim.imapnotes.ui.background;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
public class NewNoteService extends AbstractNoteService<Note> {

    private StringProperty subject = new SimpleStringProperty();

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    private BooleanProperty createFolder = new SimpleBooleanProperty();

    public void setCreateFolder(boolean flag) {
        this.createFolder.set(flag);
    }

    public NewNoteService( NoteController backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Note> createTask() {
        Task<Note> task = new Task<Note>() {

            @Override
            protected Note call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Creating new note ...");

                final Note newNote;
                if (createFolder.getValue()) {
                    newNote = controller.getBackend().createNewFolder(subject.getValue());
                } else {
                    System.out.println("Erstelle Notiz");
                    newNote = controller.getBackend().createNewNote(subject.getValue());
                }
                updateMessage(String.format("Speichern von %s erfolgreich!", subject.getValue()));
                updateProgress(1, 1);

                return newNote;
            }
        };
        return task;
    }

}