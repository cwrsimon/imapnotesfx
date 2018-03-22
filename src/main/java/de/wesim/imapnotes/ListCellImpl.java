
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
	private final ContextMenu newMenu = new ContextMenu();


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
		final MenuItem newItem = new MenuItem("Neu");
		addMenu.getItems().add(newItem);
		newMenu.getItems().add(newItem);

		newItem.setOnAction(e -> {
			caller.createNewMessage(false);                
		});

		eventHandler = e-> {
			if (e.getButton() == MouseButton.PRIMARY 
					&& e.getClickCount() == 2) {
				caller.openNote(getItem());
			}
		};

	}

	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
		setStyle("-fx-control-inner-background: white;");		

		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setOnMouseClicked( null );
			setContextMenu(newMenu);

		} else {
			if (item.isFolder()) {
				setStyle("-fx-control-inner-background: yellow;");		
			} else { 

			}
			setText(item.getSubject());
			setContextMenu(addMenu);

			setOnMouseClicked( eventHandler);
		}
	}
}
