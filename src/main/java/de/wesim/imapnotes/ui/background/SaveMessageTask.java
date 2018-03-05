package de.wesim.imapnotes.ui.background;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.INoteProvider;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
public class SaveMessageTask extends AbstractNoteService<Void> {

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

    public SaveMessageTask(  INoteProvider backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Beginne mit dem Speichern ...");

                backend.update(getNote());
                //Thread.sleep(2000);
                updateMessage("Speichern erfolgreich!");
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }

}