package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.models.Note;
import javafx.application.Platform;
import javafx.scene.control.TreeView;

@Component
@Scope("prototype")
public class RenameNoteTask extends AbstractNoteTask<Void> {

	@Autowired
	private OutlinerWidget outlinerWidget;

	private final Note note;

	private String subject;

	private String oldTitle;

	public RenameNoteTask(Note note, String subject) {
		this.note = note;
		this.subject = subject;
		this.oldTitle = this.note.getSubject();
	}

    @Override
	protected void succeeded() {
    	super.succeeded();
    	Platform.runLater( () -> {
    		outlinerWidget.refresh();
    		// TODO FIXME unnötig, falls wir nur den Ordner neu laden ...
        	// if (this.note.isFolder()) {
            // 	mainViewController.triggerReload();
        	// }
    	});
	}

	@Override
	public String getActionName() {
		return "Rename Note";
	}

	@Override
	public String getSuccessMessage() {
		return i18N.getMessageAndTranslation("user_message_finished_renaming",
				this.oldTitle); 
	}

	@Override
	public String getRunningMessage() {
		return i18N.getMessageAndTranslation("user_message_start_renaming",
				this.oldTitle); 
	}

	@Override
	protected Void call() throws Exception {
		if (this.note.isFolder()) {
        	mainViewController.getBackend().renameFolder(this.note, this.subject);
        } else {
        	mainViewController.getBackend().renameNote(this.note, this.subject);
        }
		return null;
	}

}