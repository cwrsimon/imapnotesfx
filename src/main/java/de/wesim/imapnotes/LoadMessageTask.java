package de.wesim.imapnotes;

import javafx.concurrent.Task;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javax.mail.Message;

public class LoadMessageTask extends Task<ObservableList<Message>> {
    private final IMAPBackend backend;

    public LoadMessageTask( IMAPBackend backend) {
        this.backend = backend;
    }

    @Override protected ObservableList<Message> call() throws Exception {
        final List<Message> messages = this.backend.getMessages();	
        return FXCollections.observableArrayList(messages);
		
    }
}