package de.wesim.imapnotes.preferenceview.components;

import java.util.ArrayList;
import java.util.List;

import de.wesim.imapnotes.models.Account;
import de.wesim.imapnotes.services.I18NService;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FSTab extends Tab {

    @Autowired
    private I18NService i18N;

    @Autowired
    private ApplicationContext context;

    private final Hyperlink button;

    private TitledPane createTitledPane(String name, String path) {
        var newForm = context.getBean(FSForm.class);
        final TitledPane tp = new TitledPane();
        tp.setContent(newForm);
        tp.textProperty().bind(newForm.nameField.textProperty());
        newForm.nameField.textProperty().set(name);
        newForm.pathField.textProperty().set(path);
        newForm.removeMe.setOnAction(e -> {
            acco.getPanes().remove(tp);
        });
        return tp;
    }

    final Accordion acco;

    public FSTab() {
        super("FS");
        final VBox vbox = new VBox();
        setContent(vbox);
        vbox.setPadding(new Insets(5, 5, 5, 5));

        acco = new Accordion();
        var scrollPane = new ScrollPane(acco);
        scrollPane.setFitToWidth(true);

        this.button = new Hyperlink();
        final ToolBar toolbar = new ToolBar(button);

        vbox.getChildren().add(toolbar);
        vbox.getChildren().add(scrollPane);

        button.setOnAction(e -> {
            acco.getPanes().add(0, createTitledPane("", ""));
        });
    }

    public void openAccordion() {
        if (acco.getPanes().isEmpty()) {
            return;
        }
        final TitledPane first = acco.getPanes().get(0);
        acco.setExpandedPane(first);
    }

    public void addAccount(Account account) {
        final String name = account.getAccount_name();
        final String path = account.getRoot_folder();
        acco.getPanes().add(createTitledPane(name, path));
    }

    public List<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>();
        for (TitledPane tp : acco.getPanes()) {
            final FSForm form = (FSForm) tp.getContent();
            accounts.add(form.getAccount());
        }
        return accounts;
    }

    @PostConstruct
    void init() {
        this.button.setText(this.i18N.getTranslation("new"));
    }

}
