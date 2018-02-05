package de.wesim.imapnotes;

import javafx.concurrent.Task;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javax.mail.Message;

import de.wesim.models.Note;

public class LoadMessageTask extends Task<ObservableList<Note>> {
    private final IMAPBackend backend;

    public LoadMessageTask( IMAPBackend backend) {
        this.backend = backend;
    }

    @Override protected ObservableList<Note> call() throws Exception {
        final List<Note> messages = this.backend.getMessages();	
        return FXCollections.observableArrayList(messages);
		
    }
}