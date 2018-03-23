package de.wesim.imapnotes;

import java.util.Arrays;
import java.util.Optional;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

// TODO
// Account-Model
// Dependency Injection
// http://www.kurtsparber.de/?p=246
// https://dzone.com/articles/fxml-javafx-powered-cdi-jboss
// https://github.com/bpark/weldse-javafx-integration
// 1. Ungespeicherte Änderungen
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
// Beim Schließen auf Änderungen prüfen
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
// CellFactory muss Folder anders darstellen...
// Beim Löschen: Nächstes Element auswählen
// Rückwärtsgehen anders implementieren
// Bessere Farben für Back-Folder (Symbole???)
// Zurück nicht  als Note implementieren
public class HelloWorld extends Application {

	private MyListView noteCB; 
	private final HTMLEditor myText = new HTMLEditor();
	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	private final Label running = new Label();
	private final Label account = new Label();
	
	// TODO Binding nach NoteController erstellen
    //private BooleanBinding allRunning;
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
		menu.getItems().addAll(loadMenu, reset, new SeparatorMenuItem(), exit);
		msgMenu.getItems().addAll(newFolder, new SeparatorMenuItem(), newMenu, delete, update, renameNote);
		
		this.running.textProperty().bind(
				Bindings.createStringBinding( () -> 
					String.valueOf(this.noteController.allRunning.getValue())
				, this.noteController.allRunning)
			);	

		GridPane hbox = new GridPane();
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

		BorderPane myPane = new BorderPane();
		myPane.setCenter(myText);
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

		exit.setOnAction(e -> {
			try {
				this.noteController.destroy();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
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
			this.noteController.deleteCurrentMessage(this.noteCB.getSelectionModel().getSelectedItem());
		});

		reset.setOnAction( e -> {
			this.noteController.chooseAccount();
		});

		Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(500);
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
