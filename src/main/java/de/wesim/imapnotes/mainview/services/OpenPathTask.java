package de.wesim.imapnotes.mainview.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.models.Note;
import java.util.ArrayDeque;
import java.util.Deque;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.springframework.context.ApplicationContext;

@Component
@Scope("prototype")
public class OpenPathTask extends AbstractNoteTask<ObservableList<Note>> {

    private Note searchItem;

    private class UnknownNoteException extends Exception {

    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OutlinerWidget outlinerWidget;

    private Runnable callbackFunction;
    private TreeItem<Note> subPathItem;
    private final Deque<String> subPaths;

    // TODO Besser dokumentieren
    // z.B: /Notes.Papa/Notes.Papa.Bastelprojekte/ 
    public OpenPathTask(TreeItem<Note> baseNode, String path, Note searchItem, Runnable callback) {
        this(baseNode, getPathElements(path), searchItem, callback);
    }

    private static Deque<String> getPathElements(String path) {
        var pathItems = path.split("/");
        var paths = new ArrayDeque<String>();
        for (String p : pathItems) {
            if (p.isEmpty()) {
                continue;
            }
            paths.add(p);
        }
        return paths;
    }

    public OpenPathTask(TreeItem<Note> baseNode, Deque<String> subPaths, Note searchItem, Runnable callback) {
        super();
        this.searchItem = searchItem;
        this.subPaths = subPaths;
        this.subPathItem = baseNode;
        this.callbackFunction = callback;
        if (!subPaths.isEmpty()) {
            var first = subPaths.removeFirst();
            var next = new Note(first);
            this.subPathItem = findSubpathItem(baseNode, next);            
        }
    }

    private TreeItem<Note> findSubpathItem(TreeItem<Note> baseNode, Note searchNote) {
        if (searchNote == null) return null;
        // suchen ...
        for (TreeItem<Note> child : baseNode.getChildren()) {
            Note childNote = child.getValue();
            if (childNote.equals(searchNote)) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        final ObservableList<Note> loadedItems = getValue();
        Platform.runLater(() -> {
            if (loadedItems != null) {
                this.outlinerWidget.addChildrenToNode(loadedItems, subPathItem);
            }
            subPathItem.setExpanded(true);
            // TODO Rekursive Aufrufe mit dem restlichen Subpath
            if (!this.subPaths.isEmpty()) {
                OpenPathTask newPathTask = context.getBean(OpenPathTask.class, this.subPathItem, this.subPaths, this.searchItem, this.callbackFunction);
                newPathTask.run();
            } else {
                var foundItem = findSubpathItem(subPathItem, searchItem);
                if (foundItem != null) {
                    this.outlinerWidget.getSelectionModel().select(foundItem);
                }
                this.callbackFunction.run();
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
