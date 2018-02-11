package de.wesim.imapnotes;

import javafx.concurrent.Task;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;


import de.wesim.models.Note;

public class DeleteMessageTask extends Task<ObservableList<Note>> {
    private final IMAPBackend backend;
	private final Note victim;

    public DeleteMessageTask( IMAPBackend backend, Note msgToDelete) {
        this.backend = backend;
        this.victim = msgToDelete;
    }

    @Override 
    protected ObservableList<Note> call() throws Exception {
    	this.victim.delete(this.backend);
    	// reload messages after deletion
        final List<Note> messages = this.backend.getMessages();
        return FXCollections.observableArrayList(messages);
    }
}