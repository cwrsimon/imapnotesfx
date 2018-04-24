package de.wesim.imapnotes;

import de.wesim.imapnotes.ui.components.QuillEditor;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// https://stackoverflow.com/questions/15555510/javafx-stop-opening-url-in-webview-open-in-browser-instead
// https://stackoverflow.com/questions/25622515/desktop-class-in-javafx#25625871

// Neuer Editor:
// https://docs.oracle.com/javase/8/javafx/embedded-browser-tutorial/js-commands.htm
// Editor-View: Icon für URLs hinzufügen
// URLS müssen in einem separaten Fesnter geöffnet werden ...
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
// Logging mit slf4j
// Sortierung nach Änderungsdatum?
// Rechtsklicks implementieren
// About-Menü-Popup
// Sinnvollere Nachrichten auf Englisch
// TreeView??
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
public class HelloWorld extends Application {

	private MyListView noteCB; 
	//private final HTMLEditor myText = new HTMLEditor();
	private final QuillEditor myText = new QuillEditor();
	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	private final Label running = new Label();
	private final Label account = new Label();
	
	
	private NoteController noteController;

	
	@Override
	public void init() throws Exception {
		super.init();
		//this.backend = new FSNoteProvider();
		this.noteController = new NoteController(p1, status);
		this.noteController.setHTMLEditor(myText);
		this.noteCB = new MyListView(this.noteController);

		this.noteController.setListView(noteCB);
	}
	

	@Override
	public void start(Stage primaryStage) {
		
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
		
		this.running.textProperty().bind(
				Bindings.createStringBinding( () -> 
					String.valueOf(this.noteController.allRunning.getValue())
				, this.noteController.allRunning)
			);	

		final GridPane hbox = new GridPane();
		hbox.add(account, 0, 0);
		hbox.add(status, 1, 0);
		hbox.add(running, 2, 0);
		hbox.add(p1, 3, 0);
		hbox.setVgap(10);
		hbox.setHgap(10);
		
		GridPane.setHgrow(account, Priority.ALWAYS);
		GridPane.setHgrow(status, Priority.ALWAYS);
		GridPane.setHgrow(running, Priority.ALWAYS);
		GridPane.setHalignment(p1, HPos.RIGHT);
		account.textProperty().bind(this.noteController.currentAccount);

		Tab t = new Tab("main", myText);
		TabPane tp = new TabPane();
		tp.getTabs().add(t);

		BorderPane myPane = new BorderPane();
		myPane.setCenter(tp);
		myPane.setBottom(hbox);
		myPane.setLeft(noteCB);
		myPane.setTop(menuBar);

		renameNote.setOnAction( e -> {
			this.noteController.renameCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem());
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
					System.err.println("Destroying the backend has failed ...");
					e.printStackTrace();
				}
				primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
			} else {
				// Nachricht
				System.out.println("exitPossible ist falsch ...");
			}
		});

		update.setOnAction(e -> {
			if (this.noteController.allRunning.getValue() == true) {
				return;
			}
			this.noteController.saveCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem());

		});
		newFolder.setOnAction(e -> {
			this.noteController.createNewMessage(true);
		});
		newMenu.setOnAction(e -> {
			this.noteController.createNewMessage(false);
		});
		delete.setOnAction(e -> {
			this.noteController.deleteCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem(), false);
		});
		reset.setOnAction( e -> {
			this.noteController.chooseAccount();
		});
		preferences.setOnAction( e-> {
			Preferences bla = new Preferences();
			final Stage newStage = new Stage();
			newStage.initModality(Modality.APPLICATION_MODAL);
			newStage.initOwner(primaryStage);
			newStage.setScene(bla.getScene());
			newStage.showAndWait();
		});

		final Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(500);
		primaryStage.setResizable(true);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			System.err.println("Quitting application.");
		});
		this.noteController.startup();;
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}
