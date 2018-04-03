import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.BeanPropertyUtils;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.services.ConfigurationService;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {



public static void main(String[] args) {
    launch(args);
}

@Override
public void start(Stage primaryStage) throws Exception {

    Configuration configuration = ConfigurationService.readConfig();

    PropertySheet ps = new PropertySheet(     );
    ps.setModeSwitcherVisible(false);
    ps.setSearchBoxVisible(false);
    ps.setMode(Mode.CATEGORY);
    for (Account a : configuration.getAccountList()) {
        ObservableList<Item> list = BeanPropertyUtils.getProperties(a);
        for (Item i : list) {
            BeanProperty prop =  (BeanProperty) i;
            prop.getPropertyDescriptor().setValue(BeanProperty.CATEGORY_LABEL_KEY, a.toString());
        }
        ps.getItems().addAll(list);
    }
    Button button = new Button("Print");
    VBox myPane = new VBox(ps, button);
    button.setOnAction( e-> {
        for (Account a : configuration.getAccountList()) {

            System.out.println(a.getType());

        }

    });

	Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(500);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {

			System.err.println("Quitting application.");
		});
}

}