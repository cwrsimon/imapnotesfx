package de.wesim.imapnotes.ui.background;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@Component
public class LoadMessageTask extends AbstractNoteService<ObservableList<Note>> {

    public LoadMessageTask( ) {
        super();
    }

    @Autowired
	@Qualifier("myListView")
	private TreeView<Note> noteCB;

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
        noteCB.getRoot().getChildren().clear();
        for (Note n : loadedItems) {
            final TreeItem<Note> newItem = new TreeItem<Note>(n);
            if (n.isFolder()) {
                newItem.getChildren().add(new TreeItem<Note>());
                // TODO
                // https://stackoverflow.com/questions/14236666/how-to-get-current-treeitem-reference-which-is-expanding-by-user-click-in-javafx#14241151
                newItem.setExpanded(false);
                newItem.expandedProperty().addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                            Boolean newValue) {
                        if (!newValue) {
                            return;
                        }

                        BooleanProperty bb = (BooleanProperty) observable;

                        TreeItem<Note> callee = (TreeItem<Note>) bb.getBean();
                        if (callee.getChildren().size() != 1)
                            return;
                        // nur bei einem einzigen leeren Kind
                        if (callee.getChildren().get(0).getValue() != null)
                            return;
                        controller.openFolder(callee);

                    }
                });
            }
            noteCB.getRoot().getChildren().add(newItem);
        }
        // noteCB.setItems(loadedItems);
        // currentlyOPen = null;
        // das erste Element öffnen
        if (loadedItems.isEmpty()) return;
        final Note firstELement = loadedItems.get(0);
        if (!firstELement.isFolder()) {
            controller.openNote(firstELement);
        }

    }
}