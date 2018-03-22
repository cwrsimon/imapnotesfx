package de.wesim.imapnotes.ui.background;

import de.wesim.imapnotes.NoteController;
import javafx.concurrent.Service;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;


public abstract class AbstractNoteService<T> extends Service<T> {
    protected final NoteController controller;
	
    public AbstractNoteService( NoteController parent, ProgressBar progress, Label status) {
        this.controller = parent;
        this.setOnScheduled(e -> {
			progress.progressProperty().unbind();
			progress.progressProperty().bind(this.progressProperty());
			status.textProperty().unbind();
			status.textProperty().bind( this.messageProperty());
        });
        this.setOnFailed(e-> {

            getException().printStackTrace();

        });
    }

}