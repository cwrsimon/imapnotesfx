
package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ListCellImpl extends ListCell<Note> {

	private NoteController caller;
	private final ContextMenu addMenu = new ContextMenu();

	private EventHandler<? super MouseEvent> eventHandler;
	public ListCellImpl (NoteController caller) {
		final MenuItem deleteItem = new MenuItem("Delete");
		addMenu.getItems().add(deleteItem);
		deleteItem.setOnAction(e -> {
			caller.deleteCurrentMessage(getItem());
		});
		final MenuItem renameItem = new MenuItem("Rename");
		addMenu.getItems().add(renameItem);
		renameItem.setOnAction(e -> {
			caller.renameCurrentMessage(getItem());                
		});

		eventHandler = e-> {
			if (e.getButton() == MouseButton.PRIMARY 
					&& e.getClickCount() == 2) {
				// TODO Hier weitermachen
				System.out.println("Double-Click!");
				System.out.println(getItem());
				System.out.println(caller.toString());

				caller.openNote(getItem());
			}
		};

	}

	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setOnMouseClicked( null );

		} else {
			setText(item.getSubject());
			setContextMenu(addMenu);

			setOnMouseClicked( eventHandler);
		}
	}
}
