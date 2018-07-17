package de.wesim.imapnotes.ui.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.ui.views.MainView;
import de.wesim.imapnotes.ui.views.Preferences;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Service
public class BootstrapService {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapService.class);

	
	@Autowired
	private Label status;
	
	@Autowired
	private ProgressBar p1;
	
	@Autowired
	private MainView mainView;
	
	@Autowired
	private NoteController noteController;


    public void init(Stage stage) {

//		this.noteController = new NoteController(p1, 
//				status, hostServices);

		// TODO als PostConstruct
		mainView.init(this.noteController);

		this.noteController.setListView(mainView.getNoteCB());		
		this.noteController.setTabPane(mainView.getTP());

		mainView.getAccount().textProperty().bind(this.noteController.currentAccount);

		mainView.getReloadMenuItem().setOnAction(e -> {
			if (this.noteController.allRunning.getValue() == true) {
				return;
			}
			if (this.noteController.closeAccount()) {
				this.noteController.loadMessages(null);
			}
		});

		mainView.getExit().setOnAction(event -> {
			if (this.noteController.exitPossible()) {
				try {
					this.noteController.destroy();
				} catch (Exception e) {
					logger.error("Destroying the backend has failed ...", e);
				}
				stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			} else {
				logger.error("exitPossible returned false ...");
			}
		});

		mainView.getUpdate().setOnAction(e -> {
			if (this.noteController.allRunning.getValue() == true) {
				return;
			}
			this.noteController.saveCurrentMessage();
		});
		
		mainView.getSwitchAccountMenuItem().setOnAction( e -> {
			this.noteController.chooseAccount();
		});
		mainView.getPreferences().setOnAction( e-> {
			final Preferences prefs = new Preferences();
			final Stage newStage = new Stage();
			newStage.initModality(Modality.APPLICATION_MODAL);
			newStage.initOwner(stage);
			newStage.setHeight(500);
			newStage.setScene(prefs.getScene());
			prefs.getCancelButton().setOnAction( e2-> {
				newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			prefs.getApplyButton().setOnAction( e2-> {
				prefs.savePreferences();
				newStage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			newStage.showAndWait();
			this.noteController.refreshConfig();
		});

		// TODO Raus da
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