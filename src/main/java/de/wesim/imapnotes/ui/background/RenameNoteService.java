package de.wesim.imapnotes.ui.background;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
public class RenameNoteService extends AbstractNoteService<Void> {

    private StringProperty subject = new SimpleStringProperty();

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    private ObjectProperty<Note> note = new SimpleObjectProperty<Note>(this, "note");

    public final void setNote(Note value) {
        note.set(value);
    }

    public final Note getNote() {
        return note.get();
    }

    public final ObjectProperty<Note> noteProperty() {
        return note;
    }

    public RenameNoteService(  NoteController backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Rename note '' to '' ...");
                System.out.println("Renaming ..." + subject.getValue());
                //try {
                if (getNote().isFolder()) {
                	controller.getBackend().renameFolder(getNote(), subject.getValue());
                } else {
                	controller.getBackend().renameNote(getNote(), subject.getValue());
                }
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
                updateMessage(String.format("Umbennen von %s erfolgreich!", subject.getValue()));
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }

}