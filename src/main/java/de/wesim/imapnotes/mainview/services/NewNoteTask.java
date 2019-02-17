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
public class NewNoteTask extends AbstractNoteTask<Note> {

	@Autowired
	private OutlinerWidget outliner;
	
    private final TreeItem<Note> parentFolder;
    private final boolean createFolder;
    private final String subject;
    
	public NewNoteTask( TreeItem<Note> parent, String subject, boolean isFolder ) {
        super();
        this.parentFolder = parent;
        this.subject = subject;
        this.createFolder = isFolder;
    }
    
    @Override
	protected void succeeded() {
    	super.succeeded();
    	Platform.runLater( () -> {
    		outliner.addNoteToTree(this.parentFolder, getValue());
    		mainViewController.openNote(getValue());
    	});
	}

	@Override
	public String getActionName() {
		return "Create Note";
	}

	@Override
	public String getSuccessMessage() {
		return i18N.getMessageAndTranslation("user_folder_finished_creating",
        		this.subject);           
	}

	@Override
	public String getRunningMessage() {
		return i18N.getMessageAndTranslation("user_folder_start_creating",
        		this.subject);           
	}

	@Override
	protected Note call() throws Exception {
		Note parentFolderParam = null;
        if (parentFolder != null) {
            parentFolderParam = parentFolder.getValue();
        }

        final Note newNote;
        if (createFolder) {
            newNote = mainViewController.getBackend().createNewFolder(subject, parentFolderParam);
        } else {
            newNote = mainViewController.getBackend().createNewNote(subject, parentFolderParam);
        }
        return newNote;
	}

}