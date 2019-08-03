package de.wesim.imapnotes.mainview;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.AccountChoiceDialog;
import de.wesim.imapnotes.mainview.components.EditorTab;
import de.wesim.imapnotes.mainview.components.PrefixedAlertBox;
import de.wesim.imapnotes.mainview.components.PrefixedTextInputDialog;
import de.wesim.imapnotes.mainview.components.outliner.OutlinerWidget;
import de.wesim.imapnotes.mainview.services.DeleteNoteTask;
import de.wesim.imapnotes.mainview.services.LoadNotesTask;
import de.wesim.imapnotes.mainview.services.MoveNoteTask;
import de.wesim.imapnotes.mainview.services.NewNoteTask;
import de.wesim.imapnotes.mainview.services.OpenFolderTask;
import de.wesim.imapnotes.mainview.services.OpenNoteTask;
import de.wesim.imapnotes.mainview.services.OpenPathTask;
import de.wesim.imapnotes.mainview.services.RenameNoteTask;
import de.wesim.imapnotes.mainview.services.SaveNoteTask;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.preferenceview.PreferenceView;
import de.wesim.imapnotes.services.ConfigurationService;
import de.wesim.imapnotes.services.FSNoteProvider;
import de.wesim.imapnotes.services.IMAPNoteProvider;
import de.wesim.imapnotes.services.INoteProvider;
import de.wesim.imapnotes.services.LuceneResult;
import de.wesim.imapnotes.services.LuceneService;
import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class MainViewController implements HasLogger {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LuceneService luceneService;

    @Autowired
    private Label account;

    @Autowired
    private Label status;

    @Autowired
    private TabPane tp;

    @Autowired
    private OutlinerWidget outlinerWidget;

    @Autowired
    private MenuItem reloadMenuTask;

    @Autowired
    private MenuItem exit;

    @Autowired
    private MenuItem update;

    @Autowired
    private MenuItem switchAccountMenuItem;

    @Autowired
    private MenuItem preferences;

    @Autowired
    private MenuItem find;

    @Autowired
    private MenuItem findGlobal;

    @Autowired
    private MenuItem about;

    @Autowired
    private MenuItem newNote;

    @Autowired
    private MenuItem newFolder;

    // these two fields must be injected
    // after the bean has been created
    private HostServices hostServices;
    private Stage stage;

    private INoteProvider backend;

    public MainViewController() {

    }

    @PostConstruct
    public void init() {
        this.refreshConfig();

        // Actions
        newNote.setOnAction(e -> {
            createNewMessage(false, null);
        });

        newFolder.setOnAction(e -> {
            createNewMessage(true, null);
        });

        switchAccountMenuItem.setOnAction(e -> {
            chooseAccount();
        });

        about.setOnAction(e -> {
            final Gson gson = new Gson();
            try {
                final Note aboutNote;
                aboutNote = gson.fromJson(new String(MainViewController.class.getResourceAsStream("/about.json").readAllBytes(),
                        "UTF-8"), Note.class);
                openEditor(aboutNote);

            } catch (JsonSyntaxException | IOException e1) {
                getLogger().error("Opening about.json failed.", e1);
            }

        });

        reloadMenuTask.setOnAction(e -> {
            triggerReload();
        });

        update.setOnAction(e -> {
            saveCurrentMessage();
        });

        exit.setOnAction(event -> {
            if (exitPossible()) {
                destroyBackend();
                configurationService.writeConfig();
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } else {
                getLogger().error("Quitting application not possible ...");
            }
        });

        preferences.setOnAction(e -> {
            final PreferenceView prefs = this.context.getBean(PreferenceView.class, stage);
            prefs.showAndWait();
            if (prefs.isPreferencesSaved()) {
                refreshConfig();
                startup();
            }
        });

        find.setOnAction(e -> {
            final PrefixedTextInputDialog dialog
                    = this.context.getBean(PrefixedTextInputDialog.class, "find");
            final Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return;
            }
            final String entered = result.get();
            final EditorTab et = (EditorTab) this.tp.getSelectionModel().getSelectedItem();
            et.markSearchItems(entered);
        });
        findGlobal.setOnAction(e -> {
            final PrefixedTextInputDialog dialog
                    = this.context.getBean(PrefixedTextInputDialog.class, "find");
            final Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return;
            }
            final String entered = result.get();
            var lastOpenedAccount = configurationService.getConfig().getLastOpenendAccount();
            final List<LuceneResult> luceneHits = luceneService.search(lastOpenedAccount, entered);
            getLogger().info("{}", luceneHits);
            if (luceneHits.isEmpty()) {
                return;
            }
            var matchingNote = luceneHits.get(0);
            if (luceneHits.size() > 1) {
                final ChoiceDialog<LuceneResult> folderChooser
                        = this.context.getBean(AccountChoiceDialog.class, "findglobal", luceneHits, luceneHits.get(0));

                var choice = folderChooser.showAndWait();
                if (!choice.isPresent()) {
                    return;
                }
                matchingNote = choice.get();
            }            
            final Note itemToFind = new Note(matchingNote.getUuid());
                
            Runnable callback = () -> {
                var foundItem = OutlinerWidget.searchTreeItem(itemToFind, 
                        this.outlinerWidget.getRoot());
                if (foundItem != null) {
                    this.outlinerWidget.getSelectionModel().select(foundItem);
                }
                
            };        
                
            OpenPathTask opt = context.getBean(OpenPathTask.class, outlinerWidget.getRoot(),
                    matchingNote.getPath(), callback);
            opt.run();
        });
    }

    public void triggerReload() {
        if (closeAccount()) {
            loadNotes();
        }
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void refreshConfig() {
        configurationService.refresh();
    }

    public void chooseAccount() {
        final List<Account> availableAccounts = this.configurationService.getConfig().getAccountList();
        if (availableAccounts.size() < 2) {
            return;
        }
        final ChoiceDialog<Account> cd
                = this.context.getBean(AccountChoiceDialog.class, "account", availableAccounts, availableAccounts.get(0));
        final Optional<Account> result = cd.showAndWait();
        if (result.isPresent()) {
            openAccount(result.get());
        }
    }

    public boolean closeAccount() {
        if (!exitPossible()) {
            return false;
        }
        this.tp.getTabs().clear();
        destroyBackend();
        return true;
    }

    private void openAccount(Account first) {
        if (!closeAccount()) {
            return;
        }
        // currently, still hard-coded ...
        if (first.getType() == Account_Type.FS) {
            this.backend = context.getBean(FSNoteProvider.class);
        } else {
            this.backend = context.getBean(IMAPNoteProvider.class);
        }
        try {
            this.backend.init(first);
        } catch (Exception e) {
            getLogger().error("Initializing note backend for account {} failed.", e);
            status.textProperty().unbind();
            status.setText(e.getLocalizedMessage());
            return;
        }
        final String account_name = first.getAccount_name();
        this.account.setText(account_name);
        this.configurationService.getConfig().setLastOpenendAccount(account_name);
        loadNotes();
    }

    public void openEditor(final Note openedNote) {
        final Tab editorTab = context.getBean(EditorTab.class, openedNote);
        tp.getTabs().add(editorTab);
        tp.getSelectionModel().select(editorTab);
    }

    public void startup() {
        final Configuration config = configurationService.getConfig();
        final String lastOpenedAccount = config.getLastOpenendAccount();

        Account firstAccount = null;
        if (!config.getAccountList().isEmpty()) {
            firstAccount = config.getAccountList().get(0);
        }

        if (lastOpenedAccount != null) {
            for (Account account : config.getAccountList()) {
                if (account.getAccount_name().equals(lastOpenedAccount)) {
                    firstAccount = account;
                    break;
                }
            }
        }
        if (firstAccount == null) {
            getLogger().warn("No account available for opening at startup.");
            return;
        }
        openAccount(firstAccount);
    }

    public void move(Note msg, TreeItem<Note> target) {
        // find the tree item with the note to be removed
        final TreeItem<Note> foundTreeItem = OutlinerWidget.searchTreeItem(msg, this.outlinerWidget.getRoot());
        if (foundTreeItem == null) {
            // this should never happen ...
            getLogger().error("Unable to find {} in outliner.", msg.toString());
            return;
        }
        final MoveNoteTask moveNoteTask
                = context.getBean(MoveNoteTask.class, foundTreeItem, target);
        moveNoteTask.run();
    }

    public void deleteNote(TreeItem<Note> treeItem, boolean dontTask) {
        final Note deleteItem = treeItem.getValue();
        if (!dontTask) {
            final PrefixedAlertBox alert = context.getBean(PrefixedAlertBox.class, "really_delete", deleteItem.getSubject());
            alert.setAlertType(Alert.AlertType.CONFIRMATION);
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                return;
            }
        }
        final DeleteNoteTask deleteMessageTask = context.getBean(DeleteNoteTask.class, treeItem);
        deleteMessageTask.run();
    }

    public void renameCurrentMessage(Note note) {
        final Dialog<String> dialog = context.getBean(PrefixedTextInputDialog.class, "rename", note);
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }

        final RenameNoteTask renameNoteService = context.getBean(RenameNoteTask.class, note, result.get());
        renameNoteService.run();
    }

    public void loadNotes() {
        getLogger().info("Loading notes ...");
        LoadNotesTask newLoadTask = context.getBean(LoadNotesTask.class);
        newLoadTask.run();
    }

    private Optional<ButtonType> demandConfirmation() {
        final Alert alert = context.getBean(PrefixedAlertBox.class, "really_quit");
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        return alert.showAndWait();
    }

    // Called when selecting an instance of ListViewItem
    public void openNote(Note note) {
        if (note == null) {
            return;
        }

        if (note.isFolder()) {
            getLogger().warn("Opening folders like this not supported, yet.");
            return;
        }

        // Böse, aber funktioniert ...
        for (Tab t : this.tp.getTabs()) {
            EditorTab et = (EditorTab) t;
            if (et.getNote().equals(note)) {
                this.tp.getSelectionModel().select(t);
                return;
            }
        }
        getLogger().debug("Opening {}", note.getSubject());

        final OpenNoteTask newOpenMessageTask = context.getBean(OpenNoteTask.class, note);
        newOpenMessageTask.run();
    }

    // Needed by OutlinerItemChangeListener
    public void openFolder(TreeItem<Note> m) {
        if (m == null) {
            return;
        }
        getLogger().debug("Opening Folder {}", m.getValue().getSubject());

        final OpenFolderTask openFolderTask = context.getBean(OpenFolderTask.class, m);
        openFolderTask.run();

    }

    public void createNewMessage(boolean createFolder, TreeItem<Note> parent) {
        final Dialog<String> dialog;
        if (createFolder) {
            dialog = context.getBean(PrefixedTextInputDialog.class, "new_folder");
        } else {
            dialog = context.getBean(PrefixedTextInputDialog.class, "new_note");
        }
        final Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }
        final NewNoteTask newNoteService = context.getBean(NewNoteTask.class, parent, result.get(), createFolder);
        newNoteService.run();
    }

    public void saveCurrentMessage() {
        final EditorTab et = (EditorTab) this.tp.getSelectionModel().getSelectedItem();
        final String newContent = et.getQe().getHtmlText();
        getLogger().debug("Saving new content: {}", newContent);

        et.getNote().setContent(newContent);
        final SaveNoteTask saveTask = context.getBean(SaveNoteTask.class, et);
        saveTask.run();
    }

    public boolean exitPossible() {
        boolean noUnsavedChanges = true;
        for (Tab t : this.tp.getTabs()) {
            EditorTab et = (EditorTab) t;
            noUnsavedChanges = noUnsavedChanges && !(et.getQe().getContentUpdate());

        }
        if (noUnsavedChanges) {
            return true;
        }
        final Optional<ButtonType> result = demandConfirmation();
        return (result.isPresent() && result.get() == ButtonType.OK);
    }

    public void destroyBackend() {
        if (this.backend != null) {
            try {
                this.backend.destroy();
            } catch (Exception e) {
                getLogger().error("Destroying the backend has failed ...", e);
            }
        }
    }

    public INoteProvider getBackend() {
        return this.backend;
    }

    public void closeTab(Note deletedNote) {
        Iterator<Tab> tabIter = this.tp.getTabs().iterator();
        while (tabIter.hasNext()) {
            final EditorTab et = (EditorTab) tabIter.next();
            if (et.getNote().equals(deletedNote)) {
                tabIter.remove();
            }
        }
    }

    public void removeTreeItem(TreeItem<Note> treeItem) {
        // Does this make sense?
        this.outlinerWidget.getSelectionModel().clearSelection();
        final TreeItem<Note> parentNote = treeItem.getParent();
        final Note deletedNote = treeItem.getValue();
        closeTab(deletedNote);
        final int index = parentNote.getChildren().indexOf(treeItem);

        parentNote.getChildren().remove(treeItem);
        // TODO Check, whether this makes sense

//        final int previousItem = Math.max(0, index - 1);
//        if (parentNote.getChildren().isEmpty()) {
//            return;
//        }
        //final TreeItem<Note> previous = parentNote.getChildren().get(previousItem);
        // openNote(previous.getValue());
    }

    public void move(Note item) {
        // available target folders for moving action
        var nodes = this.outlinerWidget.getFlatList();

        final ChoiceDialog<String> folderChooser
                = this.context.getBean(AccountChoiceDialog.class, "move", nodes.keySet(), "/");

        var choice = folderChooser.showAndWait();
        if (!choice.isPresent()) {
            return;
        }
        var chosenPath = choice.get();
        getLogger().info("Chosen path: {}", chosenPath);
        getLogger().info("Chosen path in der Map: {}", nodes.get(chosenPath));
        final String pathOfTarget;
        if (chosenPath.equals("/")) {
            pathOfTarget = "/";
        } else {
            var assignedNote = nodes.get(chosenPath).getValue() ;
            pathOfTarget = OutlinerWidget.determinePath(assignedNote, this.outlinerWidget.getRoot(), "")
                + assignedNote.getUuid() + "/";
            getLogger().info("DEtermined path: {}", pathOfTarget);
        }

        Runnable callback = () -> move(item, nodes.get(chosenPath));        
        OpenPathTask opt = context.getBean(OpenPathTask.class, outlinerWidget.getRoot(),
                    pathOfTarget, callback);
        opt.run();        
    }

    public String getCurrentAccount() {
        return this.configurationService.getConfig().getLastOpenendAccount();
    }

}
