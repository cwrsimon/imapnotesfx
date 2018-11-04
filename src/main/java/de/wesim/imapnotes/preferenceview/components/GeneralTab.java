package de.wesim.imapnotes.preferenceview.components;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.services.I18NService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;

public class GeneralTab extends Tab {

	final ChoiceBox<String> fontSizeField;
	final ChoiceBox<String> fontFamilyField;

	public GeneralTab(I18NService i18n) {
		super("General");

		final GridPane vbox = new GridPane();
		setContent(vbox);
		vbox.setPadding(new Insets(5, 5, 5, 5));
		vbox.setHgap(10);
		vbox.setVgap(10);
		
		final Label fontSizeLabel = new Label(i18n.getTranslation("general_config_editor_font_size") + ":");
		vbox.add(fontSizeLabel, 0,0);
		fontSizeField = new ChoiceBox<String>();
		vbox.add(fontSizeField, 1,0);

		final Label fontFamilyLabel = new Label(i18n.getTranslation("general_config_editor_font_family") + ":");
		vbox.add(fontFamilyLabel, 0,1);
		fontFamilyField = new ChoiceBox<String>();
		vbox.add(fontFamilyField, 1,1);
		
		fontSizeField.setItems(FXCollections.observableArrayList(Consts.fontSizes));
		fontSizeField.getSelectionModel().select(Consts.DEFAULT_FONT_SIZE);

		fontFamilyField.setItems(FXCollections.observableArrayList(Consts.fontFamilies));
		fontFamilyField.getSelectionModel().select(Consts.DEFAULT_FONT_FAMILY);
	}

	public String getFontSize() {
		return fontSizeField.getSelectionModel().getSelectedItem();
	}
	
	public void setFontSize(String fontSize) {
		fontSizeField.getSelectionModel().select(fontSize);
	}
	
	public String getFontFamily() {
		return fontFamilyField.getSelectionModel().getSelectedItem();
	}
	
	public void setFontFamily(String fontFamily) {
		fontFamilyField.getSelectionModel().select(fontFamily);
	}
}
