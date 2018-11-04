package de.wesim.imapnotes.mainview.components;


import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.MyScene;
import de.wesim.imapnotes.services.I18NService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Mostly the same as "TextInputDialog", but with a
 * a password input field, instead. ;-)
 * 
 * A dialog that shows a text input control to the user.
 *
 * @see Dialog
 * @since JavaFX 8u40
 */
@Component
@Scope("prototype")
public class PasswordInputDialog extends Dialog<String> {

	
	@Autowired
    private I18NService i18N;

    /**************************************************************************
     *
     * Fields
     *
     **************************************************************************/

    private final GridPane grid;
    private final Label label;
    private final PasswordField textField;


    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/


    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     * @param defaultValue the default value entered into the dialog
     */
    public PasswordInputDialog() {
        final DialogPane dialogPane = getDialogPane();
        setResizable(true);
        // -- textfield
        this.textField = new PasswordField();
        this.textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setFillWidth(textField, true);

        // -- label
        label = new Label(dialogPane.getContentText());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());
        
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        MyScene.setFontSize(getDialogPane());
        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textField.getText() : null;
        });
    }
    
    @PostConstruct
    public void init() {
    	final String titleText = i18N.getTranslation("password_input_title_text");
      	final String contentText = i18N.getTranslation("password_input_content_text");
      	final String headerText = i18N.getTranslation("password_input_header_text");
      	
      	setTitle(titleText);
        getDialogPane().setContentText(contentText);
        getDialogPane().setHeaderText(headerText);
    }



    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /**
     * Returns the {@link TextField} used within this dialog.
     * @return the {@link TextField} used within this dialog
     */
    public final PasswordField getEditor() {
        return textField;
    }





    /**************************************************************************
     *
     * Private Implementation
     *
     **************************************************************************/

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(textField, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> textField.requestFocus());
    }
}
