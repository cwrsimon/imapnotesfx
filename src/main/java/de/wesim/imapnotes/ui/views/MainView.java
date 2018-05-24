package de.wesim.imapnotes.ui.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.ui.components.MyListView;
import de.wesim.imapnotes.ui.components.MyTreeView;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// Beim Ordnerwechsel oder Reload alle geöffneten Tabs schließen
// Neue Implementierung von Gnome Keyring:
// https://github.com/revelc/gnome-keyring-java
// Neuer Editor:
// JSOUP durch etwas Sinnvolleres ersetzen ...
// Asynchron gestalten
// Sortierung nach Datum
// Verzeichniswechsel: Editor clearen
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
// Umstellung auf TreeView in separatem Branch
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

public class MainView extends Application {

	private static final Logger logger = LoggerFactory.getLogger(MainView.class);

	private TreeView<Note> noteCB; 
	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	//private final Label running = new Label();
	private final Label account = new Label();
	private final TabPane tp = new TabPane();

	
	private NoteController noteController;

	
	@Override
	public void init() throws Exception {
		super.init();
		this.noteController = new NoteController(p1, status, getHostServices());
//		this.noteCB = new TreeVi(this.noteController);

		this.noteCB = new MyListView(this.noteController);
		this.noteCB.setShowRoot(false);
		
		this.noteController.setListView(noteCB);
	}

	@Override
	public void start(Stage primaryStage) {
		this.noteController.setTabPane(tp);

	
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem reset   = new MenuItem("Switch Account ...");
		MenuItem loadMenu = new MenuItem("Reload");
		MenuItem preferences = new MenuItem("Preferences");

		MenuItem exit = new MenuItem("Exit");

		Menu msgMenu = new Menu("Notes");
		MenuItem newFolder = new MenuItem("New Folder");
		MenuItem newMenu = new MenuItem("New Note");
		MenuItem delete = new MenuItem("Delete current Note");

		MenuItem update  = new MenuItem("Save current Note");
		update.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		MenuItem renameNote  = new MenuItem("Rename current Note");

		menuBar.getMenus().add(menu);
		menuBar.getMenus().add(msgMenu);
		menu.getItems().addAll(loadMenu, reset, preferences, new SeparatorMenuItem(), exit);
		msgMenu.getItems().addAll(newFolder, new SeparatorMenuItem(), newMenu, delete, update, renameNote);
		
//		this.running.textProperty().bind(
//				Bindings.createStringBinding( () -> 
//					String.valueOf(this.noteController.allRunning.getValue())
//				, this.noteController.allRunning)
//			);	
//		p1.progressProperty().

		final GridPane hbox = new GridPane();
		hbox.add(account, 0, 0);
		hbox.add(status, 1, 0);
		//hbox.add(running, 2, 0);
		hbox.add(p1, 3, 0);
		hbox.setVgap(10);
		hbox.setHgap(10);
		
		GridPane.setHgrow(account, Priority.ALWAYS);
		GridPane.setHgrow(status, Priority.ALWAYS);
		//GridPane.setHgrow(running, Priority.ALWAYS);
		GridPane.setHalignment(p1, HPos.RIGHT);
		account.textProperty().bind(this.noteController.currentAccount);
		
		this.noteCB.setPrefWidth(150);
		
		this.tp.setMinWidth(500);
		this.tp.setPrefWidth(500);
		final SplitPane sp = new SplitPane(new StackPane(noteCB),    tp);
		sp.setOrientation(Orientation.HORIZONTAL);
		sp.setDividerPositions(0.3);
		
		BorderPane myPane = new BorderPane();
		myPane.setCenter(sp);
		myPane.setBottom(hbox);
		myPane.setTop(menuBar);

		renameNote.setOnAction( e -> {
			// FIXME
			//this.noteController.renameCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem());
		});
		
		loadMenu.setOnAction(e -> {
			if (this.noteController.allRunning.getValue() == true) {
				return;
			}
			this.noteController.loadMessages(null);
		});

		exit.setOnAction(event -> {
			if (this.noteController.exitPossible()) {
				try {
					this.noteController.destroy();
				} catch (Exception e) {
					logger.error("Destroying the backend has failed ...", e);
				}
				primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
			} else {
				logger.error("exitPossible returned false ...");
			}
		});

		update.setOnAction(e -> {
			if (this.noteController.allRunning.getValue() == true) {
				return;
			}
			this.noteController.saveCurrentMessage();

		});
		newFolder.setOnAction(e -> {
			this.noteController.createNewMessage(true, null);
		});
		newMenu.setOnAction(e -> {
			this.noteController.createNewMessage(false, null);
		});
		delete.setOnAction(e -> {
			// FIXME
		//	this.noteController.deleteCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem(), false);
		});
		reset.setOnAction( e -> {
			this.noteController.chooseAccount();
		});
		preferences.setOnAction( e-> {
			final Preferences prefs = new Preferences();
			final Stage newStage = new Stage();
			newStage.initModality(Modality.APPLICATION_MODAL);
			newStage.initOwner(primaryStage);
			newStage.setScene(prefs.getScene());
			newStage.showAndWait();
			this.noteController.refreshConfig();
		});

		final Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(500);
		primaryStage.setResizable(true);
		primaryStage.setTitle("ImapNotesFX");
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			logger.info("Quitting application.");
		});
		this.noteController.startup();;
		
	}

	public static void main(String[] args) {
		launch(args);
	}

}
