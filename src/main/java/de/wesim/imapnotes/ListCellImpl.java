
package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

public class ListCellImpl extends ListCell<Note> {

    private NoteController caller;
    private final ContextMenu addMenu = new ContextMenu();
    
	public ListCellImpl (NoteController caller) {
        final MenuItem addMenuItem = new MenuItem("Bla");
            addMenu.getItems().add(addMenuItem);
            // addMenuItem.setOnAction();
            //     getTreeItem().getChildren().add(newEmployee);
            // });
    }

    @Override
    public void updateItem(Note item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getSubject());
            setContextMenu(addMenu);
        }
    }
}
