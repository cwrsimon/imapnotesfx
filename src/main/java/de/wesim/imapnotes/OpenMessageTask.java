package de.wesim.imapnotes;

import de.wesim.models.Note;
import de.wesim.services.INoteProvider;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class OpenMessageTask extends AbstractNoteService<String> {
    
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

    
    public OpenMessageTask(  INoteProvider backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<String> createTask() {
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Opening " + note.getValue().toString() + "...");

                backend.load(getNote());

                updateMessage(String.format("Öffnen von %s erfolgreich!", note.getValue().toString()));
                updateProgress(1, 1);

                return getNote().getContent();
            }
        };
        return task;
    }
   
}