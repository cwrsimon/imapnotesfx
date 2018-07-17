import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.ui.bootstrap.BootstrapService;
import javafx.application.Application;
import javafx.stage.Stage;

// https://github.com/bpark/weldse-javafx-integration/blob/master/src/main/java/com/github/bpark/weld/fx/StageInitService.java
// https://github.com/FibreFoX/javafx-gradle-plugin
// Beim Ordnerwechsel oder Reload alle geöffneten Tabs schließen
// Neue Implementierung von Gnome Keyring:
// https://github.com/revelc/gnome-keyring-java
// Neuer Editor:
// JSOUP durch etwas Sinnvolleres ersetzen ...
// Asynchron gestalten
// Sortierung nach Datum
// http://www.kurtsparber.de/?p=246
// https://dzone.com/articles/fxml-javafx-powered-cdi-jboss
// https://github.com/bpark/weldse-javafx-integration
// 1a. Locking
// 2. Kontextmenüs
// 3. Refaktorisierung: Dependency-Injection !!!
// Für verschiedene Fensterebenen:
// http://www.javafxtutorials.com/tutorials/creating-a-pop-up-window-in-javafx/
// https://stackoverflow.com/questions/22166610/how-to-create-a-popup-windows-in-javafx
// Einarbeiten:
// http://code.makery.ch/library/javafx-8-tutorial/part2/
// Where to go from here:
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
// Tabs fertig implementieren
// Geöffneten Zustand abspeichern
// Schließen der Preferences
// Gmail-Integration
// Umgangt mit LEEREM Account
// Sortierung nach Änderungsdatum?
// Rechtsklicks implementieren
// About-Menü-Popup
// Sinnvollere Nachrichten auf Englisch
// Einstellungen
// Dependency Injection
// DMG/ZIP generieren lassen ...:
// https://github.com/FibreFoX/javafx-gradle-plugin/tree/master/examples
// Exceptions !!!
// Beim Löschen: Nächstes Element auswählen
// Rückwärtsgehen anders implementieren
// Bessere Farben für Back-Folder (Symbole???)
// Aktuellen Pfad anzeigen
// Zurück nicht  als Note implementieren
// Löschen ohne Reload ...
// Exceptions als Benutzermeldung bis nach oben propagieren
// Neues Feature: Verschieben per Drag und Drop
// Zurückfolder: Nur Subject heißt "Subject",
// sonst ist er nur eine Referenz auf den jeweiligen Ordner nach
// oben -> komplette IMAP-Pfade als UUID speichern
// Wenn kein Passwort vorhanden ist, muss es eine Abfrage gebrn ...
// Copy , Paste, Historie ans Menü binden ...
// Support für Tiff Bilder
public class MainApp extends Application {


	
	@Override
	public void init() throws Exception {
		super.init();
	}

	@Override
	public void start(Stage primaryStage) {
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
			NoteController noteController = ctx.getBean(NoteController.class);
			noteController.setHostServices(getHostServices());
			final BootstrapService myService = ctx.getBean(BootstrapService.class);
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
