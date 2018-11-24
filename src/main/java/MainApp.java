
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

public class MainApp {

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
		fileHandler.setLevel(Level.SEVERE);
		rootLogger.addHandler(fileHandler);
		// call actual JavaFX main app
		// thanks to https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
		JFXMain.main(args);
	}
}
