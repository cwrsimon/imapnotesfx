package de.wesim.imapnotes.mainview.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.models.Note;
import java.util.Deque;
import java.util.Stack;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.springframework.context.ApplicationContext;

@Component
@Scope("prototype")
public class OpenPathTask extends AbstractNoteTask<ObservableList<Note>> {

    private class UnknownNoteException extends Exception {
  
    }
    
    @Autowired
    private ApplicationContext context;

    @Autowired
    private OutlinerWidget outlinerWidget;

    private TreeItem<Note> subPathItem;
    private final Deque<String> subPaths;

    public OpenPathTask(TreeItem<Note> baseNode, Deque<String> subPaths) {
        super();
        this.subPaths = subPaths;
        // TODO Transformiere
        String first = subPaths.removeFirst();
        Note searchItem = new Note(first);
        // suchen ...
        for (TreeItem<Note> child : baseNode.getChildren()) {
            Note childNote = child.getValue();
            if (childNote.equals(searchItem) && childNote.isFolder()) {
                subPathItem = child;
                break;

                }
            }
        }
    

    @Override
    protected void succeeded() {
        super.succeeded();
        final ObservableList<Note> loadedItems = getValue();
        if (loadedItems == null) return;
        Platform.runLater(() -> {
            this.outlinerWidget.addChildrenToNode(loadedItems, subPathItem);
            subPathItem.setExpanded(true);
            // TODO Rekursive Aufrufe mit dem restlichen Subpath
            if (!this.subPaths.isEmpty()) {
                OpenPathTask newPathTask = context.getBean(OpenPathTask.class, subPathItem, this.subPaths);
                newPathTask.run();
            }
        }
        );
    }

    @Override
    public String getActionName() {
        return "Open Folder";
    }

    @Override
    public String getSuccessMessage() {
        return i18N.getMessageAndTranslation("user_folder_finished_opening", "BLA");

    }

    @Override
    public String getRunningMessage() {
        return i18N.getMessageAndTranslation("user_folder_start_opening", "BLA");
    }

    @Override
    protected ObservableList<Note> call() throws Exception {
        // TODO UNter root das TreeItem für den SUbpath
        // lokalisieren und dann laden
        // TODO wenn schon geladen ist, dann einfach mit Suceed weitermachen
        //final TreeItem<Note> subPathItem = ...;
        if (this.subPathItem == null) {
            throw new UnknownNoteException();
        }
       
        if (this.subPathItem.getChildren().size() == 1) {
            var childchild = this.subPathItem.getChildren().get(0);
            if (childchild.getValue() == null) {
                final Note folderToOpen = subPathItem.getValue();
                
                final List<Note> messages = mainViewController.getBackend().getNotesFromFolder(folderToOpen);

                return FXCollections.observableArrayList(messages);
            }
        }
        return null;
    }

}