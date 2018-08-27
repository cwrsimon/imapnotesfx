import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
			final MainViewController mainViewController = ctx.getBean(MainViewController.class);
			// FIXME EVIL ...
			mainViewController.setHostServices(getHostServices());
			mainViewController.setStage(primaryStage);
			final MainViewLoaderService myService = ctx.getBean(MainViewLoaderService.class);
	        myService.init(primaryStage);				
		}
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();	
	}

	public static void main(String[] args) {
		System.setProperty("prism.lcdtext", "false");
		// TODO mal ausprobieren
//	    System.setProperty("prism.subpixeltext", "false");
		launch(args);
	}
}
