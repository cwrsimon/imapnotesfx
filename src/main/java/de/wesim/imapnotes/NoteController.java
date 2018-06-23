package de.wesim.imapnotes;

import java.util.List;
import java.util.Optional;

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
import de.wesim.imapnotes.ui.components.EditorTab;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class NoteController {

	private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

	private MoveNoteService moveNoteService;
	private NewNoteService newNoteService;
	private INoteProvider backend;
	private final ProgressBar progressBar;
	private final Label status;
	public BooleanBinding allRunning;
	private OpenMessageTask openMessageTask;
	private DeleteMessageTask deleteNoteService;
	private RenameNoteService renameNoteService;
	private LoadMessageTask newLoadTask;
	private OpenFolderTask openFolderTask;
	private TreeView<Note> noteCB;

	public StringProperty currentAccount = new SimpleStringProperty("");

	private Configuration config;

	private TabPane tp;

	private HostServices hostServices;

	public NoteController(ProgressBar progressBar, Label status, HostServices hostServices) {
		this.hostServices = hostServices;
		this.progressBar = progressBar;
		this.status = status;
		this.refreshConfig();
		this.initAsyncTasks();

	}

	public HostServices getHostServices() {
		return hostServices;
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
		if (!exitPossible()) {
			return;
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

	private boolean isEmptyTreeItem(TreeItem<Note> treeItem) {
		if (treeItem.isLeaf()) return false;
		if (treeItem.getChildren().isEmpty()) return true;
		if (treeItem.getChildren().size() > 1) return false;
		TreeItem<Note> firstItem = treeItem.getChildren().get(0);
		return firstItem.getValue() == null;
	}

	private void initAsyncTasks() {
		this.moveNoteService = new MoveNoteService(this, this.progressBar, this.status);
		this.newLoadTask = new LoadMessageTask(this, this.progressBar, this.status);
		this.openMessageTask = new OpenMessageTask(this, this.progressBar, this.status);
		this.openFolderTask = new OpenFolderTask(this, this.progressBar, this.status);
		this.renameNoteService = new RenameNoteService(this, this.progressBar, this.status);
		this.deleteNoteService = new DeleteMessageTask(this, this.progressBar, this.status);
		this.newNoteService = new NewNoteService(this, this.progressBar, this.status);
		this.newNoteService.setOnSucceeded(e -> {
			// FIXME
			TreeItem<Note> pTreeItem = newNoteService.getParentFolder();
			final Note newNote = newNoteService.getValue();
			final TreeItem<Note> newTreeItem = new TreeItem<Note>(newNote);
			if (newNote.isFolder()) {
				if (isEmptyTreeItem(newTreeItem)) {
					newTreeItem.getChildren().clear();
				}
				newTreeItem.getChildren().add(new TreeItem<Note>(null));
			}
			if (pTreeItem != null) {
				if (isEmptyTreeItem(pTreeItem)) {
					pTreeItem.getChildren().clear();
				}
				pTreeItem.getChildren().add(newTreeItem);
			} else {
				this.noteCB.getRoot().getChildren().add(newTreeItem);
			}
			openNote(newNote);
		});
		this.allRunning = Bindings.or(this.newLoadTask.runningProperty(), this.openMessageTask.runningProperty())
				.or(this.deleteNoteService.runningProperty()).or(this.newNoteService.runningProperty())
				.or(this.openFolderTask.runningProperty()).or(this.renameNoteService.runningProperty());
		// TODO openFolderTask

		newLoadTask.setOnSucceeded(e -> {
			final ObservableList<Note> loadedItems = newLoadTask.getValue();
			noteCB.getRoot().getChildren().clear();
			for (Note n : loadedItems) {
				final TreeItem<Note> newItem = new TreeItem<Note>(n);
				if (n.isFolder()) {
					newItem.getChildren().add(new TreeItem<Note>());
					// TODO
					// https://stackoverflow.com/questions/14236666/how-to-get-current-treeitem-reference-which-is-expanding-by-user-click-in-javafx#14241151
					newItem.setExpanded(false);
					newItem.expandedProperty().addListener(new ChangeListener<Boolean>() {

						@Override
						public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
								Boolean newValue) {
							if (!newValue) {
								return;
							}

							BooleanProperty bb = (BooleanProperty) observable;

							TreeItem<Note> callee = (TreeItem<Note>) bb.getBean();
							if (callee.getChildren().size() != 1)
								return;
							// nur bei einem einzigen leeren Kind
							if (callee.getChildren().get(0).getValue() != null)
								return;
							openFolder(callee);

						}
					});
				}
				noteCB.getRoot().getChildren().add(newItem);
			}
			// noteCB.setItems(loadedItems);
			// currentlyOPen = null;
			// das erste Element öffnen
			if (loadedItems.isEmpty()) return;
			final Note firstELement = loadedItems.get(0);
			if (!firstELement.isFolder()) {
				openNote(firstELement);
			}
		});

		openMessageTask.setOnSucceeded(e -> {
			final Note openedNote = openMessageTask.getValue();

			Tab editorTab = new EditorTab(this, openedNote);
			tp.getTabs().add(editorTab);
			tp.getSelectionModel().select(editorTab);

			// FIXME ???
			// noteCB.getSelectionModel().select(openedNote);
		});

		deleteNoteService.setOnSucceeded(e -> {
			final TreeItem<Note> parentNote = deleteNoteService.getParentFolder();
			final TreeItem<Note> deletedItem = deleteNoteService.getNote();

			final int index = parentNote.getChildren().indexOf(deletedItem);

			parentNote.getChildren().remove(deletedItem);

			final int previousItem = Math.max(0, index - 1);
			if (parentNote.getChildren().isEmpty()) return;
			final TreeItem<Note> previous = parentNote.getChildren().get(previousItem);
			openNote(previous.getValue());
		});
		moveNoteService.setOnSucceeded(e -> {
			final Note moved = moveNoteService.getValue();
			final TreeItem<Note> parentFolder = moveNoteService.getParentFolder();
			// FIXME TODO
			parentFolder.getChildren().add(new TreeItem<Note>(moved));
			noteCB.refresh();
		});
		openFolderTask.setOnSucceeded(e -> {
			TreeItem<Note> containedTreeItem = openFolderTask.noteFolderProperty().get();
			containedTreeItem.getChildren().clear();
			final ObservableList<Note> loadedItems = openFolderTask.getValue();
			for (Note n : loadedItems) {
				final TreeItem<Note> newItem = new TreeItem<Note>(n);
				if (n.isFolder()) {
					newItem.getChildren().add(new TreeItem<Note>());
					// TODO
					// https://stackoverflow.com/questions/14236666/how-to-get-current-treeitem-reference-which-is-expanding-by-user-click-in-javafx#14241151
					newItem.setExpanded(false);
					newItem.expandedProperty().addListener(new ChangeListener<Boolean>() {

						@Override
						public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
								Boolean newValue) {
							if (!newValue) {
								return;
							}

							BooleanProperty bb = (BooleanProperty) observable;

							TreeItem<Note> callee = (TreeItem<Note>) bb.getBean();
							if (callee.getChildren().size() != 1)
								return;
							// nur bei einem einzigen leeren Kind
							if (callee.getChildren().get(0).getValue() != null)
								return;
							openFolder(callee);

						}
					});
				}
				containedTreeItem.getChildren().add(newItem);
			}
			openFolderTask.noteFolderProperty().set(null);
		});
		renameNoteService.setOnSucceeded(e -> {
			noteCB.refresh();
		});
	}

	public void startup() {
		final Account first = this.config.getAccountList().get(0);
		openAccount(first);
	}

	private TreeItem<Note> searchTreeItem(Note searchItem, TreeItem<Note> parent) {
		if (parent.getValue() != null && searchItem.equals(parent.getValue())) {
			return parent;
		}
		if (parent.getChildren().isEmpty()) return null;
		for (TreeItem<Note> child : parent.getChildren()) {
			TreeItem<Note> found = searchTreeItem(searchItem, child);
			if (found != null) return found;
		}
		return null;
	}

	public void move(Note msg, TreeItem<Note> target) {
		logger.info("Moving {} to {}", msg, target);
		// TODO Suchen
		this.moveNoteService.setNote(msg);
		this.moveNoteService.setParentFolder(target);
		// TODO Refresh des Trees
		moveNoteService.reset();
		moveNoteService.restart();
		TreeItem<Note> foundTreeItem = searchTreeItem(msg, this.noteCB.getRoot()); 
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

	// Aufgerufen beim Klick aufs ListViewItem
	public void openNote(Note m) {
		if (m == null)
			return;

		if (m.isFolder()) {
			logger.warn("Opening folders like this not supported, yet.");
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

		logger.info("Opening {}", m.getSubject());
		// if (m.isFolder() == false) {
		this.openMessageTask.noteProperty().set(m);
		this.openMessageTask.restart();
		// }
		// else {
		// this.openFolderTask.noteFolderProperty().set(m);
		// this.openFolderTask.restart();
		// }
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

		logger.info("Opening Folder {}", m.getValue().getSubject());

		this.openFolderTask.noteFolderProperty().set(m);
		this.openFolderTask.restart();

	}
	// private boolean hasContentChanged(Note curMsg) {
	// return this.contentUpdated;
	// //if (curMsg == null) return false;

	// String oldContent = curMsg.getContent();
	// if (oldContent == null) return false;
	// oldContent = parse(oldContent);
	// String newContent = myText.getHtmlText();
	// if (newContent == null) return false;
	// newContent = parse(newContent);

	// return !oldContent.equals(newContent);
	// }

	// private String parse(String htmlContent) {
	// final String plainContent = Jsoup.parse(htmlContent).text();
	// return plainContent;
	// }

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
		logger.info("Saving new content: {}", newContent);

		et.getNote().setContent(newContent);
		et.saveContents();
	}

	public void setListView(TreeView noteCB) {
		this.noteCB = noteCB;
		this.noteCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Note>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<Note>> observable, TreeItem<Note> oldValue,
					TreeItem<Note> newValue) {
				if (newValue == null)
					return;
				if (oldValue != newValue) {
					openNote(newValue.getValue());
				}

			}
		});
	}

	public TreeView getListView() {
		return this.noteCB;
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

	public void setTabPane(TabPane tp) {
		this.tp = tp;
	}
}
