package de.wesim.imapnotes;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;


import de.wesim.models.Note;

public class DeleteMessageTask extends AbstractNoteService<Void> {
    
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

    
    public DeleteMessageTask( IMAPBackend backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Deleting " + note.getValue().toString() + "...");

                getNote().delete(backend);

                updateMessage("Deleting was successful! :-)");
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }
   
}

