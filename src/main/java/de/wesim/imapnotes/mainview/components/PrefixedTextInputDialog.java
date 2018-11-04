package de.wesim.imapnotes.mainview.components;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.MyScene;
import de.wesim.imapnotes.services.I18NService;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

@Component
@Scope("prototype")
public class PrefixedTextInputDialog extends TextInputDialog {

    @Autowired
    private I18NService i18N;

    private final String translationIdPrefix;

	private String messageArgument = null;

    public PrefixedTextInputDialog(String prefix) {
        super("");
        this.translationIdPrefix = prefix;
        setHeight(500);
        setWidth(500);
        setResizable(true);
        MyScene.setFontSize(getDialogPane());
    }
    
    public PrefixedTextInputDialog(String prefix, String messageArgument) {
    	this(prefix);
        this.messageArgument = messageArgument;
    }

    @PostConstruct
    public void init() {
    	String titleText = i18N.getTranslation(this.translationIdPrefix + "_input_title_text");
    	String contentText = i18N.getTranslation(this.translationIdPrefix + "_input_content_text");
    	String headerText = i18N.getTranslation(this.translationIdPrefix + "_input_header_text");
    	if (this.messageArgument != null) {
    		titleText = i18N.getFormattedMessage(titleText, this.messageArgument);
    		contentText = i18N.getFormattedMessage(contentText, this.messageArgument);
    		headerText = i18N.getFormattedMessage(headerText, this.messageArgument);
    	}
    	setTitle(titleText);
    	setContentText(contentText);
    	setHeaderText(headerText);
    }

}
