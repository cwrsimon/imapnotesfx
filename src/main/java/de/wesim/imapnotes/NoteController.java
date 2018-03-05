package de.wesim.imapnotes;

import java.util.Optional;

import de.wesim.imapnotes.services.INoteProvider;
import de.wesim.imapnotes.ui.background.NewNoteService;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;

public class NoteController {

	private HelloWorld mainApp;
	private NewNoteService newNoteService;
	private INoteProvider backend;
	private final ProgressBar progressBar;
	private final Label status;

	public NoteController(HelloWorld mainApp, INoteProvider backend, ProgressBar progressBar, Label status) {
		this.backend = backend;
		this.mainApp = mainApp;
		this.progressBar = progressBar;
		this.status = status;
		this.newNoteService = new NewNoteService(this.backend, this.progressBar, this.status);
		this.newNoteService.setOnSucceeded( e -> {
			System.out.println("Neu erstelle NAchricht");
			System.out.println(newNoteService.getValue());
			mainApp.loadMessages(newNoteService.getValue());
		});

	}
	


	public void createNewMessage(boolean createFolder) {
		// TODO check for unsaved changes ...
		//this.myText.setDisable(true);

		Dialog dialog = new TextInputDialog("Bla");
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
	
}
