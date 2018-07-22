package de.wesim.imapnotes.ui.background;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.wesim.imapnotes.NoteController;
import javafx.concurrent.Service;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;


public abstract class AbstractNoteService<T> extends Service<T> {
	
	@Autowired
	@Qualifier("noteController")
	protected NoteController controller;
	
	@Autowired
	@Qualifier("p1")
	private ProgressBar progress;
	
	@Autowired
	private Label status;
    
    public AbstractNoteService( ) {
       
    }

    @PostConstruct
    public void init() {
    	 this.setOnScheduled(e -> {
 			progress.progressProperty().unbind();
 			progress.progressProperty().bind(this.progressProperty());
 			status.textProperty().unbind();
 			status.textProperty().bind( this.messageProperty());
         });
         this.setOnFailed(e-> {
			status.textProperty().unbind();
			// FIXME
         	status.setText(getException().getMessage());
            getException().printStackTrace();
         });
    }
}