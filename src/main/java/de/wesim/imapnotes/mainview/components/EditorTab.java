package de.wesim.imapnotes.mainview.components;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
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

	private QuillEditor qe;

	private final Note note;

	private static final Logger logger = LoggerFactory.getLogger(EditorTab.class);

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
		this.qe = new QuillEditor(mainViewController.getHostServices(),  note.getContent(), mainViewController.getConfiguration());
		setContent(this.qe);
		setOnCloseRequest(e-> {
			logger.info("About to close this tab {} with status {}", this.note.getSubject(), this.qe.getContentUpdate());
			if (!this.qe.getContentUpdate()) {
				return;
			}
			final Optional<ButtonType> result = demandConfirmation();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				e.consume();
			}
		});
		this.textProperty().bind(
				Bindings.createStringBinding( () -> 
				{
					if (this.qe.contentUpdateProperty().get()) {
						return "* " + note.getSubject();
					} else {
						return note.getSubject();
					}
				}
				, this.qe.contentUpdateProperty()
						)
				);
	}

	public QuillEditor getQe() {
		return qe;
	}

	public Note getNote() {
		return note;
	}

}
