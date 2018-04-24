package de.wesim.imapnotes;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.BeanPropertyUtils;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.services.ConfigurationService;
import impl.org.controlsfx.skin.PropertySheetSkin;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
        BeanProperty prop =  (BeanProperty) i;
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

    PropertySheet ps = new PropertySheet(     );
    ps.setModeSwitcherVisible(false);
    ps.setSearchBoxVisible(false);
    ps.setMode(Mode.CATEGORY);
    // TODO verlagern
    updateEverything(ps, configuration);
   
    Button button = new Button("New");
    Button delete = new Button("Delete");
    Button save = new Button("Save");

    HBox buttonBar = new HBox(button, delete, save);
    VBox myPane = new VBox(ps, buttonBar);
    save.setOnAction( e-> {
        // TODO Asynchron auslagren ???
        ConfigurationService.writeConfig(configuration);
    });
    button.setOnAction( e-> {
        final Account newAccount = configuration.createNewAccount();
        ps.getItems().addAll(createPrefItemsFromAccount(newAccount));

    });
    delete.setOnAction( e-> {

        Skin<?> skin = ps.getSkin();
        PropertySheetSkin pss = (PropertySheetSkin) skin;
        BorderPane np = (BorderPane) pss.getChildren().get(0);
        ScrollPane scroller = (ScrollPane) np.getCenter();
        Accordion categories = (Accordion) scroller.getContent();
        String currentAccount = categories.getExpandedPane().getText();
        configuration.deleteAccount(currentAccount);
        ps.getItems().clear();
        updateEverything(ps, configuration);
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

			System.err.println("Quitting application.");
        });
    }

    public Scene getScene() {
        return this.myScene;
    }

}