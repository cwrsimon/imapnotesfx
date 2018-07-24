package de.wesim.imapnotes.mainview;

import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.EditorTab;
import de.wesim.imapnotes.mainview.components.outliner.MyListView;
import de.wesim.imapnotes.mainview.services.DeleteMessageTask;
import de.wesim.imapnotes.mainview.services.LoadMessageTask;
import de.wesim.imapnotes.mainview.services.MoveNoteService;
import de.wesim.imapnotes.mainview.services.NewNoteService;
import de.wesim.imapnotes.mainview.services.OpenFolderTask;
import de.wesim.imapnotes.mainview.services.OpenMessageTask;
import de.wesim.imapnotes.mainview.services.RenameNoteService;
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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
public class MainViewController implements HasLogger {
	
	private INoteProvider backend;
		
	@Autowired
	private MoveNoteService moveNoteService;
	
	@Autowired
	private NewNoteService newNoteService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	@Qualifier("p1")
	private ProgressBar progressBar;
	
	@Autowired
	private Label account;

	public BooleanBinding allRunning;
	
	@Autowired
	private OpenMessageTask openMessageTask;

	@Autowired
	private DeleteMessageTask deleteNoteService;

	@Autowired
	private RenameNoteService renameNoteService;

	@Autowired
	private LoadMessageTask newLoadTask;

	@Autowired
	private OpenFolderTask openFolderTask;

	public StringProperty currentAccount = new SimpleStringProperty("");

	private Configuration config;

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
	
	// must be set manually
	private HostServices hostServices;
	private Stage stage;
	
	
	public MainViewController() {

	}

	@PostConstruct
	public void init() {
		this.refreshConfig();
		// TODO Was machen wir damit???
		this.allRunning = Bindings.or(this.newLoadTask.runningProperty(), this.openMessageTask.runningProperty())
		.or(this.deleteNoteService.runningProperty()).or(this.newNoteService.runningProperty())
		.or(this.openFolderTask.runningProperty()).or(this.renameNoteService.runningProperty());

		// Bindings
		account.textProperty().bind(currentAccount);
		
		// Actions
		switchAccountMenuItem.setOnAction( e -> {
			chooseAccount();
		});
		
		reloadMenuTask.setOnAction(e -> {
			if (allRunning.getValue() == true) {
				return;
			}
			if (closeAccount()) {
				loadMessages(null);
			}
		});
		
		update.setOnAction(e -> {
			if (allRunning.getValue() == true) {
				return;
			}
			saveCurrentMessage();
		});
		
		exit.setOnAction(event -> {
			if (exitPossible()) {
				try {
					destroy();
				} catch (Exception e) {
					getLogger().error("Destroying the backend has failed ...", e);
				}
				config.setLastOpenendAccount(this.currentAccount.getValue());
				configurationService.writeConfig(config);
				stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			} else {
				getLogger().error("exitPossible returned false ...");
			}
		});
		
		preferences.setOnAction( e-> {
			final Preferences prefs = new Preferences();
			final Stage newStage = new Stage();
			newStage.initModality(Modality.APPLICATION_MODAL);
			newStage.initOwner(stage);
			newStage.setHeight(500);
			newStage.setScene(prefs.getScene());
			prefs.getCancelButton().setOnAction( e2-> {
				newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			prefs.getApplyButton().setOnAction( e2-> {
				prefs.savePreferences();
				newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			newStage.showAndWait();
			refreshConfig();
		});
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

	public void refreshConfig() {
		this.config = configurationService.readConfig();
	}

	public void chooseAccount() {
		List<Account> availableAccounts = this.config.getAccountList();
		ChoiceDialog<Account> cd = new ChoiceDialog<>(availableAccounts.get(0), availableAccounts);

		Optional<Account> result = cd.showAndWait();
		if (result.isPresent()) {
			openAccount(result.get());
		}
	}
	
	public boolean closeAccount() {
		if (!exitPossible()) {
			return false;
		}
		this.tp.getTabs().clear();
		if (this.backend != null) {
			try {
				this.backend.destroy();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private void openAccount(Account first) {
		if (!closeAccount()) {
			return;
		}
		if (first.getType() == Account_Type.FS) {
			this.backend = new FSNoteProvider();
		} else {
			this.backend = new IMAPNoteProvider();
		}
		try {
			this.backend.init(first);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.currentAccount.set(first.getAccount_name());
		loadMessages(null);
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
		getLogger().info("Moving {} to {}", msg, target);
		// TODO Suchen
		this.moveNoteService.setNote(msg);
		this.moveNoteService.setParentFolder(target);
		moveNoteService.reset();
		moveNoteService.restart();
		final TreeItem<Note> foundTreeItem = MyListView.searchTreeItem(msg, this.noteCB.getRoot()); 
		deleteCurrentMessage(foundTreeItem, true);
	}

	public void deleteCurrentMessage(TreeItem<Note> treeItem, boolean dontTask) {
		final Note deleteItem = treeItem.getValue();
		if (!dontTask) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Echt jetzt?");
			alert.setContentText("Do really want to delete '" + deleteItem.getSubject() + "' ?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				return;
			}
		}
		deleteNoteService.parentFolderProperty().set(treeItem.getParent());
		deleteNoteService.noteProperty().set(treeItem);
		deleteNoteService.reset();
		deleteNoteService.restart();
	}

	public void renameCurrentMessage(Note curMsg) {
		if (this.allRunning.getValue() == true) {
			return;
		}
		final Dialog dialog = new TextInputDialog("");
		dialog.setTitle("Make a choice");
		dialog.setHeaderText("Please enter the new name for " + curMsg.getSubject());
		Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		renameNoteService.setSubject(entered);
		renameNoteService.noteProperty().set(curMsg);
		renameNoteService.reset();
		renameNoteService.restart();
	}

	public void loadMessages(Note messageToOpen) {
		getLogger().info("Loading message {}", messageToOpen);
		newLoadTask.setNote(messageToOpen);
		newLoadTask.reset();
		newLoadTask.restart();
	}

	private Optional<ButtonType> demandConfirmation() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Content has changed ...");
		alert.setContentText("Do you want to continue without saving first?");
		return alert.showAndWait();
	}

	// Aufgerufen beim Klick aufs ListViewItem
	public void openNote(Note m) {
		if (m == null)
			return;

		if (m.isFolder()) {
			getLogger().warn("Opening folders like this not supported, yet.");
			return;
		}
		// Böse, aber funktioniert ...
		for (Tab t : this.tp.getTabs()) {
			EditorTab et = (EditorTab) t;
			if (et.getNote().equals(m)) {
				this.tp.getSelectionModel().select(t);
				return;
			}
		}

		getLogger().info("Opening {}", m.getSubject());

		this.openMessageTask.noteProperty().set(m);
		this.openMessageTask.restart();
	}

	// Aufgerufen beim Klick aufs ListViewItem
	public void openFolder(TreeItem<Note> m) {
		if (m == null)
			return;
		// TODO Brauchen wir das noch ????
		// Böse, aber funktioniert ...
		for (Tab t : this.tp.getTabs()) {
			EditorTab et = (EditorTab) t;
			if (et.getNote().equals(m)) {
				this.tp.getSelectionModel().select(t);
				return;
			}
		}

		getLogger().info("Opening Folder {}", m.getValue().getSubject());

		this.openFolderTask.noteFolderProperty().set(m);
		this.openFolderTask.restart();

	}


	public void createNewMessage(boolean createFolder, TreeItem<Note> parent) {
		final Dialog<String> dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("What title is the new note going to have?");
		final Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		newNoteService.setParentFolder(parent);
		newNoteService.setCreateFolder(createFolder);
		newNoteService.setSubject(entered);
		newNoteService.reset();
		newNoteService.restart();
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
		if (noUnsavedChanges)
			return true;
		final Optional<ButtonType> result = demandConfirmation();
		return (result.isPresent() && result.get() == ButtonType.OK);
	}

	public void destroy() throws Exception {
		if (this.backend != null) {
			this.backend.destroy();
		}
	}

	public INoteProvider getBackend() {
		// TODO Auto-generated method stub
		return this.backend;
	}

	public Configuration getConfiguration() {
		return this.config;
	}
}
