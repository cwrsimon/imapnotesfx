package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.models.Note;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

@Component
@Scope("prototype")
public class MoveNoteTask extends AbstractNoteTask<Void> {
    
    @Autowired
	private OutlinerWidget outlinerWidget;
    
	private final TreeItem<Note> moveItem;
	private final TreeItem<Note> target;

    
    public MoveNoteTask( TreeItem<Note> moveItem, TreeItem<Note> target) {
        super();
        this.moveItem = moveItem;
        this.target = target;
    }
   
    @Override
	protected void succeeded() {
    	super.succeeded();
    	Platform.runLater( () -> {	
    		mainViewController.removeTreeItem(this.moveItem);
    		target.getChildren().add(new TreeItem<>(this.moveItem.getValue()));    		
    		outlinerWidget.refresh();    		
    	});
	}

	@Override
	public String getActionName() {
		return "Move Note";
	}

	@Override
	public String getSuccessMessage() {
		 return i18N.getMessageAndTranslation("user_message_finished_moving",
	        		this.moveItem.getValue().getSubject());     
	}

	@Override
	public String getRunningMessage() {
            return i18N.getMessageAndTranslation("user_message_start_moving",
	        		this.moveItem.getValue().getSubject());                
	}

	@Override
	protected Void call() throws Exception {
            mainViewController.getBackend().move(this.moveItem.getValue(), this.target.getValue());
            return null;
	}
    
    
}

