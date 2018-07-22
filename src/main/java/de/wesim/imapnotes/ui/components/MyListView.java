package de.wesim.imapnotes.ui.components;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@Component
public class MyListView extends TreeView<Note> {

	@Autowired
	@Qualifier("noteController")
	protected NoteController controller;

	@Autowired
	private ListCellFactory listCellFactory;

	private MyTreeItemChangeListener changeListener;
	
	public MyListView() {
		super(new TreeItem<Note>());
		setShowRoot(false);
		setPrefWidth(150);
    }
	
	@PostConstruct
	public void init() {
		this.changeListener = new MyTreeItemChangeListener(controller);
		this.setCellFactory(listCellFactory);
		// set default selection listener
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Note>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<Note>> observable, TreeItem<Note> oldValue,
					TreeItem<Note> newValue) {
				if (newValue == null)
					return;
				if (oldValue != newValue) {
					controller.openNote(newValue.getValue());
				}

			}
		});
	}

	public static boolean isEmptyTreeItem(TreeItem<Note> treeItem) {
		if (treeItem.isLeaf()) return false;
		if (treeItem.getChildren().isEmpty()) return true;
		if (treeItem.getChildren().size() > 1) return false;
		TreeItem<Note> firstItem = treeItem.getChildren().get(0);
		return firstItem.getValue() == null;
	}

	public void addChildrenToNode(List<Note> loadedItems, TreeItem<Note> containedTreeItem) {
		containedTreeItem.getChildren().clear();
        for (Note n : loadedItems) {
            final TreeItem<Note> newItem = new TreeItem<Note>(n);
            if (n.isFolder()) {
                newItem.getChildren().add(new TreeItem<Note>());
                // TODO
                // https://stackoverflow.com/questions/14236666/how-to-get-current-treeitem-reference-which-is-expanding-by-user-click-in-javafx#14241151
                newItem.setExpanded(false);
                newItem.expandedProperty().addListener(this.changeListener);
            }
            containedTreeItem.getChildren().add(newItem);
        }
	}

	public static TreeItem<Note> searchTreeItem(Note searchItem, TreeItem<Note> parent) {
		if (parent.getValue() != null && searchItem.equals(parent.getValue())) {
			return parent;
		}
		if (parent.getChildren().isEmpty()) return null;
		for (TreeItem<Note> child : parent.getChildren()) {
			TreeItem<Note> found = MyListView.searchTreeItem(searchItem, child);
			if (found != null) return found;
		}
		return null;
	}
}