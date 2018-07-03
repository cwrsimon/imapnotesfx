package de.wesim.imapnotes.ui.components;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.ConfigurationService;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class FSTab extends Tab {

	// private final QuillEditor qe;

	private Note note;

	private NoteController controller;

	private static final Logger logger = LoggerFactory.getLogger(IMAPTab.class);


	private class FSForm extends GridPane {

		final TextField nameField;
		final TextField pathField;
	
		// TODO Name als Property
		// UNd mit Name der TitledPane verbinden
		public FSForm() {
			//this.setAlignment(Pos.);
			this.setHgap(10);
			this.setVgap(10);
			this.setPadding(new Insets(5, 5, 5, 5));

			Label nameLabel = new Label("Name");
			this.add(nameLabel, 0,0);
			nameField = new TextField();
			this.add(nameField, 1,0);

			Label pathLabel = new Label("File Path");
			this.add(pathLabel, 0,1);

			pathField = new TextField();
			Button dirButton = new Button("...");
			HBox hbox = new HBox(pathField, dirButton);
			this.add(hbox, 1,1);
			
			dirButton.setOnAction( e-> {
				DirectoryChooser dirChooser = new DirectoryChooser();
				File selectedDir = dirChooser.showDialog(getScene().getWindow());
				if (selectedDir != null) {
					pathField.setText(selectedDir.getAbsolutePath());
				}
			});
		}
		

	}

	private TitledPane createTitledPane(String name, String path) {
		FSForm newForm = new FSForm();
		final TitledPane tp = new TitledPane();
		tp.setContent(newForm);
		tp.textProperty().bind(newForm.nameField.textProperty());
		newForm.nameField.textProperty().set(name);
		newForm.pathField.textProperty().set(path);
		return tp;
	}
	
	final Accordion acco;
	
	public FSTab() {
		super("FS");

		final VBox vbox = new VBox();
		setContent(vbox);
		vbox.setPadding(new Insets(5, 5, 5, 5));
		
		acco = new Accordion();
		
		final Button button = new Button("New");
        final Button delete = new Button("Remove");
		final Button save = new Button("Save");
		save.setDisable(true);

		final HBox accountButtons = new HBox(button, delete, save);

		vbox.getChildren().add(acco);
		vbox.getChildren().add(accountButtons);
		
		save.setOnAction(e -> {
            // TODO Asynchron auslagren ???
           // ConfigurationService.writeConfig(configuration);
        });
        button.setOnAction(e -> {
    		acco.getPanes().add(createTitledPane("", ""));
    		save.setDisable(false);

            //final Account newAccount = configuration.createNewAccount();
           // ps.getItems().addAll(createPrefItemsFromAccount(newAccount));

        });
        delete.setOnAction(e -> {
    		save.setDisable(false);

//             Skin<?> skin = ps.getSkin();
//             PropertySheetSkin pss = (PropertySheetSkin) skin;
//             BorderPane np = (BorderPane) pss.getChildren().get(0);
//             ScrollPane scroller = (ScrollPane) np.getCenter();
            // Accordion categories = (Accordion) scroller.getContent();
            // String currentAccount = categories.getExpandedPane().getText();
             TitledPane currentAccount = acco.getExpandedPane();
             acco.getPanes().remove(currentAccount);
             
            // configuration.deleteAccount(currentAccount);
            // ps.getItems().clear();
            // updateEverything(ps, configuration);
        });
	}

	public void addAccount(Account account) {
		// TODO Auto-generated method stub
		final String name = account.getAccount_name();
		final String path = account.getRoot_folder();
		acco.getPanes().add(createTitledPane(name, path));
	}

	
}
