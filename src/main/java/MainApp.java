
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

	private AnnotationConfigApplicationContext context = null;

	@Override
	public void start(Stage primaryStage) {
		this.context = new AnnotationConfigApplicationContext(AppConfig.class);
		final MainViewController mainViewController = this.context.getBean(MainViewController.class);
		// FIXME EVIL ...
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
		// improved font rendering with anti aliasing
		System.setProperty("prism.lcdtext", "false");
		if (!Files.exists(Consts.APP_DIRECTORY)) {
			System.out.println("Creating application directory " 
					+ Consts.APP_DIRECTORY.toAbsolutePath().toString());
			Files.createDirectory(Consts.APP_DIRECTORY);
		}
		// TODO give it a try some day ...
		// System.setProperty("prism.subpixeltext", "false");

		// suppress the logging output to the console
		// and log to file instead
		final Logger rootLogger = Logger.getLogger("");
		final FileHandler fileHandler = new FileHandler(Consts.LOG_FILE.toAbsolutePath().toString());
		fileHandler.setFormatter(new SimpleFormatter());
		// TODO configure log level as a parameter 
		fileHandler.setLevel(Level.SEVERE);
		rootLogger.addHandler(fileHandler);
		launch(args);
	}
}
