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

// TODO Überarbeiten!!!
@Component
@Scope("prototype")
public class OpenPathTask extends AbstractNoteTask<ObservableList<Note>> {

    private class UnknownNoteException extends Exception {

    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OutlinerWidget outlinerWidget;

    private Runnable callbackFunction;
    private TreeItem<Note> baseNode;
    private final Deque<String> pathElements;

    // TODO Besser dokumentieren
    // z.B: /Notes.Papa/Notes.Papa.Bastelprojekte/ 
    public OpenPathTask(TreeItem<Note> baseNode, String path, Runnable callback) {
        this(baseNode, getPathElements(path), callback);
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

    public OpenPathTask(TreeItem<Note> baseNode, Deque<String> pathElements, Runnable callback) {
        super();
        this.pathElements = pathElements;
        this.baseNode = baseNode;
        this.callbackFunction = callback;
        if (!pathElements.isEmpty()) {
            var first = pathElements.removeFirst();
            var next = new Note(first);
            this.baseNode = findSubpathItem(baseNode, next);            
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
                this.outlinerWidget.addChildrenToNode(loadedItems, baseNode);
            }
            baseNode.setExpanded(true);
            // TODO Rekursive Aufrufe mit dem restlichen Subpath
            if (!this.pathElements.isEmpty()) {
                OpenPathTask newPathTask = context.getBean(OpenPathTask.class, this.baseNode, this.pathElements, this.callbackFunction);
                newPathTask.run();
            } else {
                if (this.callbackFunction != null) {
                    this.callbackFunction.run();
                }
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
        if (this.baseNode == null) {
            throw new UnknownNoteException();
        }

        if (this.baseNode.getChildren().size() == 1) {
            // make sure, the folder wasn't opened before
            var childchild = this.baseNode.getChildren().get(0);
            if (childchild.getValue() == null) {
                final Note folderToOpen = baseNode.getValue();

                final List<Note> messages = mainViewController.getBackend().getNotesFromFolder(folderToOpen);

                return FXCollections.observableArrayList(messages);
            }
        }
        return null;
    }

}
