package de.wesim.imapnotes.mainview.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.models.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TreeView;

// TODO Später abändern, damit auf Fehlschläge reagiert werden kann ...
@Component
public class RenameNoteService extends AbstractNoteService<Void> {

    @Autowired
	@Qualifier("myListView")
	private TreeView<Note> noteCB;

    private StringProperty subject = new SimpleStringProperty();

    public void setSubject(String subject) {
        this.subject.set(subject);
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

    public RenameNoteService() {
        super();
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateProgress(0, 1);
                updateMessage("Rename note '' to '' ...");
                System.out.println("Renaming ..." + subject.getValue());
                if (getNote().isFolder()) {
                	mainViewController.getBackend().renameFolder(getNote(), subject.getValue());
                } else {
                	mainViewController.getBackend().renameNote(getNote(), subject.getValue());
                }
                updateMessage(String.format("Umbennen von %s erfolgreich!", subject.getValue()));
                updateProgress(1, 1);

                return null;
            }
        };
        return task;
    }

    @Override
	protected void succeeded() {
        noteCB.refresh();
        if (getNote().isFolder()) {
            mainViewController.triggerReload();
        }
	}

}