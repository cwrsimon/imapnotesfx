package de.wesim.imapnotes;

import java.util.Optional;

import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.FSNoteProvider;
import de.wesim.imapnotes.services.IMAPNoteProvider;
import de.wesim.imapnotes.services.INoteProvider;
import de.wesim.imapnotes.ui.background.DeleteMessageTask;
import de.wesim.imapnotes.ui.background.LoadMessageTask;
import de.wesim.imapnotes.ui.background.NewNoteService;
import de.wesim.imapnotes.ui.background.OpenFolderTask;
import de.wesim.imapnotes.ui.background.OpenMessageTask;
import de.wesim.imapnotes.ui.background.RenameNoteService;
import de.wesim.imapnotes.ui.background.SaveMessageTask;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

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
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
// Services implementieren:
// https://stackoverflow.com/questions/37087848/task-progress-bar-javafx-application
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Service.html
// https://gist.github.com/jewelsea/2774476
// https://stackoverflow.com/questions/39299724/javafx-service-and-gui
// TODOs
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

public class HelloWorld extends Application {

	// FIXME 
	private INoteProvider backend;// = new IMAPBackend();
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
		this.backend = new IMAPNoteProvider();
		//this.backend = new FSNoteProvider();
		this.noteController = new NoteController(this.backend, p1, status);
		this.noteController.setHTMLEditor(myText);
		this.noteCB = new MyListView(this.noteController);

		this.noteController.setListView(noteCB);
	}
	

	@Override
	public void start(Stage primaryStage) {
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem reset   = new MenuItem("Reset");
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

		TilePane hbox = new TilePane(account,  status, running, p1);
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setPrefWidth(Double.MAX_VALUE);
		//hbox.setSpacing(50);
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
				this.backend.destroy();
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
