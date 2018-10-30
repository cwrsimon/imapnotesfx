package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.scene.control.TreeView;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
@Component
public class RenameNoteService extends AbstractNoteTask<Void> {

    @Autowired
	@Qualifier("myListView")
	private TreeView<Note> noteCB;

	private final Note note;

	private String subject;

	private String oldTitle;

	public RenameNoteService(Note note, String subject) {
		this.note = note;
		this.subject = subject;
		this.oldTitle = this.note.getSubject();
	}

    @Override
	protected void succeeded() {
    	super.succeeded();
        noteCB.refresh();
        if (this.note.isFolder()) {
            mainViewController.triggerReload();
        }
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