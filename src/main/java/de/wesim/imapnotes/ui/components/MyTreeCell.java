
package de.wesim.imapnotes.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class MyTreeCell extends TreeCell<Note> {

	private static final Logger logger = LoggerFactory.getLogger(MyTreeCell.class);

	private static final DataFormat myNotes = new DataFormat("de.wesim.imapnotes.models.Note");
	//private static final DataFormat myNotes = new DataFormat("javafx.scene.control.TreeItem");

	private final ContextMenu noteMenu = new ContextMenu();
	private final ContextMenu genericMenu = new ContextMenu();
	private final ContextMenu folderMenu = new ContextMenu();

	public MyTreeCell (NoteController caller) {
		final MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setOnAction(e -> {
			final TreeItem<Note> treeItem =  getTreeItem();
			caller.deleteCurrentMessage(getTreeItem(), false);
		});

		final MenuItem renameItem = new MenuItem("Rename");
		renameItem.setOnAction(e -> {
			caller.renameCurrentMessage(getItem());                
		});

		final MenuItem delete2 = new MenuItem("deleteme");
		final MenuItem renameNote = new MenuItem("renameme");
		noteMenu.getItems().add(renameNote);
		noteMenu.getItems().add(delete2);
		delete2.setOnAction(e -> {
			caller.deleteCurrentMessage(getTreeItem(), false);
		});
		renameNote.setOnAction(e -> {
			caller.renameCurrentMessage(getItem());                
		});

		final MenuItem newItem = new MenuItem("New Root Note");
		newItem.setOnAction(e -> {
			caller.createNewMessage(false, null);                
		});

		final MenuItem newFolder = new MenuItem("New Root Folder");
		newFolder.setOnAction(e -> {
			caller.createNewMessage(true, null);              
		});

		genericMenu.getItems().add(newItem);
		genericMenu.getItems().add(newFolder);


		final MenuItem newSubfolder = new MenuItem("Add subfolder");
		final MenuItem newFolderNote = new MenuItem("Add note to folder");
		newFolderNote.setOnAction(e -> {
			caller.createNewMessage(false, getTreeItem());                
		});
		newSubfolder.setOnAction(e -> {
			caller.createNewMessage(true, getTreeItem());                
		});
		folderMenu.getItems().add(newSubfolder);
		folderMenu.getItems().add(newFolderNote);
		folderMenu.getItems().add(renameItem);
		folderMenu.getItems().add(deleteItem);

		this.setOnDragDetected(e -> {
			final Dragboard db = this.startDragAndDrop( TransferMode.MOVE );
			final ClipboardContent content = new ClipboardContent();			
			content.put(myNotes, getItem());
			db.setContent( content );
			e.consume();
			logger.info("Drage Detected");

		});
		this.setOnDragOver( ( DragEvent event ) ->
		{
			logger.info("Drage Over");

			final Dragboard db = event.getDragboard();
			if ( db.hasContent(myNotes) 
					&& getItem().isFolder() && getTreeItem().getValue().isFolder())
			{
				logger.info("onDragOver:" + getItem());
				event.acceptTransferModes( TransferMode.MOVE );
			}
			event.consume();
		} );
		this.setOnDragEntered(( DragEvent event ) -> {
			logger.info("Drag Entered");

			if (event.getGestureSource() != getItem() &&
					event.getDragboard().hasContent(myNotes)

					&& getTreeItem().getValue().isFolder()) {
				this.setTextFill(Color.RED);
			}

			event.consume();
		}
				);
		this.setOnDragExited(( DragEvent event ) -> {
			logger.info("setOnDragExited");

			//this.setUnderline(false);
			this.setTextFill(Color.BLACK);
			//items.pop();
			event.consume();
		}
				);
		// Beim Ziel, hier muss dann verschoben werden ...
		this.setOnDragDropped(( DragEvent event ) -> {
			logger.info("setOnDragDropped");

			final Dragboard db = event.getDragboard();
			final Note source = (Note) db.getContent(myNotes);
			caller.move(source, getTreeItem());
			event.setDropCompleted(true);    
			event.consume();
		});

	}



	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
		//	setStyle("-fx-control-inner-background: white;");		
		setUnderline(false);
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setContextMenu(genericMenu);
			setBorder(null);
		} else {
			if (item.isFolder()) {
				setContextMenu(folderMenu);
				setUnderline(true);
			} else { 
				setContextMenu(noteMenu);
				setGraphic(null);				
				setBorder(null);
			}
			setText(item.getSubject());
		}
	}
}
