package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;

public class GeneralTab extends Tab {

	private static List<String> fontSizes;
	
	static {
		fontSizes = new ArrayList<>();
		for (int i=8; i<50; i++) {
			fontSizes.add(String.format("%dpx", i));
		}
	}

	private static final String DEFAULT_FONT_SIZE = "17px";

	private static final String DEFAULT_FONT_FAMILY = "sans-serif";

	final ChoiceBox<String> fontSizeField;
	final ChoiceBox<String> fontFamilyField;

	private List<String> fontFamilies = List.of("sans-serif", "serif", "monospace", "arial", "courier");

		
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

		final Label fontFamilyLabel = new Label("Editor font family:");
		vbox.add(fontFamilyLabel, 0,1);
		fontFamilyField = new ChoiceBox<String>();
		vbox.add(fontFamilyField, 1,1);
		
		fontSizeField.setItems(FXCollections.observableArrayList(fontSizes));
		fontSizeField.getSelectionModel().select(DEFAULT_FONT_SIZE);

		fontFamilyField.setItems(FXCollections.observableArrayList(fontFamilies));
		fontFamilyField.getSelectionModel().select(DEFAULT_FONT_FAMILY);
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
