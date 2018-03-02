package de.wesim.imapnotes.ui.background;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import de.wesim.imapnotes.services.INoteProvider;
import javafx.concurrent.Service;


public abstract class AbstractNoteService<T> extends Service<T> {
    protected final INoteProvider backend;
	// protected final ProgressBar progress;
	// protected final Label status;

    public AbstractNoteService( INoteProvider backend, ProgressBar progress, Label status) {
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