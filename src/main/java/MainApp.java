import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.mainview.MainViewLoaderService;
import javafx.application.Application;
import javafx.stage.Stage;

// i18n
// ERROR-Handling !!
// TIFF-Image-Support
// Aotmatisches Setzen des Subjects durch erste Zeile
// https://github.com/FibreFoX/javafx-gradle-plugin
// Neue Implementierung von Gnome Keyring:
// https://github.com/revelc/gnome-keyring-java
// Neuer Editor:
// JSOUP durch etwas Sinnvolleres ersetzen ...
// Sortierung nach Datum
// http://www.kurtsparber.de/?p=246
// https://dzone.com/articles/fxml-javafx-powered-cdi-jboss
// Services implementieren:
// https://stackoverflow.com/questions/37087848/task-progress-bar-javafx-application
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Service.html
// https://gist.github.com/jewelsea/2774476
// https://stackoverflow.com/questions/39299724/javafx-service-and-gui
// TODOs
// Linux: Gnome-Keyring selber öffnen, wenn nicht schon geschehen ...
// optional: Passwort erfragen ...
// Icon
// Zu Applikation bündeln
// Gmail-Integration
// Umgangt mit LEEREM Account
// Sortierung nach Änderungsdatum?
// About-Menü-Popup vervollständigen
// Sinnvollere Nachrichten auf Englisch
// DMG/ZIP generieren lassen ...:
// https://github.com/FibreFoX/javafx-gradle-plugin/tree/master/examples
// Exceptions !!!
// Beim Löschen: Nächstes Element auswählen und selbiges Element aus den tabs entfernen !!!!
// Bessere Farben für Back-Folder (Symbole???)
// Aktuellen Pfad anzeigen
// Exceptions als Benutzermeldung bis nach oben propagieren
// Fixen: Verschieben per Drag und Drop
// sonst ist er nur eine Referenz auf den jeweiligen Ordner nach
// Wenn kein Passwort vorhanden ist, muss es eine Abfrage gebrn ...
// Copy , Paste, Historie ans Menü binden ...
// BEim Löschen des Tab schließen
// Beim Umbennen einen Reload des Ordners machen 
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
		launch(args);
	}
}
