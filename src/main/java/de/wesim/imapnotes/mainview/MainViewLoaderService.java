package de.wesim.imapnotes.mainview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.wesim.imapnotes.HasLogger;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Service
public class MainViewLoaderService implements HasLogger {

	
	@Autowired
	private MainView mainView;
	
	@Autowired
	private MainViewController mainViewController;


    public void init(Stage stage) {
    	final Scene myScene = new Scene(mainView);
		stage.setScene(myScene);
		stage.setWidth(1024);
		stage.setHeight(500);
		stage.setResizable(true);
		stage.setTitle("ImapNotesFX");
		stage.show();
		stage.setOnCloseRequest(e -> {
			getLogger().info("Quitting application.");
		});
		this.mainViewController.startup();
    }
}