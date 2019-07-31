package de.wesim.imapnotes.mainview.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.models.Note;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

// TODO Wegreduzieren zugunsten von OpenPathTask
@Component
@Scope("prototype")
public class OpenFolderTask extends AbstractNoteTask<ObservableList<Note>> {

    @Autowired
    private OutlinerWidget outlinerWidget;

    private final TreeItem<Note> folderTreeItem;

    public OpenFolderTask(TreeItem<Note> folderTreeItem) {
        super();
        this.folderTreeItem = folderTreeItem;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        final ObservableList<Note> loadedItems = getValue();
        Platform.runLater(()
                -> this.outlinerWidget.addChildrenToNode(loadedItems, folderTreeItem)
        );
    }

    @Override
    public String getActionName() {
        return "Open Folder";
    }

    @Override
    public String getSuccessMessage() {
        return i18N.getMessageAndTranslation("user_folder_finished_opening",
                this.folderTreeItem.getValue().getSubject());
    }

    @Override
    public String getRunningMessage() {
        return i18N.getMessageAndTranslation("user_folder_start_opening",
                this.folderTreeItem.getValue().getSubject());
    }

    @Override
    protected ObservableList<Note> call() throws Exception {
        final Note folderToOpen = folderTreeItem.getValue();
        final List<Note> messages = mainViewController.getBackend().getNotesFromFolder(folderToOpen);
        return FXCollections.observableArrayList(messages);
    }
}
