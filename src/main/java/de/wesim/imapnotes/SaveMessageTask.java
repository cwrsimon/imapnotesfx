package de.wesim.imapnotes;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javax.mail.Message;

import de.wesim.models.Note;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
public class SaveMessageTask extends Task<Void> {
    private final IMAPBackend backend;
	private final Note victim;

    public SaveMessageTask( IMAPBackend backend, Note msgToSave) {
        this.backend = backend;
        this.victim = msgToSave;
    }

    @Override 
    protected Void call() throws Exception {
    	updateProgress(0, 1);
		this.victim.update(this.backend);
    	updateProgress(1, 1);

    	return null;
    }
}