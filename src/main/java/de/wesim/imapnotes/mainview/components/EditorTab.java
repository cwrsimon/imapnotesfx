package de.wesim.imapnotes.mainview.components;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.ConfigurationService;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;

@Component
@Scope("prototype")
public class EditorTab extends Tab {

    @Autowired
    protected MainViewController mainViewController;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigurationService configurationService;

    private SummerNoteEditor qe;

    private final Note note;

    private Optional<ButtonType> demandConfirmation() {
        Alert alert = context.getBean(PrefixedAlertBox.class, "close_tab");
        alert.setAlertType(AlertType.CONFIRMATION);
        return alert.showAndWait();
    }

    public EditorTab(Note note) {
        super(note.getSubject());
        this.note = note;
    }

    @PostConstruct
    public void init() {
        this.qe = new SummerNoteEditor(mainViewController.getHostServices(), note.getContent(), configurationService.getConfig());
        setContent(this.qe);
        setOnCloseRequest(e -> {
            if (!this.qe.getContentUpdate()) {
                return;
            }
            final Optional<ButtonType> result = demandConfirmation();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                e.consume();
            }
        });
        this.textProperty().bind(
                Bindings.createStringBinding(()
                        -> {
                    if (this.qe.contentUpdateProperty().get()) {
                        return "* " + note.getSubject();
                    } else {
                        return note.getSubject();
                    }
                },
                        this.qe.contentUpdateProperty()
                )
        );
    }

    public SummerNoteEditor getQe() {
        return qe;
    }

    public Note getNote() {
        return note;
    }

    public void markSearchItems(String entered) {
        getQe().findItems(entered);
    }
}
