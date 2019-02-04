
import java.io.IOException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.mainview.MainViewLoaderService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JFXMain extends Application {

	private AnnotationConfigApplicationContext context = null;

	@Override
	public void start(Stage primaryStage) {
		this.context = new AnnotationConfigApplicationContext(AppConfig.class);
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
}
