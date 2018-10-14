package de.wesim.imapnotes.mainview.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.MyListView;
import de.wesim.imapnotes.models.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

@Component
public class LoadMessageTask extends AbstractNoteService<ObservableList<Note>> {

    public LoadMessageTask( ) {
        super();
    }

    @Autowired
	@Qualifier("myListView")
	private MyListView noteCB;

    @Override
    protected Task<ObservableList<Note>> createTask() {
        Task<ObservableList<Note>> task = new Task<ObservableList<Note>>() {

            @Override
            protected ObservableList<Note> call() throws Exception {
                updateProgress(0, 1);
                updateMessage(i18N.getTranslation("user_message_start_loading"));

                final List<Note> messages = mainViewController.getBackend().getNotes();	
                updateMessage(i18N.getTranslation("user_message_finished_loading"));
                updateProgress(1, 1);

                return FXCollections.observableArrayList(messages);
            }
        };
        return task;
    }

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
}