package de.wesim.imapnotes.mainview.components.outliner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@Component
public class OutlinerWidget extends TreeView<Note> implements HasLogger {

	@Autowired
	protected MainViewController mainViewController;

	@Autowired
	private OutlinerCellFactory listCellFactory;

	private OutlinerItemChangeListener changeListener;

	public OutlinerWidget() {
		super(new TreeItem<Note>());
		setShowRoot(false);
		setPrefWidth(150);
	}

	@PostConstruct
	public void init() {
		this.changeListener = new OutlinerItemChangeListener(mainViewController);
		this.setCellFactory(listCellFactory);
		// set default selection listener
		getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Note>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<Note>> observable, TreeItem<Note> oldValue,
					TreeItem<Note> newValue) {
				if (newValue == null)
					return;
				if (oldValue != newValue) {
					mainViewController.openNote(newValue.getValue());
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
			TreeItem<Note> found = OutlinerWidget.searchTreeItem(searchItem, child);
			if (found != null) return found;
		}
		return null;
	}

	public void addNoteToTree(TreeItem<Note> treeItem, Note newNote) {
		final TreeItem<Note> newTreeItem = new TreeItem<Note>(newNote);
		if (newNote.isFolder()) {
			if (OutlinerWidget.isEmptyTreeItem(newTreeItem)) {
				newTreeItem.getChildren().clear();
			}
			newTreeItem.getChildren().add(new TreeItem<Note>(null));
		}
		if (treeItem != null) {
			if (OutlinerWidget.isEmptyTreeItem(treeItem)) {
				treeItem.getChildren().clear();
			}
			treeItem.getChildren().add(newTreeItem);
		} else {
			getRoot().getChildren().add(newTreeItem);
		}
	}
	
	private static void getChildren(TreeItem<Note> node, String prefix, List<String> accu) {
//		if (node.getValue() == null) {
//			return;
//		}
//		if (node.getChildren().isEmpty()) {
//			accu.add(prefix);
//			return;
//		}
		for (TreeItem<Note> child : node.getChildren()) {
			final Note value = child.getValue();
			if (value == null) continue;
			if (!value.isFolder()) continue;
			String myprefix = prefix + value.getSubject() + "/";
			accu.add(myprefix);
			getChildren(child, myprefix, accu);
		}
	}
	
	public Map<String, TreeItem<Note>> getFlatList() {
		List<String> accu = new ArrayList<>();
		accu.add("/");
		getChildren(getRoot(), "/", accu);

		getLogger().info("{}", accu
		);
//		if (parent.getValue() != null && searchItem.equals(parent.getValue())) {
//			return parent;
//		}
//		if (parent.getChildren().isEmpty()) return null;
//		for (TreeItem<Note> child : parent.getChildren()) {
//			TreeItem<Note> found = OutlinerWidget.searchTreeItem(searchItem, child);
//			if (found != null) return found;
//		}
		return null;
	}
}