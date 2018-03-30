package de.wesim.imapnotes.ui.background;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class MoveNoteService extends AbstractNoteService<Boolean> {
    
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

    private ObjectProperty<Note> folder = new SimpleObjectProperty<Note>(this, "folder");

    public final void setFolder(Note value) {
        folder.set(value);
    }

    public final Note getFolder() {
        return folder.get();
    }

    public final ObjectProperty<Note> folderProperty() {
        return folder;
    }

    
    public MoveNoteService( NoteController backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Boolean> createTask() {
        Task<Boolean> task = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Moving " + note.getValue().toString() + "...");

                boolean retValue = controller.getBackend().move(getNote(), getFolder());

                updateMessage("Moving was successful! :-)");
                updateProgress(1, 1);

                return retValue;
            }
        };
        return task;
    }
   
}

