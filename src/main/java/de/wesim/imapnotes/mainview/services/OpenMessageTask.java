package de.wesim.imapnotes.mainview.services;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

@Component
public class OpenMessageTask extends AbstractNoteService<Note> {
    
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

    
    public OpenMessageTask(  ) {
        super();
    }
    
    

    @Override
	protected void succeeded() {
    	final Note openedNote = getValue();
    	mainViewController.openEditor(openedNote);
	}

	@Override
    protected Task<Note> createTask() {
        Task<Note> task = new Task<Note>() {

            @Override
            protected Note call() throws Exception {
            	final Note workingItem = note.getValue();
                updateProgress(0, 1);
                updateMessage(String.format("Opening %s ...", workingItem.getSubject()));

                mainViewController.getBackend().load(workingItem);

                updateMessage(String.format("%s was successfully opened!", workingItem.getSubject()));
                updateProgress(1, 1);

                return workingItem;
            }
        };
        return task;
    }
   
}