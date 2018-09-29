package de.wesim.imapnotes.preferenceview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.preferenceview.components.FSTab;
import de.wesim.imapnotes.preferenceview.components.GeneralTab;
import de.wesim.imapnotes.preferenceview.components.IMAPTab;
import de.wesim.imapnotes.services.ConfigurationService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// TODO
// Validierungen einbauen
/// FS-Accounts
// Dedizierte Account-Klassen für FS und IMAP
// Layout hübscher machen

// Logger log = Logger.getLogger("myApp");
// log.setLevel(Level.ALL);
// log.info("initializing - trying to load configuration file ...");
// Layout extrahieren !!!
public class Preferences extends Application {

    private Scene myScene;

    private static Logger logger = LoggerFactory.getLogger(Preferences.class);

    Button cancel;
    Button save2;
    FSTab fsTab;
    GeneralTab generalTab;
    IMAPTab imapTab;
    Configuration configuration;
    
    // TODO FIXME
    ConfigurationService cs = new ConfigurationService();
    
    public Preferences() {
        initScene();
    }

    // https://stackoverflow.com/questions/24238858/property-sheet-example-with-use-of-a-propertyeditor-controlsfx#26060211
    public static void main(String[] args) {
        launch(args);
    }

    private void initScene() {
    	// TODO FIXME
        this.configuration = cs.readConfig();

        this.generalTab = new GeneralTab();
        this.imapTab = new  IMAPTab();
        this.fsTab = new FSTab();

        final TabPane tabPane = new TabPane(generalTab, imapTab, fsTab);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        this.cancel = new Button("Cancel");
        this.save2 = new Button("Apply");
        final HBox buttonBar = new HBox(save2, cancel);

        final BorderPane myPane = new BorderPane();
        
        myPane.setCenter(tabPane);
        myPane.setBottom(buttonBar);
		myPane.setPadding(new Insets(5, 5, 5, 5));

        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(tabPane, Priority.SOMETIMES);
        
        for (Account account : configuration.getAccountList()) {
        	if (account.getType() == Account_Type.FS) {
        		fsTab.addAccount(account);
            } else {
                imapTab.addAccount(account);
            }
        }
        generalTab.setFontSize(configuration.getFontSize());
        generalTab.setFontFamily(configuration.getFontFamily());

        imapTab.openAccordion();
        fsTab.openAccordion();
        this.myScene = new Scene(myPane);
        // set global font style
        this.myScene.getRoot().setStyle("-fx-font-size: 18;");
    }

    public void savePreferences() {
        logger.info("{}", fsTab.getAccounts().size());
        configuration.getFSAccounts().clear();
        configuration.getFSAccounts().addAll(fsTab.getAccounts());
        configuration.getIMAPAccounts().clear();
        configuration.getIMAPAccounts().addAll(imapTab.getAccounts());
        configuration.setFontSize(generalTab.getFontSize());
        configuration.setFontFamily(generalTab.getFontFamily());
        this.cs.writeConfig(configuration);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
    	// TODO configure me !!!
        primaryStage.getScene().getRoot().setStyle("-fx-font-size: 18;");
        primaryStage.setScene(this.myScene);
        primaryStage.setWidth(1024);
        primaryStage.setHeight(500);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            logger.info("Quitting application.");
        });
        
        this.cancel.setOnAction( e-> {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        this.save2.setOnAction( e-> {
             savePreferences();
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    public Scene getScene() {
        return this.myScene;
    }

    public Button getCancelButton() {
        return this.cancel;
    }
    public Button getApplyButton() {
        return this.save2;
    }

}