package de.wesim.imapnotes.ui.views;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.BeanPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.services.ConfigurationService;
import de.wesim.imapnotes.ui.components.FSTab;
import de.wesim.imapnotes.ui.components.GeneralTab;
import de.wesim.imapnotes.ui.components.IMAPTab;
import impl.org.controlsfx.skin.PropertySheetSkin;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// TODO
// Passwort-Felder verschlüsseln
// 2. Passwort-Feld
// Tab-Reiter:
// Generic (Schriftgröße, etc.)
// Validierungen einbauen
/// FS-Accounts
// Dedizierte Account-Klassen für FS und IMAP
// Layout hübscher machen

// Logger log = Logger.getLogger("myApp");
// log.setLevel(Level.ALL);
// log.info("initializing - trying to load configuration file ...");

// //Properties preferences = new Properties();
// try {
//     //FileInputStream configFile = new //FileInputStream("/path/to/app.properties");
//     //preferences.load(configFile);
//     InputStream configFile = myApp.class.getResourceAsStream("app.properties");
//     LogManager.getLogManager().readConfiguration(configFile);
// } catch (IOException ex)
// {
//     System.out.println("WARNING: Could not open configuration file");
//     System.out.println("WARNING: Logging not configured (console output only)");
// }
// log.info("starting myApp");
public class Preferences extends Application {

    private Scene myScene;

    private static Logger logger = LoggerFactory.getLogger(Preferences.class);

    Button cancel;
    Button save2;
    FSTab fsTab;
    GeneralTab generalTab;
    IMAPTab imapTab;
    Configuration configuration;

    public Preferences() {
        initScene();
    }

    // https://stackoverflow.com/questions/24238858/property-sheet-example-with-use-of-a-propertyeditor-controlsfx#26060211
    public static void main(String[] args) {
        launch(args);
    }

    private void initScene() {
        this.configuration = ConfigurationService.readConfig();

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
       
        fsTab.openAccordion();
        this.myScene = new Scene(myPane);
    }

    public void savePreferences() {
        logger.info("{}", fsTab.getAccounts().size());
        configuration.getFSAccounts().clear();
        configuration.getFSAccounts().addAll(fsTab.getAccounts());
        configuration.getIMAPAccounts().clear();
        configuration.getIMAPAccounts().addAll(imapTab.getAccounts());
        configuration.setFontSize(generalTab.getFontSize());
        ConfigurationService.writeConfig(configuration);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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