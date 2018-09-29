
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.mainview.MainViewLoaderService;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) {
       // try (
       // TODO close me
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
            final MainViewController mainViewController = ctx.getBean(MainViewController.class);
            // FIXME EVIL ...
            mainViewController.setHostServices(getHostServices());
            mainViewController.setStage(primaryStage);
            final MainViewLoaderService myService = ctx.getBean(MainViewLoaderService.class);
            myService.init(primaryStage);
        //}
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) throws IOException {
    	// improved font rendering with anti aliasing
        System.setProperty("prism.lcdtext", "false");
        if (!Files.exists(Consts.APP_DIRECTORY)) {
        	System.out.println("Creating application directory " 
        			+ Consts.APP_DIRECTORY.toAbsolutePath().toString());
        	Files.createDirectory(Consts.APP_DIRECTORY);
        }
        // TODO give it a try some day ...
//	    System.setProperty("prism.subpixeltext", "false");

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        final FileHandler fileHandler = new FileHandler(Consts.LOG_FILE.toAbsolutePath().toString());
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(Level.SEVERE);
        rootLogger.addHandler(fileHandler);
        launch(args);
    }
}
