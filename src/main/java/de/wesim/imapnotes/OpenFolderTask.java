package de.wesim.imapnotes;

import de.wesim.models.Note;
import de.wesim.models.NoteFolder;
import de.wesim.services.INoteProvider;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class OpenFolderTask extends AbstractNoteService<String> {
    
    private ObjectProperty<NoteFolder> noteFolder = new SimpleObjectProperty<NoteFolder>(this, "noteFolder");

    public final void setNoteFolder(NoteFolder value) {
        noteFolder.set(value);
    }

    public final NoteFolder getNoteFolder() {
        return noteFolder.get();
    }

    public final ObjectProperty<NoteFolder> noteFolderProperty() {
        return noteFolder;
    }

    
    public OpenFolderTask(  INoteProvider backend, ProgressBar progress, Label status ) {
        super(backend, progress, status);
    }

    @Override
    protected Task<String> createTask() {
        Task<String> task = new Task<String>() {

            @Override
            protected String call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Opening " + noteFolder.getValue().toString() + "...");

                final NoteFolder folderToOpen = getNoteFolder();
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

                return getNoteFolder().getContent();
            }
        };
        return task;
    }
   
}