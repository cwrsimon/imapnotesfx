package de.wesim.imapnotes;

import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.ConfigurationService;
import de.wesim.imapnotes.services.FSNoteProvider;
import de.wesim.imapnotes.services.IMAPNoteProvider;
import de.wesim.imapnotes.services.INoteProvider;
import de.wesim.imapnotes.ui.background.DeleteMessageTask;
import de.wesim.imapnotes.ui.background.LoadMessageTask;
import de.wesim.imapnotes.ui.background.MoveNoteService;
import de.wesim.imapnotes.ui.background.NewNoteService;
import de.wesim.imapnotes.ui.background.OpenFolderTask;
import de.wesim.imapnotes.ui.background.OpenMessageTask;
import de.wesim.imapnotes.ui.background.RenameNoteService;
import de.wesim.imapnotes.ui.background.SaveMessageTask;
import de.wesim.imapnotes.ui.components.MyListView;
import de.wesim.imapnotes.ui.components.QuillEditor;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;

public class NoteController {

	private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

	private MoveNoteService moveNoteService;
	private NewNoteService newNoteService;
	private INoteProvider backend;
	private final ProgressBar progressBar;
	private final Label status;
	private SaveMessageTask saveMessageTask;
	public BooleanBinding allRunning;
	private OpenMessageTask openMessageTask;
	private DeleteMessageTask deleteNoteService;
	private RenameNoteService renameNoteService;
	private LoadMessageTask newLoadTask;
	private OpenFolderTask openFolderTask;
	private QuillEditor myText;
	private MyListView noteCB;

	private Note currentlyOPen = null;

	public StringProperty currentAccount = new SimpleStringProperty("");

	private Configuration config;

	private boolean contentUpdated = false;

	private TabPane tp;

	private HostServices hostServices;

	public NoteController(ProgressBar progressBar, Label status, HostServices hostServices) {
		this.hostServices = hostServices;
		this.progressBar = progressBar;
		this.status = status;
		this.refreshConfig();
		this.initAsyncTasks();
	}

	public void contentUpdated() {
		logger.debug("ContentUpdated()");
		this.contentUpdated = true;
	}

	public void refreshConfig() {
		this.config = ConfigurationService.readConfig();
	}

	public void chooseAccount() {
		List<Account> availableAccounts = this.config.getAccountList();
		ChoiceDialog<Account> cd = new ChoiceDialog<>(availableAccounts.get(0), availableAccounts);

		Optional<Account> result = cd.showAndWait();
		if (result.isPresent()) {
			openAccount(result.get());
		}
	}

	private void openAccount(Account first) {
		if (this.backend != null) {
			try {
				this.backend.destroy();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	// public void setHTMLEditor(QuillEditor node) {
	// 	this.myText = node;
	// }

	private void initAsyncTasks() {
		this.moveNoteService = new MoveNoteService(this, this.progressBar, this.status);
		this.saveMessageTask = new SaveMessageTask(this, this.progressBar, this.status);
		this.newLoadTask = new LoadMessageTask(this, this.progressBar, this.status);
		this.openMessageTask = new OpenMessageTask(this, this.progressBar, this.status);
		this.openFolderTask = new OpenFolderTask(this, this.progressBar, this.status);
		this.renameNoteService = new RenameNoteService(this, this.progressBar, this.status);
		this.deleteNoteService = new DeleteMessageTask(this, this.progressBar, this.status);
		this.newNoteService = new NewNoteService(this, this.progressBar, this.status);
		this.newNoteService.setOnSucceeded(e -> {
			this.noteCB.getItems().add(newNoteService.getValue());
			openNote(newNoteService.getValue());
		});
		this.allRunning = Bindings.or(this.newLoadTask.runningProperty(), this.saveMessageTask.runningProperty())
				.or(this.openMessageTask.runningProperty()).or(this.deleteNoteService.runningProperty())
				.or(this.newNoteService.runningProperty()).or(this.openFolderTask.runningProperty())
				.or(this.renameNoteService.runningProperty());
		// TODO openFolderTask	

		newLoadTask.setOnSucceeded(e -> {
			noteCB.setItems(newLoadTask.getValue());
			currentlyOPen = null;
			// das erste Element öffnen
			final Note firstELement = noteCB.getItems().get(0);
			if (!firstELement.isFolder()) {
				openNote(firstELement);
			}
		});

		openMessageTask.setOnSucceeded(e -> {
			currentlyOPen = openMessageTask.getNote();

			QuillEditor qe = new QuillEditor(this.hostServices, this, openMessageTask.getValue());
			final Tab t = new Tab(currentlyOPen.getSubject(), qe);
			tp.getTabs().add(t);
			tp.getSelectionModel().select(t);
			
			//qe.setHtmlText(openMessageTask.getValue());
			noteCB.getSelectionModel().select(currentlyOPen);
			this.contentUpdated = false;
		});

		deleteNoteService.setOnSucceeded(e -> {
			final Note deleted = deleteNoteService.getNote();
			int index = this.noteCB.getItems().indexOf(deleted);
			this.noteCB.getItems().remove(deleted);
			final int previousItem = Math.max(0, index - 1);
			final Note previous = this.noteCB.getItems().get(previousItem);
			openNote(previous);
		});
		moveNoteService.setOnSucceeded(e -> {
			final Note moved = moveNoteService.getNote();
			deleteCurrentMessage(moved, true);
		});
		openFolderTask.setOnSucceeded(e -> {
			openFolderTask.noteFolderProperty().set(null);
			loadMessages(null);
		});
		renameNoteService.setOnSucceeded(e -> {
			noteCB.refresh();
		});
	}

	public void startup() {
		final Account first = this.config.getAccountList().get(0);
		openAccount(first);
	}

	public void move(Note msg, Note target) {
		logger.info("Moving {} to {}", msg, target);
		this.moveNoteService.setNote(msg);
		this.moveNoteService.setFolder(target);
		moveNoteService.reset();
		moveNoteService.restart();
	}

	public void deleteCurrentMessage(Note curMsg, boolean dontTask) {
		if (!dontTask) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Echt jetzt?");
			alert.setContentText("Do really want to delete '" + curMsg.getSubject() + "' ?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				return;
			}
		}
		deleteNoteService.noteProperty().set(curMsg);
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
		System.out.println(messageToOpen);
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

	public void openNote(Note m) {

		if (this.currentlyOPen != null) {
			System.out.println(this.currentlyOPen.getSubject());
		} else {
			logger.debug("Nicht gesetzt");
		}
		if (this.currentlyOPen != null && hasContentChanged(this.currentlyOPen)) {
			final Optional<ButtonType> result = demandConfirmation();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				//noteCB.toggleOverrideOpening();
				noteCB.getSelectionModel().select(this.currentlyOPen);
				return;
			}
		}
		logger.info("Opening {}", m.getSubject());
		if (m.isFolder() == false) {
			this.openMessageTask.noteProperty().set(m);
			this.openMessageTask.restart();
		} else {
			this.openFolderTask.noteFolderProperty().set(m);
			this.openFolderTask.restart();
		}
	}

	private boolean hasContentChanged(Note curMsg) {
		return this.contentUpdated;
		//if (curMsg == null) return false;

		// String oldContent = curMsg.getContent();
		// if (oldContent == null) return false;
		// oldContent = parse(oldContent);
		// String newContent = myText.getHtmlText();
		// if (newContent == null) return false;
		// newContent = parse(newContent);

		// return !oldContent.equals(newContent);
	}

	private String parse(String htmlContent) {
		final String plainContent = Jsoup.parse(htmlContent).text();
		return plainContent;
	}

	public void createNewMessage(boolean createFolder) {
		final Dialog dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("What title is the new note going to have?");
		final Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		newNoteService.setCreateFolder(createFolder);
		newNoteService.setSubject(entered);
		newNoteService.reset();
		newNoteService.restart();
	}

	public void saveCurrentMessage(Note curMsg) {
		// Alert alert = new Alert(Alert.AlertType.INFORMATION);
		// alert.setTitle("About to save note ...");
		// alert.showAndWait();
		final String newContent = myText.getHtmlText();
		System.out.println(newContent);

		curMsg.setContent(newContent);
		saveMessageTask.noteProperty().set(curMsg);
		saveMessageTask.reset();
		saveMessageTask.restart();
		// TODO muss asynchron geschehen ...
		this.contentUpdated = false;
	}

	public void setListView(MyListView noteCB) {
		this.noteCB = noteCB;
	}

	public MyListView getListView() {
		return this.noteCB;
	}

	public boolean exitPossible() {
		if (!this.hasContentChanged(this.currentlyOPen)) {
			return true;
		}
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

	public void setTabPane(TabPane tp) {
		this.tp = tp;
	}
}
