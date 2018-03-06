package de.wesim.imapnotes;

import java.util.Optional;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.INoteProvider;
import de.wesim.imapnotes.ui.background.DeleteMessageTask;
import de.wesim.imapnotes.ui.background.LoadMessageTask;
import de.wesim.imapnotes.ui.background.NewNoteService;
import de.wesim.imapnotes.ui.background.OpenFolderTask;
import de.wesim.imapnotes.ui.background.OpenMessageTask;
import de.wesim.imapnotes.ui.background.RenameNoteService;
import de.wesim.imapnotes.ui.background.SaveMessageTask;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.HTMLEditor;

public class NoteController {

	private NewNoteService newNoteService;
	private INoteProvider backend;
	private final ProgressBar progressBar;
	private final Label status;
	private SaveMessageTask saveMessageTask;
    public BooleanBinding allRunning;
	private OpenMessageTask openMessageTask;
	private DeleteMessageTask deleteNoteService;
	private RenameNoteService renameNoteService;
	private LoadMessageTask newLoadTask ;
	private OpenFolderTask openFolderTask;
	private HTMLEditor myText;
	private ListView<Note> noteCB;

	public NoteController(INoteProvider backend, ProgressBar progressBar, Label status) {
		this.backend = backend;
		this.progressBar = progressBar;
		this.status = status;
		
		this.initAsyncTasks();

	}

	public void setHTMLEditor(HTMLEditor node) {
		this.myText = node;
	}

	private void initAsyncTasks() {
		this.saveMessageTask = new SaveMessageTask(this.backend, this.progressBar, this.status);
		this.newLoadTask = new LoadMessageTask(this.backend, this.progressBar, this.status);
		this.openMessageTask = new OpenMessageTask(this.backend, this.progressBar, this.status);
		this.openFolderTask = new OpenFolderTask(this.backend, this.progressBar, this.status);
		this.renameNoteService = new RenameNoteService(this.backend, this.progressBar, this.status);
		this.deleteNoteService = new DeleteMessageTask(this.backend, this.progressBar, this.status);
		this.newNoteService = new NewNoteService(this.backend, this.progressBar, this.status);
		this.newNoteService.setOnSucceeded( e -> {
			System.out.println("Neu erstelle NAchricht");
			System.out.println(newNoteService.getValue());
			this.loadMessages(newNoteService.getValue());
		});
		this.allRunning = Bindings.or(
				this.newLoadTask.runningProperty(), 
			this.saveMessageTask.runningProperty())
			.or(this.openMessageTask.runningProperty())
			.or(this.deleteNoteService.runningProperty())
			.or(this.newNoteService.runningProperty())
			.or(this.openFolderTask.runningProperty())
			.or(this.renameNoteService.runningProperty())
					;	
			// TODO openFolderTask	
		
		newLoadTask.setOnSucceeded(e -> {
			System.out.println("Bla");
			noteCB.setItems(newLoadTask.getValue());
			if (newLoadTask.noteProperty().getValue() != null) {
				noteCB.getSelectionModel().select(newLoadTask.noteProperty().getValue());
			} else {
				noteCB.getSelectionModel().select(null);
			}
		});
		
		openMessageTask.setOnSucceeded(e -> {
			System.out.println(openMessageTask.getValue());
			myText.setHtmlText(openMessageTask.getValue());
		});
		deleteNoteService.setOnSucceeded( e -> {
			loadMessages( null );
		});
		openFolderTask.setOnSucceeded( e-> {
			openFolderTask.noteFolderProperty().set(null);
			loadMessages( null );
		});
		renameNoteService.setOnSucceeded( e-> {
			noteCB.refresh();
		});
	}

	public void deleteCurrentMessage(Note curMsg) {
		if (this.allRunning.getValue() == true) {
			return;
		}
		//final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Echt jetzt?");
		alert.setContentText("Do really want to delete '" + curMsg.getSubject() + "' ?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.CANCEL) {
			return;
		}
		
		deleteNoteService.noteProperty().set(curMsg);
		deleteNoteService.reset();
		deleteNoteService.restart();
	}
	
	public void renameCurrentMessage(Note curMsg) {
		if (this.allRunning.getValue() == true) {
			return;
		}
		//final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();

		Dialog dialog = new TextInputDialog("");
		dialog.setTitle("Make a choice");
		dialog.setHeaderText("Please enter the new name:");
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

	public void openNote(Note old, Note m) {
		System.out.println("openNOte");
		//System.out.println(hasContentChanged());
		if (hasContentChanged(old)) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Content has changed ...");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				return;
			}
		}
		// if (this.allRunning.getValue() == true) {
		// 	return;
		// }
		System.out.println("Opening " +m.getSubject());
		if (m.isFolder() == false) {
			this.openMessageTask.noteProperty().set(m);
			this.openMessageTask.restart();
		} else {
			this.openFolderTask.noteFolderProperty().set(m);
			this.openFolderTask.restart();
		}
	}
	
	// TODO lieber einen Key-Event-Listener implementieren
	private boolean hasContentChanged(Note curMsg) {
		if (curMsg == null) return false;

		System.out.println(curMsg.getSubject());
		final String oldContent = curMsg.getContent();
		if (oldContent == null) return false;
		final String newContent = myText.getHtmlText();
		if (newContent == null) return false;
		System.out.println(oldContent);
		System.out.println(newContent);

		return !oldContent.equals(newContent);
	}

	public void createNewMessage(boolean createFolder) {
		// TODO check for unsaved changes ...
		//this.myText.setDisable(true);

		final Dialog dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("What title is the new note going to have?");
		Optional<String> result = dialog.showAndWait();
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
		
		//final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();
		curMsg.setContent(newContent);
		saveMessageTask.noteProperty().set(curMsg);
		saveMessageTask.reset();
		saveMessageTask.restart();
		
	}

	public void setListView(ListView<Note> noteCB) {
		this.noteCB = noteCB;
	}
}
