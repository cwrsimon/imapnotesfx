import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.mainview.MainViewLoaderService;
import javafx.application.Application;
import javafx.stage.Stage;

// Watch out for:
// https://bugs.openjdk.java.net/browse/JDK-8140491
// i18n
// ERROR-Handling !!
// Aotmatisches Setzen des Subjects durch erste Zeile
// https://github.com/FibreFoX/javafx-gradle-plugin
// Neue Implementierung von Gnome Keyring:
// https://github.com/revelc/gnome-keyring-java
// Neuer Editor:
// JSOUP durch etwas Sinnvolleres ersetzen ...
// TODOs
// Linux: Gnome-Keyring selber öffnen, wenn nicht schon geschehen ...
// optional: Passwort erfragen ...
// Icon
// Zu Applikation bündeln
// Gmail-Integration
// Umgangt mit LEEREM Account
// About-Menü-Popup vervollständigen
// Sinnvollere Nachrichten auf Englisch
// DMG/ZIP generieren lassen ...:
// https://github.com/FibreFoX/javafx-gradle-plugin/tree/master/examples
// Exceptions !!!
// Exceptions als Benutzermeldung bis nach oben propagieren
// Fixen: Verschieben per Drag und Drop
// sonst ist er nur eine Referenz auf den jeweiligen Ordner nach
// Wenn kein Passwort vorhanden ist, muss es eine Abfrage gebrn ...
// Copy , Paste, Historie ans Menü binden ...
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
