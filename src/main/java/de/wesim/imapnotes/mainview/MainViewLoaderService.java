package de.wesim.imapnotes.mainview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.MyScene;
import java.nio.file.Paths;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@Service
//@PropertySource("classpath:/application.properties")
public class MainViewLoaderService implements HasLogger {

    // TODO Woanders hin
    @Value("${imapnotes_home}")
    private String homeDirectory;
    
    @Autowired
    private MainView mainView;

    @Autowired
    private MainViewController mainViewController;

    public void init(Stage stage) {
        final Scene myScene = new MyScene(mainView);
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
