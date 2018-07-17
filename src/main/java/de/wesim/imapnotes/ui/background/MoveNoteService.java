package de.wesim.imapnotes.ui.background;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;

@Component
public class MoveNoteService extends AbstractNoteService<Note> {
    
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

    private ObjectProperty<TreeItem<Note>> parentFolder = new SimpleObjectProperty<TreeItem<Note>>(this, "parentFolder");

    public final void setParentFolder(TreeItem<Note> value) {
        parentFolder.set(value);
    }

    public final TreeItem<Note> getParentFolder() {
        return parentFolder.get();
    }

    public final ObjectProperty<TreeItem<Note>> parentFolderProperty() {
        return parentFolder;
    }

    public MoveNoteService( ) {
        super();
    }

    @Override
    protected Task<Note> createTask() {
        Task<Note> task = new Task<Note>() {

            @Override
            protected Note call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Moving " + note.getValue().toString() + "...");

                Note retValue = 
                    controller.getBackend().move(getNote(), parentFolder.get().getValue());

                updateMessage("Moving was successful! :-)");
                updateProgress(1, 1);

                return retValue;
            }
        };
        return task;
    }
   
}

