package de.wesim.imapnotes.preferenceview.components;

import de.wesim.imapnotes.services.I18NService;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class GeneralTab extends Tab {

    @Autowired
    private I18NService i18N;

    @Value("${default_font_family}")
    private String default_font_family;
            
    @Value("${default_font_size}")
    private String default_font_size;
            
    @Autowired
    private List<String> availableFontFamilies;
    
    @Autowired
    private List<String> availableFontSizes;
    
    final ChoiceBox<String> fontSizeField;
    final ChoiceBox<String> fontFamilyField;
    private final Label fontFamilyLabel;
    private final Label fontSizeLabel;

    public GeneralTab() {
        super("General");

        final GridPane vbox = new GridPane();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setHgap(10);
        vbox.setVgap(10);

        this.fontSizeLabel = new Label();
        vbox.add(fontSizeLabel, 0, 0);
        fontSizeField = new ChoiceBox<>();
        vbox.add(fontSizeField, 1, 0);

        this.fontFamilyLabel = new Label();
        vbox.add(fontFamilyLabel, 0, 1);
        fontFamilyField = new ChoiceBox<>();
        vbox.add(fontFamilyField, 1, 1);

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

    @PostConstruct
    void init() {
        this.fontSizeLabel.setText(i18N.getTranslation("general_config_editor_font_size") + ":");
        this.fontFamilyLabel.setText(i18N.getTranslation("general_config_editor_font_family") + ":");
        fontFamilyField.setItems(FXCollections.observableArrayList(availableFontFamilies));
        fontSizeField.setItems(FXCollections.observableArrayList(availableFontSizes));
        fontSizeField.getSelectionModel().select(default_font_size);
        fontFamilyField.getSelectionModel().select(default_font_family);
    }
}
