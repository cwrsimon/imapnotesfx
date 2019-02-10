package de.wesim.imapnotes.mainview.components;

import de.wesim.imapnotes.MyScene;
import de.wesim.imapnotes.services.I18NService;
import java.util.Collection;
import javafx.scene.control.ChoiceDialog;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AccountChoiceDialog<T> extends ChoiceDialog<T> {

    @Autowired
    private I18NService i18N;
    
    private final String translationIdPrefix;


    public AccountChoiceDialog(String prefix, Collection<T> availableAccounts, T firstChoice) {
        super(firstChoice, availableAccounts);
        this.translationIdPrefix = prefix;
        setHeight(500);
        setWidth(500);
        setResizable(true);
        MyScene.setFontSize(getDialogPane());
    }
    
    @PostConstruct
    public void init() {
    	String titleText = i18N.getTranslation(this.translationIdPrefix + "_choice_title_text");
    	String contentText = i18N.getTranslation(this.translationIdPrefix + "_choice_content_text");
    	String headerText = i18N.getTranslation(this.translationIdPrefix + "_choice_header_text");
    	setTitle(titleText);
    	setContentText(contentText);
    	setHeaderText(headerText);
    }
}
