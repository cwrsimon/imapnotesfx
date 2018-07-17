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
public class DeleteMessageTask extends AbstractNoteService<Void> {
    
    private ObjectProperty<TreeItem<Note>> note = new SimpleObjectProperty<TreeItem<Note>>(this, "note");

    public final void setNote(TreeItem<Note> value) {
        note.set(value);
    }

    public final TreeItem<Note> getNote() {
        return note.get();
    }

    public final ObjectProperty<TreeItem<Note>> noteProperty() {
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
    
    public DeleteMessageTask() {
        super();
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Deleting " + note.getValue().toString() + "...");

                controller.getBackend().delete(getNote().getValue());

                updateMessage("Deleting was successful! :-)");
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }
   
}

