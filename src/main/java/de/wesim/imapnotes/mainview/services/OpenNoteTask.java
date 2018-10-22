package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.I18NService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

@Component
@Scope("prototype")
public class OpenNoteTask extends Task<Note> {

	
	@Autowired
	protected MainViewController mainViewController;
	
    @Autowired
    protected I18NService i18N;

    @Autowired
    @Qualifier("p1")
    private ProgressBar progress;

    @Autowired
    private Label status;

    
	private final Note note;

	public OpenNoteTask(Note note) {
		this.note = note;
	}
	
	@Override
	protected Note call() throws Exception {
      mainViewController.getBackend().load(this.note);
      return note;
	}

	
	@Override
	protected void scheduled() {
        //progress.progressProperty().unbind();
        progress.progressProperty().bind(this.progressProperty());
        //status.textProperty().unbind();
        status.textProperty().bind(this.messageProperty());
	}

	@Override
	protected void running() {
		updateProgress(0, 1);
	    updateMessage(i18N.getMessageAndTranslation("user_message_start_opening",
					note.getSubject())); 
	}

	@Override
	protected void succeeded() {
	      updateProgress(1, 1);
	      updateMessage(i18N.getMessageAndTranslation("user_message_finished_opening",
					this.note.getSubject()));  
	      
	      Platform.runLater( () -> mainViewController.openEditor(getValue()) );
	}

	@Override
	protected void failed() {
        //status.textProperty().unbind();
        status.setText(getException().getLocalizedMessage());
	}
	
	
    
	//https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
		
//    private ObjectProperty<Note> note = new SimpleObjectProperty<Note>(this, "note");
//
//    public final ObjectProperty<Note> noteProperty() {
//        return note;
//    }
//    
//    public OpenMessageTask(  ) {
//        super();
//    }
//
//    @Override
//	protected void succeeded() {
//    	final Note openedNote = getValue();
//    	mainViewController.openEditor(openedNote);
//	}
//
//	@Override
//    protected Task<Note> createTask() {
//        Task<Note> task = new Task<Note>() {
//
//            @Override
//            protected Note call() throws Exception {
//            	final Note workingItem = note.getValue();
//                updateProgress(0, 1);
//                updateMessage(i18N.getMessageAndTranslation("user_message_start_opening",
//						note.getValue().getSubject()));
//               
//                mainViewController.getBackend().load(workingItem);
//
//                updateMessage(i18N.getMessageAndTranslation("user_message_finished_opening",
//						note.getValue().getSubject()));                
//                updateProgress(1, 1);
//
//                return workingItem;
//            }
//        };
//        return task;
//    }
//
//	@Override
//	public String getActionName() {
//		return "Open Message";
//	}
   
}