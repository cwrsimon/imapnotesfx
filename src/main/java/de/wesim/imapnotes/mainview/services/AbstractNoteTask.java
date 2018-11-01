package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.services.I18NService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

@Component
@Scope("prototype")
public abstract class AbstractNoteTask<T> extends Task<T> implements HasLogger {
	
	@Autowired
	protected MainViewController mainViewController;
	
    @Autowired
    protected I18NService i18N;

    @Autowired
    @Qualifier("p1")
    private ProgressBar progress;

    @Autowired
    private Label status;

	
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
	    updateMessage(getRunningMessage()); 
	}

	@Override
	protected void succeeded() {
	      updateProgress(1, 1);
	      updateMessage(getSuccessMessage());  
	      
	}

	@Override
	protected void failed() {
        //status.textProperty().unbind();
        updateMessage(getException().getLocalizedMessage());
        getLogger().error("Action has failed: {}", getActionName(), getException());
	}
	
    public abstract String getActionName();

    public abstract String getSuccessMessage();
    
    public abstract String getRunningMessage();
   
}