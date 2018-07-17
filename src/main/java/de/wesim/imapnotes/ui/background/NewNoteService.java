package de.wesim.imapnotes.ui.background;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
@Component
public class NewNoteService extends AbstractNoteService<Note> {

    private StringProperty subject = new SimpleStringProperty();

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    private BooleanProperty createFolder = new SimpleBooleanProperty();

    public void setCreateFolder(boolean flag) {
        this.createFolder.set(flag);
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

    public NewNoteService( ) {
        super();
    }

    @Override
    protected Task<Note> createTask() {
        Task<Note> task = new Task<Note>() {

            @Override
            protected Note call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Creating new note ...");
                Note parentFolderParam = null;
                if (parentFolder.getValue() != null) {
                    parentFolderParam = parentFolder.getValue().getValue();
                }

                final Note newNote;
                if (createFolder.getValue()) {
                        newNote = controller.getBackend().createNewFolder(subject.getValue(), parentFolderParam);
                } else {
                    newNote = controller.getBackend().createNewNote(subject.getValue(), parentFolderParam);
                }
                updateMessage(String.format("Speichern von %s erfolgreich!", subject.getValue()));
                updateProgress(1, 1);

                return newNote;
            }
        };
        return task;
    }

}