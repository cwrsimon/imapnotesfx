package de.wesim.imapnotes.mainview.components;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.services.I18NService;
import javafx.scene.control.TextInputDialog;

@Component
@Scope("prototype")
public class PrefixedTextInputDialog extends TextInputDialog {

    @Autowired
    private I18NService i18N;

    private final String translationIdPrefix;

    public PrefixedTextInputDialog(String prefix) {
        super("");
        this.translationIdPrefix = prefix;
        setHeight(500);
        setWidth(500);
        setResizable(true);
    }

    @PostConstruct
    public void init() {
        setTitle(i18N.getTranslation(this.translationIdPrefix + "_input_title_text"));
        setContentText(i18N.getTranslation(this.translationIdPrefix + "_input_content_text"));
        setHeaderText(i18N.getTranslation(this.translationIdPrefix + "_input_header_text"));
    }

}
