package de.wesim.imapnotes.mainview.services;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.services.I18NService;
import javafx.concurrent.Service;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public abstract class AbstractNoteService<T> extends Service<T> implements HasLogger {

    @Autowired
    protected MainViewController mainViewController;

    @Autowired
    protected I18NService i18N;
    
    @Autowired
    @Qualifier("p1")
    private ProgressBar progress;

    @Autowired
    private Label status;

    public AbstractNoteService() {

    }

    @PostConstruct
    public void init() {
        this.setOnScheduled(e -> {
            progress.progressProperty().unbind();
            progress.progressProperty().bind(this.progressProperty());
            status.textProperty().unbind();
            status.textProperty().bind(this.messageProperty());
        });
        this.setOnFailed(e -> {
            status.textProperty().unbind();
            status.setText(getException().getLocalizedMessage());
            getLogger().error("Action has failed: {}", getActionName(), getException());
        });
    }
    
    public abstract String getActionName();
}
