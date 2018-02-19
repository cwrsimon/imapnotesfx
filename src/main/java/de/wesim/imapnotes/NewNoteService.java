package de.wesim.imapnotes;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import de.wesim.models.Note;
import de.wesim.services.INoteProvider;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
public class NewNoteService extends AbstractNoteService<Note> {

    private StringProperty subject = new SimpleStringProperty();

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public NewNoteService(  INoteProvider backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Note> createTask() {
        Task<Note> task = new Task<Note>() {

            @Override
            protected Note call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Creating new note ...");

                final Note newNote = backend.createNewNote(subject.getValue());
                updateMessage(String.format("Speichern von %s erfolgreich!", subject.getValue()));
                updateProgress(1, 1);

                return newNote;
            }
        };
        return task;
    }

}