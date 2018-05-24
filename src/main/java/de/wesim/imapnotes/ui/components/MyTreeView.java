
package de.wesim.imapnotes.ui.components;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class MyTreeView extends TreeCell<Note> {

    private static final DataFormat myNotes = new DataFormat("de.wesim.imapnotes.models.Note");

	private NoteController caller;
	private final ContextMenu noteMenu = new ContextMenu();
	private final ContextMenu genericMenu = new ContextMenu();
	private final ContextMenu folderMenu = new ContextMenu();


	private EventHandler<? super MouseEvent> eventHandler;
	public MyTreeView (NoteController caller) {
		
		final MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setOnAction(e -> {
			caller.deleteCurrentMessage(getItem(), false);
		});

		final MenuItem renameItem = new MenuItem("Rename");
		noteMenu.getItems().add(renameItem);
		renameItem.setOnAction(e -> {
			caller.renameCurrentMessage(getItem());                
		});
		//noteMenu.getItems().add(newItem);
		final MenuItem delete2 = new MenuItem("deleteme");
		noteMenu.getItems().add(delete2);


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
			caller.createNewMessage(false, getItem());                
		});
		newSubfolder.setOnAction(e -> {
			caller.createNewMessage(true, getItem());                
		});
		folderMenu.getItems().add(newSubfolder);
		folderMenu.getItems().add(newFolderNote);
		folderMenu.getItems().add(renameItem);
		folderMenu.getItems().add(deleteItem);

		/*
		eventHandler = e-> {
			if (e.getButton() == MouseButton.PRIMARY 
					&& e.getClickCount() == 2) {
				caller.openNote(getItem());
			}
		};
		*/

		/*
		this.setOnDragDetected(e -> {
			Dragboard db = this.startDragAndDrop( TransferMode.MOVE );
            ClipboardContent content = new ClipboardContent();
                content.put(myNotes, getItem());
                db.setContent( content );
                e.consume();
		});
		this.setOnDragOver( ( DragEvent event ) ->
		{
			Dragboard db = event.getDragboard();
			if ( db.hasContent(myNotes) && getItem().isFolder())
			{
				System.out.println("onDragOver:" + getItem());

				event.acceptTransferModes( TransferMode.MOVE );
			}
			event.consume();
		} );
		this.setOnDragEntered(( DragEvent event ) -> {
			
				 if (event.getGestureSource() != getItem() &&
						 event.getDragboard().hasContent(myNotes)
						 && getItem().isFolder()) {
					 this.setUnderline(true);
				 }
						
				 event.consume();
			}
		);
		this.setOnDragExited(( DragEvent event ) -> {
				 this.setUnderline(false);
				 event.consume();
			}
		);
		// Beim Ziel, hier muss dann verschoben werden ...
		this.setOnDragDropped(( DragEvent event ) -> {
			Dragboard db = event.getDragboard();
			Note source = (Note) db.getContent(myNotes);
			caller.move(source, getItem());
        	event.setDropCompleted(true);    
	        event.consume();
	   });
*/
	}

	
	
	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
	//	setStyle("-fx-control-inner-background: white;");		

		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			//setOnMouseClicked( null );
			setContextMenu(genericMenu);

		} else {
			if (item.isFolder()) {
				//Circle newRect = new Circle(10, Color.RED);
				final Polygon polygon = new Polygon();
				if (! item.getUuid().startsWith("BACKTOPARENT")) {
				polygon.getPoints().addAll(new Double[]{
					0.0, 0.0,
					20.0, 10.0,
					0.0, 20.0 });
				} else {
					polygon.getPoints().addAll(new Double[]{
						20.0, 0.0,
						0.0, 10.0,
						20.0, 20.0 });
				}
				polygon.setFill(Color.LIGHTBLUE);
                setGraphic(polygon);
				setContextMenu(folderMenu);

			} else { 
				setContextMenu(noteMenu);

			 }
			setText(item.getSubject());
			setGraphic(null);

			//setOnMouseClicked( eventHandler);
		}
	}
}
