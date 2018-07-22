package de.wesim.imapnotes.ui.background;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.ui.components.MyListView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private ObjectProperty<Note> note = new SimpleObjectProperty<Note>(this, "note");

    public final void setNote(Note value) {
        note.set(value);
    }

    public final Note getNote() {
        return note.get();
    }

    public final ObjectProperty<Note> noteProperty() {
        return note;
    }

    @Override
    protected Task<ObservableList<Note>> createTask() {
        Task<ObservableList<Note>> task = new Task<ObservableList<Note>>() {

            @Override
            protected ObservableList<Note> call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Beginne mit dem Laden der Notizen ...");

                final List<Note> messages = controller.getBackend().getNotes();	
                updateMessage("Notizenladen erfolgreich!");
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
            controller.openNote(firstELement);
        }
    }
}