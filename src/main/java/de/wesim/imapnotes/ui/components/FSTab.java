package de.wesim.imapnotes.ui.components;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
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

		// TODO Name als Property
		// UNd mit Name der TitledPane verbinden
		public FSForm() {
			//this.setAlignment(Pos.);
			this.setHgap(10);
			this.setVgap(10);
			//this.setPadding(new Insets(25, 25, 25, 25));

			Label nameLabel = new Label("Name");
			this.add(nameLabel, 0,0);
			TextField nameField = new TextField();
			this.add(nameField, 1,0);

			Label pathLabel = new Label("File Path");
			this.add(pathLabel, 0,1);

			TextField pathField = new TextField();
			Button dirButton = new Button("...");
			HBox hbox = new HBox(pathField, dirButton);
			this.add(hbox, 1,1);
			
			dirButton.setOnAction( e-> {
				DirectoryChooser dirChooser = new DirectoryChooser();
				dirChooser.showDialog(getScene().getWindow());
			});
		}


	}

	public FSTab() {
		super("FS");
	//	this.controller = noteController;
	
		final VBox vbox = new VBox();
		setContent(vbox);


		final Accordion acco = new Accordion();
		final TitledPane tp = new TitledPane("Bla", new FSForm());
		acco.getPanes().add(tp);

		final Button button = new Button("New");
        final Button delete = new Button("Delete");
		final Button save = new Button("Save");
		

		final HBox accountButtons = new HBox(button, delete, save);

		vbox.getChildren().add(acco);
		vbox.getChildren().add(accountButtons);
		//this.note = note;
		
		// this.textProperty().bind(
		// 		Bindings.createStringBinding( () -> 
		// 		{
		// 			if (this.qe.contentUpdateProperty().get()) {
		// 				return "* " + note.getSubject();
		// 			} else {
		// 				return note.getSubject();
		// 			}
		// 		}
		// 		, this.qe.contentUpdateProperty()
		// 				)
		// 		);
		save.setOnAction(e -> {
            // TODO Asynchron auslagren ???
           // ConfigurationService.writeConfig(configuration);
        });
        button.setOnAction(e -> {
            //final Account newAccount = configuration.createNewAccount();
           // ps.getItems().addAll(createPrefItemsFromAccount(newAccount));

        });
        delete.setOnAction(e -> {

            // Skin<?> skin = ps.getSkin();
            // PropertySheetSkin pss = (PropertySheetSkin) skin;
            // BorderPane np = (BorderPane) pss.getChildren().get(0);
            // ScrollPane scroller = (ScrollPane) np.getCenter();
            // Accordion categories = (Accordion) scroller.getContent();
            // String currentAccount = categories.getExpandedPane().getText();
            // configuration.deleteAccount(currentAccount);
            // ps.getItems().clear();
            // updateEverything(ps, configuration);
        });
	}


}
