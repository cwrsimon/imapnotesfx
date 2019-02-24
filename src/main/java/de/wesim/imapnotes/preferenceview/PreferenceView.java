package de.wesim.imapnotes.preferenceview;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.MyScene;
import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.models.Account_Type;
import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.preferenceview.components.FSTab;
import de.wesim.imapnotes.preferenceview.components.GeneralTab;
import de.wesim.imapnotes.preferenceview.components.IMAPTab;
import de.wesim.imapnotes.services.ConfigurationService;
import de.wesim.imapnotes.services.I18NService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component
@Scope("prototype")
public class PreferenceView extends Stage implements HasLogger {

    @Autowired
    private I18NService i18N;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigurationService configurationService;

    private FSTab fsTab;
    private GeneralTab generalTab;
    private IMAPTab imapTab;
    private Configuration configuration;

    private boolean preferencesSaved = false;

    private final Stage parentStage;

    public PreferenceView(Stage parent) {
        this.parentStage = parent;
    }

    @PostConstruct
    public void init() {
        final Scene newScene = initScene();
        initModality(Modality.APPLICATION_MODAL);
        setHeight(500);
        setWidth(600);
        setScene(newScene);
        setTitle(i18N.getTranslation("preference_view"));
    }

    private Scene initScene() {
        this.generalTab = new GeneralTab(i18N);
        this.imapTab = context.getBean(IMAPTab.class);
        this.fsTab = new FSTab(i18N);

        final TabPane tabPane = new TabPane(generalTab, imapTab, fsTab);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        final Button cancel = new Button(i18N.getTranslation("cancel_button"));
        final Button save2 = new Button(i18N.getTranslation("apply_button"));

        cancel.setOnAction(e2 -> {
            // discard changes
            configurationService.refresh();
            fireEvent(new WindowEvent(this.parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        save2.setOnAction(e2 -> {
            savePreferences();
            fireEvent(new WindowEvent(this.parentStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        final HBox buttonBar = new HBox(save2, cancel);

        final BorderPane myPane = new BorderPane();

        myPane.setCenter(tabPane);
        myPane.setBottom(buttonBar);
        myPane.setPadding(new Insets(5, 5, 5, 5));

        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(tabPane, Priority.SOMETIMES);

        return new MyScene(myPane);
    }

    private void savePreferences() {
        configuration.getFSAccounts().clear();
        configuration.getFSAccounts().addAll(fsTab.getAccounts());
        configuration.getIMAPAccounts().clear();
        configuration.getIMAPAccounts().addAll(imapTab.getAccounts());
        configuration.setFontSize(generalTab.getFontSize());
        configuration.setFontFamily(generalTab.getFontFamily());
        this.configurationService.writeConfig();
        this.preferencesSaved = true;
    }

    public boolean isPreferencesSaved() {
        return preferencesSaved;
    }

    @Override
    public void showAndWait() {
        this.configurationService.refresh();
        this.configuration = configurationService.getConfig();
        for (Account account : configuration.getAccountList()) {
            if (account.getType() == Account_Type.FS) {
                fsTab.addAccount(account);
            } else {
                imapTab.addAccount(account);
            }
        }
        generalTab.setFontSize(configuration.getFontSize());
        generalTab.setFontFamily(configuration.getFontFamily());
        imapTab.openAccordion();
        fsTab.openAccordion();
        super.showAndWait();
    }
}
