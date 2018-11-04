package de.wesim.imapnotes.mainview.components;

import de.wesim.imapnotes.MyScene;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.services.I18NService;
import java.util.List;
import javafx.scene.control.ChoiceDialog;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AccountChoiceDialog extends ChoiceDialog<Account> {

    @Autowired
    private I18NService i18N;

    public AccountChoiceDialog(List<Account> availableAccounts) {
        super(availableAccounts.get(0), availableAccounts);
        setHeight(500);
        setWidth(500);
        setResizable(true);
        MyScene.setFontSize(getDialogPane());
    }

    @PostConstruct
    public void init() {
        setTitle(i18N.getTranslation("account_choice_title_text"));
        setContentText(i18N.getTranslation("account_choice_content_text"));
        setHeaderText(i18N.getTranslation("account_choice_header_text"));
    }

}
