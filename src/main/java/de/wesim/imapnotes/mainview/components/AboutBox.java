package de.wesim.imapnotes.mainview.components;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import de.wesim.imapnotes.services.I18NService;
import javafx.scene.control.Alert;


// TODO Refactor into Dialog
// cf. Featerpad menu
// License !!!
@Component
@Scope("prototype")
public class AboutBox extends Alert {

    @Autowired
    private I18NService i18N;

    public AboutBox() {
    	super(AlertType.INFORMATION);
        setHeight(500);
        setWidth(500);
        setResizable(true);
    }

    @PostConstruct
    public void init() {
        setTitle(i18N.getTranslation("about_box_tile"));
        // TODO integrate version number
        setContentText(i18N.getTranslation("about_box_content"));
        setHeaderText(i18N.getTranslation("about_box_header"));
    }

}
