package de.wesim.imapnotes.mainview.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.MyListView;
import de.wesim.imapnotes.models.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Component
@Scope("prototype")
public class LoadNotesTask extends AbstractNoteTask<ObservableList<Note>> {

    public LoadNotesTask() {
        super();
    }

    @Autowired
	@Qualifier("myListView")
	private MyListView noteCB;


    // TODO
    @Override
	protected void succeeded() {
        final ObservableList<Note> loadedItems = getValue();
        this.noteCB.addChildrenToNode(loadedItems, noteCB.getRoot());

        // open the first element
        if (loadedItems.isEmpty()) return;
        final Note firstELement = loadedItems.get(0);
        if (!firstELement.isFolder()) {
        	mainViewController.openNote(firstELement);
        }
    }

	@Override
	public String getActionName() {
		return "Load Messages";
	}

	@Override
	public String getSuccessMessage() {
		return i18N.getTranslation("user_message_finished_loading");
	}

	@Override
	public String getRunningMessage() {
		return i18N.getTranslation("user_message_start_loading");
	}

	@Override
	protected ObservableList<Note> call() throws Exception {
        final List<Note> messages = mainViewController.getBackend().getNotes();	
        return FXCollections.observableArrayList(messages);
	}
}