package de.wesim.imapnotes.mainview.components.outliner;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.I18NService;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

@Component
@Scope("prototype")
public class OutlinerTreeCell extends TreeCell<Note> implements HasLogger {

    @Autowired
    private I18NService i18N;

    private static final DataFormat NOTE_FORMAT = new DataFormat("de.wesim.imapnotes.models.Note");
    private final ContextMenu noteMenu = new ContextMenu();
    private final ContextMenu genericMenu = new ContextMenu();
    private final ContextMenu folderMenu = new ContextMenu();

    private final MainViewController caller;

    @Autowired
    public OutlinerTreeCell(MainViewController caller) {
        this.caller = caller;
    }

    @PostConstruct
    public void init() {
        final MenuItem moveNoteItem = new MenuItem(i18N.getTranslation("move_note_context_menu_item"));
        moveNoteItem.setOnAction(e -> {
            caller.move(getItem());
        });
        final MenuItem moveNoteToRootItem = new MenuItem(i18N.getTranslation("move_to_root_context_menu_item"));
        moveNoteToRootItem.setOnAction(e -> {
            caller.move(getItem(), getTreeView().getRoot());
        });
        final MenuItem deleteItem = new MenuItem(i18N.getTranslation("delete_context_menu_item"));
        deleteItem.setOnAction(e -> {
            caller.deleteNote(getTreeItem(), false);
        });
        final MenuItem renameItem = new MenuItem(i18N.getTranslation("rename_context_menu_item"));
        renameItem.setOnAction(e -> {
            caller.renameCurrentMessage(getItem());
        });

        final MenuItem delete2 = new MenuItem(i18N.getTranslation("delete_msg_context_menu_item"));
        final MenuItem renameNote = new MenuItem(i18N.getTranslation("rename_msg_context_menu_item"));
        noteMenu.getItems().add(renameNote);
        noteMenu.getItems().add(delete2);
        noteMenu.getItems().add(moveNoteItem);
        noteMenu.getItems().add(moveNoteToRootItem);

        delete2.setOnAction(e -> {
            caller.deleteNote(getTreeItem(), false);
        });
        renameNote.setOnAction(e -> {
            caller.renameCurrentMessage(getItem());
        });

        final MenuItem newItem = new MenuItem(i18N.getTranslation("new_note_context_menu_item"));
        newItem.setOnAction(e -> {
            caller.createNewMessage(false, null);
        });

        final MenuItem newFolder = new MenuItem(i18N.getTranslation("new_folder_context_menu_item"));
        newFolder.setOnAction(e -> {
            caller.createNewMessage(true, null);
        });

        genericMenu.getItems().add(newItem);
        genericMenu.getItems().add(newFolder);

        final MenuItem newSubfolder = new MenuItem(i18N.getTranslation("add_note_context_menu_item"));
        final MenuItem newFolderNote = new MenuItem(i18N.getTranslation("add_folder_context_menu_item"));
        newFolderNote.setOnAction(e -> {
            caller.createNewMessage(true, getTreeItem());
        });
        newSubfolder.setOnAction(e -> {
            caller.createNewMessage(false, getTreeItem());
        });
        folderMenu.getItems().add(newSubfolder);
        folderMenu.getItems().add(newFolderNote);
        folderMenu.getItems().add(renameItem);
        folderMenu.getItems().add(deleteItem);

        // all necessary for moving notes ...
        this.setOnDragDetected(e -> {
            final Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(NOTE_FORMAT, getItem());
            db.setContent(content);
            e.consume();
            getLogger().debug("Drag Detected");
        });
        this.setOnDragOver((DragEvent event) -> {
            getLogger().info("Drag Over:" + String.valueOf(getItem()));

            final Dragboard db = event.getDragboard();
            if (db.hasContent(NOTE_FORMAT)
                    && getItem() != null
                    && getItem().isFolder()) {
                getLogger().info("onDragOver:" + getItem());
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        this.setOnDragEntered((DragEvent event) -> {
            //getLogger().debug("Drag Entered: {}", event.getGestureTarget());

            if (event.getGestureSource() != getItem()
                    && getItem() != null
                    && event.getDragboard().hasContent(NOTE_FORMAT)
                    && getItem().isFolder()) {
                this.setTextFill(Color.RED);
            }
            event.consume();
        });
        this.setOnDragExited((DragEvent event) -> {
            //getLogger().debug("setOnDragExited");
            this.setTextFill(Color.BLACK);
            event.consume();
        });

        this.setOnDragDropped((DragEvent event) -> {
            //getLogger().debug("setOnDragDropped");
            final Dragboard db = event.getDragboard();
            final Note source = (Note) db.getContent(NOTE_FORMAT);
            caller.move(source, getTreeItem());
            event.setDropCompleted(true);
            event.consume();
        });
        this.setOnMouseClicked(e -> {
            //getLogger().info("Maus geklickt: {} auf {}", e.getClickCount(), getItem().getSubject());
            if (e.getClickCount() == 2 && getItem().isFolder()) {
                caller.openFolder(getTreeItem());
            }
        });
    }

    @Override
    public void updateItem(Note item, boolean empty) {
        super.updateItem(item, empty);
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
