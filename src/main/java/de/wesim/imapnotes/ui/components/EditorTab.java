package de.wesim.imapnotes.ui.components;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;

public class EditorTab extends Tab {

	private final QuillEditor qe;

	private Note note;

	private NoteController controller;

	private static final Logger logger = LoggerFactory.getLogger(EditorTab.class);

	private Optional<ButtonType> demandConfirmation() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Content has changed ...");
		alert.setContentText("Do you want to continue without saving first?");
		return alert.showAndWait();
	}

	public EditorTab(NoteController noteController, Note note) {
		super(note.getSubject());
		this.controller = noteController;
		this.qe = new QuillEditor(noteController.getHostServices(),  note.getContent(), noteController.getConfiguration());
		setContent(this.qe);
		this.note = note;
		setOnCloseRequest(e-> {
			// Speicherstatus auslesen
			logger.info("About to close this tab {} with status {}", this.note.getSubject(), this.qe.getContentUpdate());
			if (!this.qe.getContentUpdate()) {
				return;
			}
			final Optional<ButtonType> result = demandConfirmation();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				e.consume();
			}
		});
		this.textProperty().bind(
				Bindings.createStringBinding( () -> 
				{
					if (this.qe.contentUpdateProperty().get()) {
						return "* " + note.getSubject();
					} else {
						return note.getSubject();
					}
				}
				, this.qe.contentUpdateProperty()
						)
				);
	}

	public QuillEditor getQe() {
		return qe;
	}

	public Note getNote() {
		return note;
	}

	public void saveContents() {
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				updateProgress(0, 1);
				controller.getBackend().update(note);
				updateProgress(1, 1);
				return null;
			}

			@Override
			protected void succeeded() {
				// TODO Auto-generated method stub
				super.succeeded();
				getQe().setContentUpdate(false);
				// TODO Text im Tab anpassen
			}
		};
		task.run();
	}
}
