package de.wesim.imapnotes;

import java.io.IOException;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.mainview.MainViewLoaderService;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JFXMain extends Application {

    private static final String SHORTCUT_QUIT = "Shortcut+Q";
    private static final String SHORTCUT_SAVE = "Shortcut+S";
    private static final String SHORTCUT_FIND = "Shortcut+F";

    private ConfigurableApplicationContext context = null;

    @Value("${userconfig.file}")
    private String userconfigFile;

    @Value("${keystore.file}")
    private String keyStoreFile;

    @Value("${font_families}")
    private String fontFamilies;

    @Override
    public void start(Stage primaryStage) {

        final int parameterCount = getParameters().getRaw().size();
        final String[] args = getParameters().getRaw().toArray(new String[parameterCount]);
        context = SpringApplication.run(JFXMain.class, args);

        final MainViewController mainViewController = this.context.getBean(MainViewController.class);
        // not nice, but the best we can do at the moment ...
        mainViewController.setHostServices(getHostServices());
        mainViewController.setStage(primaryStage);
        final MainViewLoaderService myService = this.context.getBean(MainViewLoaderService.class);
        myService.init(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        if (this.context != null) {
            this.context.close();
        }
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Bean
    public ProgressBar p1() {
        return new ProgressBar();
    }

    @Bean
    public Label status() {
        return new Label();
    }

    @Bean
    public Label account() {
        return new Label();
    }

    @Bean
    public MenuItem reloadMenuTask() {
        return new MenuItem();

    }

    @Bean
    public MenuItem exit() {
        final MenuItem exitItem = new MenuItem();
        exitItem.setAccelerator(KeyCombination.keyCombination(SHORTCUT_QUIT));
        return exitItem;
    }

    @Bean
    public MenuItem update() {
        // content will be configured in MainView
        final MenuItem updateItem = new MenuItem();
        updateItem.setAccelerator(KeyCombination.keyCombination(SHORTCUT_SAVE));
        return updateItem;
    }

    @Bean
    public MenuItem newNote() {
        // content will be configured in MainView
        return new MenuItem();
    }

    @Bean
    public MenuItem newFolder() {
        // content will be configured in MainView
        return new MenuItem();
    }

    @Bean
    public MenuItem switchAccountMenuItem() {
        // content will be configured in MainView
        return new MenuItem();

    }

    @Bean
    public MenuItem preferences() {
        // content will be configured in MainView
        return new MenuItem();
    }

    @Bean
    public MenuItem about() {
        // content will be configured in MainView
        return new MenuItem();
    }

    @Bean
    public MenuItem find() {
        // content will be configured in MainView
        final MenuItem findMenuItem = new MenuItem();
        findMenuItem.setAccelerator(KeyCombination.keyCombination(SHORTCUT_FIND));
        return findMenuItem;
    }

    @Bean
    public TabPane tp() {
        final TabPane newObj = new TabPane();
        newObj.setMinWidth(500);
        return newObj;
    }

    @Bean
    public Path jsonConfigFile() {
        return Paths.get(userconfigFile);
    }

    @Bean
    public Path keyStorePath() {
        return Paths.get(keyStoreFile);
    }

    @Bean
    public List<String> availableFontFamilies() {
        final String[] splitItems = fontFamilies.split(",");
        final List<String> returnList = new ArrayList<>();
        returnList.addAll(Arrays.asList(splitItems));
        return returnList;
    }

    @Bean
    public List<String> availableFontSizes() {
        final List<String> returnList = new ArrayList<>();
        for (int i = 8; i < 50; i++) {
            returnList.add(String.format("%dpx", i));
        }
        return returnList;
    }

}
