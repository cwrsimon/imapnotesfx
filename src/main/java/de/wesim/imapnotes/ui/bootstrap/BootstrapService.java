package de.wesim.imapnotes.ui.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.ui.views.MainView;
import javafx.scene.Scene;
import javafx.stage.Stage;

// TODO
// Umbenennen und verschieben
@Service
public class BootstrapService {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapService.class);

	
	@Autowired
	private MainView mainView;
	
	@Autowired
	private NoteController noteController;


    public void init(Stage stage) {
    	final Scene myScene = new Scene(mainView);
		stage.setScene(myScene);
		stage.setWidth(1024);
		stage.setHeight(500);
		stage.setResizable(true);
		stage.setTitle("ImapNotesFX");
		stage.show();
		stage.setOnCloseRequest(e -> {
			logger.info("Quitting application.");
		});
		this.noteController.startup();
    }
}