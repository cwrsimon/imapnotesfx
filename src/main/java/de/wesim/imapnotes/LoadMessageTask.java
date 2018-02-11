package de.wesim.imapnotes;

import javafx.concurrent.Task;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import de.wesim.models.Note;

public class LoadMessageTask extends Service<ObservableList<Note>> {
    private final IMAPBackend backend;

    public LoadMessageTask( IMAPBackend backend) {
        this.backend = backend;
    }

    private ObjectProperty<Note> note = new SimpleObjectProperty<Note>(this, "note");

    public final void setNote(Note value) {
        note.set(value);
    }

    public final Note getNote() {
        return note.get();
    }

    public final ObjectProperty<Note> noteProperty() {
        return note;
    }

    @Override
    protected Task<ObservableList<Note>> createTask() {
        Task<ObservableList<Note>> task = new Task<ObservableList<Note>>() {

            @Override
            protected ObservableList<Note> call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Beginne mit dem Laden der Notizen ...");

                final List<Note> messages = backend.getMessages();	
                updateMessage("Notizenladen erfolgreich!");
                updateProgress(1, 1);

                return FXCollections.observableArrayList(messages);
            }
        };
        return task;
    }
}