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

    private class UnknownNoteException extends Exception {

    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OutlinerWidget outlinerWidget;

    private Runnable callbackFunction;
    private TreeItem<Note> nodeToOpen;
    private final Deque<String> remainingPath;

    // Opens a path like these:
    // /Notes.Papa/Notes.Papa.Bastelprojekte/ 
    public OpenPathTask(TreeItem<Note> baseNode, String path, Runnable callback) {
        this(baseNode, getPathElements(path), callback);
    }

    private static Deque<String> getPathElements(String path) {
        var paths = new ArrayDeque<String>();
        if (path == null) {
            return paths;
        }
        for (String p : path.split("/")) {
            if (p.isEmpty()) {
                continue;
            }
            paths.add(p);
        }
        return paths;
    }

    public OpenPathTask(TreeItem<Note> nodeToOpen, Deque<String> remainingPath, Runnable callback) {
        super();
        getLogger().info("Incoming: {}, {}", nodeToOpen.getValue() != null ? nodeToOpen.getValue().getSubject() : "root", remainingPath);
        this.remainingPath = remainingPath;
        this.nodeToOpen = nodeToOpen;
        this.callbackFunction = callback;
    }

    private TreeItem<Note> findSubpathItem(TreeItem<Note> baseNode, Note searchNote) {
        if (searchNote == null) {
            return null;
        }
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
                this.outlinerWidget.addChildrenToNode(loadedItems, nodeToOpen);
            }
            nodeToOpen.setExpanded(true);
            if (!remainingPath.isEmpty()) {
                var nextPathElement = remainingPath.removeFirst();
                var nextNoteFolder = new Note(nextPathElement);
                var nextTreeNode = findSubpathItem(nodeToOpen, nextNoteFolder);

                OpenPathTask newPathTask = context.getBean(OpenPathTask.class, nextTreeNode, this.remainingPath, this.callbackFunction);
                newPathTask.run();
            } else {
                if (this.callbackFunction != null) {
                    this.callbackFunction.run();
                }
            }
        });
    }

    @Override
    public String getActionName() {
        return "Open Folder";
    }

    @Override
    public String getSuccessMessage() {
        return i18N.getMessageAndTranslation("user_folder_finished_opening",
                this.nodeToOpen.getValue() != null ? this.nodeToOpen.getValue().getSubject() : "ROOT");
    }

    @Override
    public String getRunningMessage() {
        return i18N.getMessageAndTranslation("user_folder_start_opening",
                this.nodeToOpen.getValue() != null ? this.nodeToOpen.getValue().getSubject() : "ROOT");
    }

    @Override
    protected ObservableList<Note> call() throws Exception {
        if (this.nodeToOpen == null) {
            throw new UnknownNoteException();
        }

        if (this.nodeToOpen.getChildren().size() > 1) {
            return null;
        }
        // make sure, the folder wasn't opened before
        if (!this.nodeToOpen.getChildren().isEmpty()
                && this.nodeToOpen.getChildren().get(0).getValue() != null) {
            return null;
        }
        final Note folderToOpen = nodeToOpen.getValue();

        final List<Note> messages;
        if (folderToOpen != null) {
            messages = mainViewController.getBackend().getNotesFromFolder(folderToOpen);
        } else {
            messages = mainViewController.getBackend().getNotes();
        }
        return FXCollections.observableArrayList(messages);
    }
}
