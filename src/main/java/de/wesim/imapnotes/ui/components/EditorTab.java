package de.wesim.imapnotes.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.scene.control.Tab;

public class EditorTab extends Tab {

	private final QuillEditor qe;

	private Note note;

	private NoteController controller;
	
	private static final Logger logger = LoggerFactory.getLogger(EditorTab.class);

	public EditorTab(NoteController noteController, Note note) {
		super(note.getSubject());
		this.controller = noteController;
		this.qe = new QuillEditor(noteController.getHostServices(),  note.getContent());
		setContent(this.qe);
		this.note = note;
		setOnCloseRequest(e-> {
			// TODO Confirmation dialog öffnen wenn true
			logger.info("About to close this tab {} with status {}", this.note.getSubject(), this.qe.isContentUpdated());
		});
		this.textProperty().bind(
				Bindings.createStringBinding( () -> 
					String.valueOf(this.noteController.allRunning.getValue())
				, this.noteController.allRunning)
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
					getQe().setContentUpdated(false);
					// TODO Text im Tab anpassen
				}
	        };
	        task.run();
	}
	
}
