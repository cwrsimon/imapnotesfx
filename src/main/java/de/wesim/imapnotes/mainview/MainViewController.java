package de.wesim.imapnotes.mainview;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.AccountChoiceDialog;
import de.wesim.imapnotes.mainview.components.EditorTab;
import de.wesim.imapnotes.mainview.components.PrefixedAlertBox;
import de.wesim.imapnotes.mainview.components.PrefixedTextInputDialog;
import de.wesim.imapnotes.mainview.components.outliner.MyListView;
import de.wesim.imapnotes.mainview.services.DeleteNoteTask;
import de.wesim.imapnotes.mainview.services.LoadNotesTask;
import de.wesim.imapnotes.mainview.services.MoveNoteTask;
import de.wesim.imapnotes.mainview.services.NewNoteTask;
import de.wesim.imapnotes.mainview.services.OpenFolderTask;
import de.wesim.imapnotes.mainview.services.OpenNoteTask;
import de.wesim.imapnotes.mainview.services.RenameNoteTask;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.preferenceview.Preferences;
import de.wesim.imapnotes.services.ConfigurationService;
import de.wesim.imapnotes.services.FSNoteProvider;
import de.wesim.imapnotes.services.IMAPNoteProvider;
import de.wesim.imapnotes.services.INoteProvider;
import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class MainViewController implements HasLogger {

    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    @Qualifier("p1")
    private ProgressBar progressBar;

    @Autowired
    private Label account;

    @Autowired
    private Label status;

    @Autowired
    private TabPane tp;

    @Autowired
    @Qualifier("myListView")
    private TreeView<Note> noteCB;

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
    private MenuItem about;
    
    @Autowired
	private MenuItem newNote;

    @Autowired
	private MenuItem newFolder;
        
    // these fields cannot be autowired, but must be set manually
    // don't ask ...
    private HostServices hostServices;
    private Stage stage;
    private INoteProvider backend;

    private Configuration config;
    
    public MainViewController() {

    }

    @PostConstruct
    public void init() {
        this.refreshConfig();
        
        // Actions
        newNote.setOnAction( e-> {
			createNewMessage(false, null);                
        });
        
        newFolder.setOnAction( e-> {
			createNewMessage(true, null);                
        });

        switchAccountMenuItem.setOnAction(e -> {
            chooseAccount();
        });
        
		about.setOnAction( e-> {
			// TODO Find a better solution
			final PrefixedAlertBox aboutBox = context.getBean(PrefixedAlertBox.class, "about");
			aboutBox.showAndWait();
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
                configurationService.writeConfig(config);
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } else {
                getLogger().error("Quitting application not possible ...");
            }
        });

        preferences.setOnAction(e -> {
            final Preferences prefs = new Preferences(stage);
//            final Stage newStage = new Stage();
//            newStage.initModality(Modality.APPLICATION_MODAL);
//            newStage.setHeight(500);
//            newStage.setWidth(600);
//            // TODO !!!
//            newStage.setScene(prefs.getScene());
//            prefs.getCancelButton().setOnAction(e2 -> {
//                newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
//            });
//            prefs.getApplyButton().setOnAction(e2 -> {
//                prefs.savePreferences();
//                newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
//                refreshConfig();
//                startup();
//            });
            prefs.showAndWait();
            // TODO Rückgabe abfangen
            refreshConfig();
            startup();
        });
        
        find.setOnAction( e-> {
        	final PrefixedTextInputDialog dialog = 
        			this.context.getBean(PrefixedTextInputDialog.class, "find");
        	final Optional<String> result = dialog.showAndWait();
        	if (!result.isPresent()) return;
            final String entered = result.get();
            final EditorTab et = (EditorTab) this.tp.getSelectionModel().getSelectedItem();
            et.getQe().findString(entered);
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
        this.config = configurationService.readConfig();
    }

    public void chooseAccount() {
        final List<Account> availableAccounts = this.config.getAccountList();
        if (availableAccounts.size() < 2) return;
        final ChoiceDialog<Account> cd = 
            this.context.getBean(AccountChoiceDialog.class, availableAccounts);
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
        // TODO use Spring tools
        if (first.getType() == Account_Type.FS) {
            this.backend = new FSNoteProvider();
        } else {
            this.backend = new IMAPNoteProvider();
        }
        try {
            this.backend.init(first);
        } catch (Exception e) {
            getLogger().error("Initializing note backend for account {} failed.", e);
            status.setText(e.getLocalizedMessage());
            return;
        }
        final String account_name = first.getAccount_name();
		this.account.setText(account_name);
        this.config.setLastOpenendAccount(account_name);
        loadNotes();
    }

    public void openEditor(final Note openedNote) {
        final Tab editorTab = new EditorTab(this, openedNote);
        tp.getTabs().add(editorTab);
        tp.getSelectionModel().select(editorTab);
    }

    public void startup() {
        final String lastOpenedAccount = config.getLastOpenendAccount();

        Account firstAccount = null;
        if (!this.config.getAccountList().isEmpty()) {
            firstAccount = this.config.getAccountList().get(0);
        }

        if (lastOpenedAccount != null) {
            for (Account account : this.config.getAccountList()) {
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
        final TreeItem<Note> foundTreeItem = MyListView.searchTreeItem(msg, this.noteCB.getRoot());
        getLogger().info("Moving source {} ", foundTreeItem);
        getLogger().info("Moving target {} ", target);

        // TODO what if foudnTreeItem == null?
        final MoveNoteTask moveNoteTask = 
        		context.getBean(MoveNoteTask.class, foundTreeItem, target);
        moveNoteTask.run();
    }

    public void deleteNote(TreeItem<Note> treeItem, boolean dontTask) {
        final Note deleteItem = treeItem.getValue();
        if (!dontTask) {
            final PrefixedAlertBox alert = context.getBean(PrefixedAlertBox.class, "really_delete", deleteItem.toString());
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
        final Dialog<String> dialog = context.getBean(PrefixedTextInputDialog.class, "rename", note.toString());
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

        getLogger().info("Opening {}", note.getSubject());

        final OpenNoteTask newOpenMessageTask = context.getBean(OpenNoteTask.class, note);
        newOpenMessageTask.run();
        
    }

    // Aufgerufen beim Klick aufs ListViewItem
    public void openFolder(TreeItem<Note> m) {
        if (m == null) {
            return;
        }
        // TODO Brauchen wir das noch ????
        // Böse, aber funktioniert ...
        for (Tab t : this.tp.getTabs()) {
            final EditorTab et = (EditorTab) t;
            // TODO
            if (et.getNote().equals(m)) {
                this.tp.getSelectionModel().select(t);
                return;
            }
        }

        getLogger().info("Opening Folder {}", m.getValue().getSubject());

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
        if (!result.isPresent()) return;
        final NewNoteTask newNoteService = context.getBean(NewNoteTask.class, parent, result.get(), createFolder);
        newNoteService.run();
    }

    public void saveCurrentMessage() {
        final EditorTab et = (EditorTab) this.tp.getSelectionModel().getSelectedItem();
        final String newContent = et.getQe().getHtmlText();
        getLogger().info("Saving new content: {}", newContent);

        et.getNote().setContent(newContent);
        et.saveContents();
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

    // TODO make unnecessary
    public Configuration getConfiguration() {
        return this.config;
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
    	final TreeItem<Note> parentNote = treeItem.getParent();
		final Note deletedNote = treeItem.getValue();
		closeTab(deletedNote);
		final int index = parentNote.getChildren().indexOf(treeItem);

		parentNote.getChildren().remove(treeItem);

		final int previousItem = Math.max(0, index - 1);
		if (parentNote.getChildren().isEmpty()) return;
		final TreeItem<Note> previous = parentNote.getChildren().get(previousItem);
		openNote(previous.getValue());
    }
    
    public void addNoteToTree(TreeItem<Note> treeItem, Note newNote) {
        // FIXME
        // Das alles nach ListView verschieben ...
        final TreeItem<Note> newTreeItem = new TreeItem<Note>(newNote);
        if (newNote.isFolder()) {
            if (MyListView.isEmptyTreeItem(newTreeItem)) {
                newTreeItem.getChildren().clear();
            }
            newTreeItem.getChildren().add(new TreeItem<Note>(null));
        }
        if (treeItem != null) {
            if (MyListView.isEmptyTreeItem(treeItem)) {
            	treeItem.getChildren().clear();
            }
            treeItem.getChildren().add(newTreeItem);
        } else {
            this.noteCB.getRoot().getChildren().add(newTreeItem);
        }
        openNote(newNote);
    }
}
