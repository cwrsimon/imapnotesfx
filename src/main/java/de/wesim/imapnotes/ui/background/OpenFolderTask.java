package de.wesim.imapnotes.ui.background;

import java.util.List;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;

public class OpenFolderTask extends AbstractNoteService<ObservableList<Note>> {
    
    private ObjectProperty<TreeItem<Note>> noteFolder = new SimpleObjectProperty<TreeItem<Note>>(this, "note");

    public final void setNoteFolder(TreeItem<Note> value) {
        noteFolder.set(value);
    }

    public final TreeItem<Note> getNoteFolder() {
        return noteFolder.get();
    }

    public final ObjectProperty<TreeItem<Note>> noteFolderProperty() {
        return noteFolder;
    }

    
    public OpenFolderTask(  NoteController backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<ObservableList<Note>> createTask() {
        Task<ObservableList<Note>> task = new Task<ObservableList<Note>>() {

            @Override
            protected ObservableList<Note> call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Opening " + noteFolder.getValue().toString() + "...");

                final TreeItem<Note> openedItem = getNoteFolder();
                final Note folderToOpen = openedItem.getValue();
                // TODO Konstante extrahieren
                // if (folderToOpen.getUuid().startsWith("BACKTOPARENT")) {
                //     System.out.println("OpenFolderTAsk: Return");
                //     controller.getBackend().returnToParent();
                // } else {
                final List<Note> messages  = controller.getBackend().getNotesFromFolder(folderToOpen);
                //}

                updateMessage(String.format("Öffnen von %s erfolgreich!", noteFolder.getValue().toString()));
                updateProgress(1, 1);

                return FXCollections.observableArrayList(messages);
            }
        };
        return task;
    }
   
}