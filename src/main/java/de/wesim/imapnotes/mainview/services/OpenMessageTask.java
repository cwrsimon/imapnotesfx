package de.wesim.imapnotes.mainview.services;

import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

@Component
public class OpenMessageTask extends AbstractNoteService<Note> {
    
    private ObjectProperty<Note> note = new SimpleObjectProperty<Note>(this, "note");

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
                updateMessage(i18N.getFormattedMessage("user_message_start_opening",
						note.getValue().getSubject()));
                mainViewController.getBackend().load(workingItem);

                updateMessage(i18N.getFormattedMessage("user_message_finished_opening",
						note.getValue().getSubject()));                
                updateProgress(1, 1);

                return workingItem;
            }
        };
        return task;
    }

	@Override
	public String getActionName() {
		return "Open Message";
	}
   
}