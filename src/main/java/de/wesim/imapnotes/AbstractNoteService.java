package de.wesim.imapnotes;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import de.wesim.models.Note;

public abstract class AbstractNoteService<T> extends Service<T> {
    protected final IMAPBackend backend;
	// protected final ProgressBar progress;
	// protected final Label status;

    public AbstractNoteService( IMAPBackend backend, ProgressBar progress, Label status) {
        this.backend = backend;
        //this.progress = progress;
        //this.status = status;
        this.setOnScheduled(e -> {
			progress.progressProperty().unbind();
			progress.progressProperty().bind(this.progressProperty());
			status.textProperty().unbind();
			status.textProperty().bind( this.messageProperty());

		});
    }

}