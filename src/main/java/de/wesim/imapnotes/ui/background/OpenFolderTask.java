package de.wesim.imapnotes.ui.background;

import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.INoteProvider;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class OpenFolderTask extends AbstractNoteService<Void> {
    
    private ObjectProperty<Note> noteFolder = new SimpleObjectProperty<Note>(this, "note");

    public final void setNoteFolder(Note value) {
        noteFolder.set(value);
    }

    public final Note getNoteFolder() {
        return noteFolder.get();
    }

    public final ObjectProperty<Note> noteFolderProperty() {
        return noteFolder;
    }

    
    public OpenFolderTask(  INoteProvider backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Opening " + noteFolder.getValue().toString() + "...");

                final Note folderToOpen = getNoteFolder();
                // TODO Konstante extrahieren
                if (folderToOpen.getUuid().startsWith("BACKTOPARENT")) {
                    System.out.println("OpenFolderTAsk: Return");
                    backend.returnToParent();
                } else {
                    System.out.println("OpenFolderTAsk: " + folderToOpen);
                    backend.openFolder(folderToOpen);
                }

                updateMessage(String.format("Öffnen von %s erfolgreich!", noteFolder.getValue().toString()));
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }
   
}