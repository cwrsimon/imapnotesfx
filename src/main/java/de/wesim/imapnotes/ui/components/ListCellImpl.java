
package de.wesim.imapnotes.ui.components;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class ListCellImpl extends ListCell<Note> {

    private static final DataFormat myNotes = new DataFormat("de.wesim.imapnotes.models.Note");

	private NoteController caller;
	private final ContextMenu addMenu = new ContextMenu();
	private final ContextMenu newMenu = new ContextMenu();

	private EventHandler<? super MouseEvent> eventHandler;
	public ListCellImpl (NoteController caller) {
		final MenuItem deleteItem = new MenuItem("Delete");
		addMenu.getItems().add(deleteItem);
		deleteItem.setOnAction(e -> {
			caller.deleteCurrentMessage(getItem(), false);
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
			/* the drag-and-drop gesture entered the target */
			/* show to the user that it is an actual gesture target */
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
			/* data dropped */
    	    /* if there is a string data on dragboard, read it and use it */
			Dragboard db = event.getDragboard();
			Note source = (Note) db.getContent(myNotes);
			caller.move(source, getItem());
        	/* let the source know whether the string was successfully 
         	 * transferred and used */
        	event.setDropCompleted(true);    
	        event.consume();
	   });
	//    this.setOnDragDone(( DragEvent event ) -> {
	// 	event.consume();
   	// 	}
	//);
	}

	
	
	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
	//	setStyle("-fx-control-inner-background: white;");		

		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			setOnMouseClicked( null );
			setContextMenu(newMenu);

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

			} else { 

			}
			setText(item.getSubject());
			setContextMenu(addMenu);

			setOnMouseClicked( eventHandler);
		}
	}
}
