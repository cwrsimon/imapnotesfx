package de.wesim.imapnotes.mainview.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

@Component
@Scope("prototype")
public class DeleteNoteTask extends AbstractNoteTask<Void> {

	private TreeItem<Note> treeItem;

	public DeleteNoteTask(TreeItem<Note> treeItem) {
		super();
		this.treeItem = treeItem;
	}

	@Override
	protected void succeeded() {
    	super.succeeded();
		Platform.runLater( () -> {
			mainViewController.removeTreeItem( this.treeItem );
		});

	}

	@Override
	public String getActionName() {
		return "Delete Message";
	}

	@Override
	public String getSuccessMessage() {
		 return i18N.getMessageAndTranslation("user_message_finished_deleting",
	        		this.treeItem.getValue().getSubject());     
	}

	@Override
	public String getRunningMessage() {
		 return i18N.getMessageAndTranslation("user_message_start_deleting",
	        		this.treeItem.getValue().getSubject());                
	}

	@Override
	protected Void call() throws Exception {
		mainViewController.getBackend().delete(this.treeItem.getValue());
		return null;
	}
}

