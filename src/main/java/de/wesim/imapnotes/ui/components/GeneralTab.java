package de.wesim.imapnotes.ui.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.ConfigurationService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
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

public class GeneralTab extends Tab {

	// private final QuillEditor qe;

	private static final Logger logger = LoggerFactory.getLogger(IMAPTab.class);

	private String[] fontSizes = new String[]{
			"8px", "10px", "12px", "14px", "15px", "16px", "17px", "18px", "20px"};

	private static final String DEFAULT_FONT_SIZE = "17px";

	final ChoiceBox<String> fontSizeField;

		
	public GeneralTab() {
		super("General");

		final GridPane vbox = new GridPane();
		setContent(vbox);
		vbox.setPadding(new Insets(5, 5, 5, 5));
		vbox.setHgap(10);
		vbox.setVgap(10);
		final Label fontSizeLabel = new Label("Editor font size (px):");
		vbox.add(fontSizeLabel, 0,0);
		fontSizeField = new ChoiceBox<String>();
		vbox.add(fontSizeField, 1,0);
		
		fontSizeField.setItems(FXCollections.observableArrayList(fontSizes));
		fontSizeField.getSelectionModel().select(DEFAULT_FONT_SIZE);
        // button.setOnAction(e -> {
    	// 	acco.getPanes().add(createTitledPane("", ""));
    	// 	//save.setDisable(false);

        //     //final Account newAccount = configuration.createNewAccount();
        //    // ps.getItems().addAll(createPrefItemsFromAccount(newAccount));

        // });
        // delete.setOnAction(e -> {
		// 	final TitledPane currentAccount = acco.getExpandedPane();
        // 	acco.getPanes().remove(currentAccount);
        // });
	}

	public String getFontSize() {
		return fontSizeField.getSelectionModel().getSelectedItem();
	}
	
	public void setFontSize(String fontSize) {
		fontSizeField.getSelectionModel().select(fontSize);
	}
}
