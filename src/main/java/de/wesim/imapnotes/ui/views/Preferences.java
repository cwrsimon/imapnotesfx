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

// TODO
// Passwort-Felder verschlüsseln
// 2. Passwort-Feld
// Tab-Reiter:
// Generic (Schriftgröße, etc.)
// IMAP-Accounts
// FS-Accounts
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

    public Preferences() {
        initScene();
    }

    // https://stackoverflow.com/questions/24238858/property-sheet-example-with-use-of-a-propertyeditor-controlsfx#26060211
    public static void main(String[] args) {
        launch(args);
    }

    private ObservableList<Item> createPrefItemsFromAccount(Account a) {
        ObservableList<Item> list = BeanPropertyUtils.getProperties(a);
        for (Item i : list) {
            BeanProperty prop = (BeanProperty) i;
            prop.getPropertyDescriptor().setValue(BeanProperty.CATEGORY_LABEL_KEY, a.toString());
        }
        return list;
    }

    private void updateEverything(PropertySheet ps, Configuration config) {
        for (Account a : config.getAccountList()) {
            ps.getItems().addAll(createPrefItemsFromAccount(a));
        }
    }

    private void initScene() {
        Configuration configuration = ConfigurationService.readConfig();

        final Tab generalTab = new  Tab("General");
        final IMAPTab imapTab = new  IMAPTab();
        final FSTab fsTab = new FSTab();

        final TabPane tabPane = new TabPane(generalTab, imapTab, fsTab);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        // PropertySheet ps = new PropertySheet();
        // ps.setModeSwitcherVisible(false);
        // ps.setSearchBoxVisible(false);
        // ps.setMode(Mode.CATEGORY);
        // TODO verlagern
        //updateEverything(ps, configuration);
        
      //  final Button ok = new Button("OK");
        final Button cancel = new Button("Cancel");
        final Button save = new Button("Apply");
		//save.setDisable(true);
        final HBox buttonBar = new HBox(save, cancel);


        final VBox myPane = new VBox(tabPane, buttonBar);
        myPane.setSpacing(5);
		myPane.setPadding(new Insets(5, 5, 5, 5));

        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(tabPane, Priority.SOMETIMES);

        // ScrollPane scrollPane = new ScrollPane(myPane);
        // scrollPane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (Account account : configuration.getAccountList()) {
        	if (account.getType() == Account_Type.FS) {
        		fsTab.addAccount(account);
            } else {
                imapTab.addAccount(account);
            }
        }
        save.setOnAction(e -> {
            logger.info("{}", fsTab.getAccounts().size());
            configuration.getFSAccounts().clear();
            configuration.getFSAccounts().addAll(fsTab.getAccounts());
            configuration.getIMAPAccounts().clear();
            configuration.getIMAPAccounts().addAll(imapTab.getAccounts());
            // TODO Asynchron auslagren ???
            ConfigurationService.writeConfig(configuration);
        });
        this.myScene = new Scene(myPane);
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
    }

    public Scene getScene() {
        return this.myScene;
    }

}