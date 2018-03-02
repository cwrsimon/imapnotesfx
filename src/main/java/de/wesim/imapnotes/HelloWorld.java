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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

// TODO
// Für verschiedene Fensterebenen:
// http://www.javafxtutorials.com/tutorials/creating-a-pop-up-window-in-javafx/
// https://stackoverflow.com/questions/22166610/how-to-create-a-popup-windows-in-javafx
// Gibt es ungespeicherte Änderungen?
// Logging
// Sortierung nach Änderungsdatum ...
// Tastaturabkürzungen (Strg-S)
// FS-Support
// IMAP-Ordner -> TreeView
// Status-Nachrichten sinnvoller formulieren
// Help-Menü
// Subject ändern ...
// Einarbeiten:
// http://code.makery.ch/library/javafx-8-tutorial/part2/
// Where to go from here:
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
//Services implementieren:
// https://stackoverflow.com/questions/37087848/task-progress-bar-javafx-application
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Service.html
// https://gist.github.com/jewelsea/2774476
// https://stackoverflow.com/questions/39299724/javafx-service-and-gui
// Beim Öffnen alles laden ...
public class HelloWorld extends Application {

	// FIXME 
	private INoteProvider backend;// = new IMAPBackend();
	private final ListView<Note> noteCB = new ListView<>();
	private final HTMLEditor myText = new HTMLEditor();
	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	private final Label running = new Label();
	private NewNoteService newNoteService;
	private OpenMessageTask openMessageTask;
	private DeleteMessageTask deleteNoteService;
	private SaveMessageTask saveMessageTask;
	private RenameNoteService renameNoteService;
	private LoadMessageTask newLoadTask ;
	private OpenFolderTask openFolderTask;
	
    private BooleanBinding allRunning;

	
	@Override
	public void init() throws Exception {
		super.init();
		this.backend = new IMAPNoteProvider();
		//this.backend = new FSNoteProvider();

		this.initAsyncTasks();
	
	}
	
	private void initAsyncTasks() {
		this.saveMessageTask = new SaveMessageTask(backend, p1, status);
		this.newLoadTask = new LoadMessageTask(backend, p1, status);
		this.newNoteService = new NewNoteService(backend, p1, status);
		this.openMessageTask = new OpenMessageTask(backend, p1, status);
		this.openFolderTask = new OpenFolderTask(backend, p1, status);
		this.renameNoteService = new RenameNoteService(backend, p1, status);
		this.deleteNoteService = new DeleteMessageTask(backend, p1, status);
		this.allRunning = Bindings.or(
				this.newLoadTask.runningProperty(), 
			this.saveMessageTask.runningProperty()).
			or(this.openMessageTask.runningProperty())
			.or(this.deleteNoteService.runningProperty())
			.or(this.newNoteService.runningProperty())
			.or(this.openFolderTask.runningProperty())
			.or(this.renameNoteService.runningProperty())
					;	
			// TODO openFolderTask	
		
		newLoadTask.setOnSucceeded(e -> {
			System.out.println("Bla");
			noteCB.setItems(newLoadTask.getValue());
			if (newLoadTask.noteProperty().getValue() != null) {
				noteCB.getSelectionModel().select(newLoadTask.noteProperty().getValue());
			} else {
				noteCB.getSelectionModel().select(null);
			}
		});
		newNoteService.setOnSucceeded( e -> {
			System.out.println("Neu erstelle NAchricht");
			System.out.println(newNoteService.getValue());
			loadMessages(newNoteService.getValue());
		});
		openMessageTask.setOnSucceeded(e -> {
			System.out.println(openMessageTask.getValue());
			myText.setHtmlText(openMessageTask.getValue());
		});
		deleteNoteService.setOnSucceeded( e -> {
			loadMessages( null );
		});
		openFolderTask.setOnSucceeded( e-> {
			openFolderTask.noteFolderProperty().set(null);
			loadMessages( null );
		});
		renameNoteService.setOnSucceeded( e-> {
			noteCB.refresh();
		});
	}

	private void openNote(Note old, Note m) {
		System.out.println("openNOte");
		//System.out.println(hasContentChanged());
		if (hasContentChanged(old)) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Content has changed ...");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				return;
			}
		}
		// if (this.allRunning.getValue() == true) {
		// 	return;
		// }
		System.out.println("Opening " +m.getSubject());
		if (m.isFolder() == false) {
			this.openMessageTask.noteProperty().set(m);
			this.openMessageTask.restart();
		} else {
			this.openFolderTask.noteFolderProperty().set(m);
			this.openFolderTask.restart();
		}
	}

	
	private void saveCurrentMessage() {
		
		// Alert alert = new Alert(Alert.AlertType.INFORMATION);
		// alert.setTitle("About to save note ...");
		// alert.showAndWait();
		final String newContent = myText.getHtmlText();
		System.out.println(newContent);
		
		final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();
		curMsg.setContent(newContent);
		saveMessageTask.noteProperty().set(curMsg);
		saveMessageTask.reset();
		saveMessageTask.restart();
		
	}

	// TODO lieber einen Key-Event-Listener implementieren
	private boolean hasContentChanged(Note curMsg) {
		if (curMsg == null) return false;

		System.out.println(curMsg.getSubject());
		final String oldContent = curMsg.getContent();
		if (oldContent == null) return false;
		final String newContent = myText.getHtmlText();
		if (newContent == null) return false;
		System.out.println(oldContent);
		System.out.println(newContent);

		return !oldContent.equals(newContent);
	}

	private void deleteCurrentMessage() {
		if (this.allRunning.getValue() == true) {
			return;
		}
		final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Echt jetzt?");
		alert.setContentText("Do really want to delete '" + curMsg.getSubject() + "' ?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.CANCEL) {
			return;
		}
		
		deleteNoteService.noteProperty().set(curMsg);
		deleteNoteService.reset();
		deleteNoteService.restart();
	}
	
	private void renameCurrentMessage() {
		if (this.allRunning.getValue() == true) {
			return;
		}
		final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();

		Dialog dialog = new TextInputDialog("");
		dialog.setTitle("Make a choice");
		dialog.setHeaderText("Please enter the new name:");
		Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		renameNoteService.setSubject(entered);
		renameNoteService.noteProperty().set(curMsg);
		renameNoteService.reset();
		renameNoteService.restart();
	}

	
	private void loadMessages(Note messageToOpen) {
		System.out.println(messageToOpen);
		newLoadTask.setNote(messageToOpen);
		newLoadTask.reset();
		newLoadTask.restart();
	}

	@Override
	public void start(Stage primaryStage) {
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TabPane.html
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD
		// TODO Hier Kontextmenüs etc. hinzufügen
		noteCB.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
			@Override
			public ListCell<Note> call(ListView<Note> param) {
				return new ListCell<Note>() {
					@Override
					public void updateItem(Note item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(null);
							setGraphic(null);
						} else {
							setText(item.getSubject());
						}
					}
				};
			}
		});

		noteCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Note>() {
			@Override
			public void changed(ObservableValue<? extends Note> observable, 
					Note oldValue, Note newValue) {
				if (oldValue == null) {
					System.out.println("oldValue:null");
				} else {
					System.out.println("oldValue:" + oldValue.getSubject());
				}
				if (newValue == null) {
					System.out.println("newValue:null");
				} else {
					System.out.println("newValue:" + newValue.getSubject());
				}
				if (newValue == null)
					return;
				openNote(oldValue, newValue);
			}
		});
		
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
		MenuItem renameNote  = new MenuItem("Rename current Note");

		menuBar.getMenus().add(menu);
		menuBar.getMenus().add(msgMenu);
		menu.getItems().addAll(loadMenu, reset, new SeparatorMenuItem(), exit);
		msgMenu.getItems().addAll(newFolder, new SeparatorMenuItem(), newMenu, delete, update, renameNote);
		
		
		this.running.textProperty().bind(
				Bindings.createStringBinding( () -> 
					String.valueOf(allRunning.getValue())
				, allRunning)
			);	

		HBox hbox = new HBox(p1, status, running);
		
		
		BorderPane myPane = new BorderPane();
		myPane.setCenter(myText);
		myPane.setBottom(hbox);
		myPane.setLeft(noteCB);
		myPane.setTop(menuBar);

		renameNote.setOnAction( e -> {
			renameCurrentMessage();
		});
		
		
		loadMenu.setOnAction(e -> {
			if (this.allRunning.getValue() == true) {
				return;
			}
			loadMessages(null);
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
			if (this.allRunning.getValue() == true) {
				return;
			}
			saveCurrentMessage();

		});
		newFolder.setOnAction(e -> {
			createNewMessage(true);
		});
		newMenu.setOnAction(e -> {
			createNewMessage(false);
		});
		delete.setOnAction(e -> {
			deleteCurrentMessage();
		});

		Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(500);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			System.err.println("Quitting application.");
		});

		loadMessages(null);
	}


	public void createNewMessage(boolean createFolder) {
		// TODO check for unsaved changes ...
		//this.myText.setDisable(true);

		Dialog dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("What title is the new note going to have?");
		Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		newNoteService.setCreateFolder(createFolder);
		newNoteService.setSubject(entered);
		newNoteService.reset();
		newNoteService.restart();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
