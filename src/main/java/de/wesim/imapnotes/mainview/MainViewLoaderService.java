package de.wesim.imapnotes.mainview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.wesim.imapnotes.HasLogger;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@Service
public class MainViewLoaderService implements HasLogger {

    @Autowired
    private MainView mainView;

    @Autowired
    private MainViewController mainViewController;

    public void init(Stage stage) {
        final Scene myScene = new Scene(mainView);
        Parent root = myScene.getRoot();
        // TODO read value from config file
		//root.setStyle("-fx-font-size: 18;");
        //stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(myScene);
        stage.setWidth(1024);
        stage.setHeight(550);
        stage.setResizable(true);
        stage.getIcons().add(new Image(MainViewLoaderService.class.getResource("/icon.png").toExternalForm()));
        stage.setTitle("ImapNotesFX");
        stage.show();
        stage.setOnCloseRequest(e -> {
            getLogger().info("Quitting application.");
        });
        this.mainViewController.startup();
    }
}
