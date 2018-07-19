package de.wesim.imapnotes.ui.components;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@Component
public class MyListView extends TreeView<Note> {

	@Autowired
	private ListCellFactory listCellFactory;
	
	public MyListView() {
		super(new TreeItem<Note>());
		setShowRoot(false);
		setPrefWidth(150);
    }
	
	@PostConstruct
	public void init() {
		this.setCellFactory(listCellFactory);
	}

	public static boolean isEmptyTreeItem(TreeItem<Note> treeItem) {
		if (treeItem.isLeaf()) return false;
		if (treeItem.getChildren().isEmpty()) return true;
		if (treeItem.getChildren().size() > 1) return false;
		TreeItem<Note> firstItem = treeItem.getChildren().get(0);
		return firstItem.getValue() == null;
	}

}