package de.wesim.imapnotes.ui.bootstrap;

import javafx.stage.Stage;

public class BootstrapService {

	private static final Logger logger = LoggerFactory.getLogger(MainView.class);

	
	private NoteController noteController;


    public void init(Stage stage) {
        MainView mainView = new MainView();

		this.noteController = new NoteController(mainView.getP1(), 
					mainView.getStatus(), getHostServices());

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
				newStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
			});
			prefs.getApplyButton().setOnAction( e2-> {
				prefs.savePreferences();
				newStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
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