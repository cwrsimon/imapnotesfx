import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;

import javax.mail.Message;
import javax.mail.MessagingException;

import de.wesim.imapnotes.DeleteMessageTask;
import de.wesim.imapnotes.IMAPBackend;
import de.wesim.imapnotes.LoadMessageTask;
import de.wesim.imapnotes.NewNoteService;
import de.wesim.imapnotes.OpenMessageTask;
import de.wesim.imapnotes.SaveMessageTask;
import de.wesim.models.Note;
import javafx.scene.layout.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;

import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.concurrent.Worker;

// TODO
// Gibt es ungespeicherte Änderungen?
// FS-Support
// IMAP-Ordner -> TreeView
// Status-Nachrichten
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
	private IMAPBackend backend;// = new IMAPBackend();
	private final ListView<Note> noteCB = new ListView<>();
	private final HTMLEditor myText = new HTMLEditor();
	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	private final Label running = new Label();
	private NewNoteService newNoteService;
	private OpenMessageTask openMessageTask;
	private DeleteMessageTask deleteNoteService;
	private SaveMessageTask saveMessageTask;
	private LoadMessageTask newLoadTask ;
    private BooleanBinding allRunning;

	
	@Override
	public void init() throws Exception {
		super.init();
		this.backend = IMAPBackend.initNotesFolder("Notes/Playground");
		this.saveMessageTask = new SaveMessageTask(backend, p1, status);
		this.newLoadTask = new LoadMessageTask(backend, p1, status);
		this.newNoteService = new NewNoteService(backend, p1, status);
		this.openMessageTask = new OpenMessageTask(backend, p1, status);
		this.deleteNoteService = new DeleteMessageTask(backend, p1, status);
		this.allRunning = Bindings.or(this.newLoadTask.runningProperty(), 
			this.saveMessageTask.runningProperty()).
			or(this.openMessageTask.runningProperty())
			.or(this.deleteNoteService.runningProperty())
			.or(this.newNoteService.runningProperty());
	
	}
	
	private void openNote(Note m) {
			System.out.println("Opening " +m.toString());
			this.openMessageTask.noteProperty().set(m);
			this.openMessageTask.reset();
			this.openMessageTask.restart();
	}

	
	private void saveCurrentMessage() {

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About to save note ...");
		alert.showAndWait();
		final String newContent = myText.getHtmlText();
		System.out.println(newContent);
		
		final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();
		curMsg.setContent(newContent);
		saveMessageTask.noteProperty().set(curMsg);
		saveMessageTask.reset();
		saveMessageTask.restart();
		
	}

	private void deleteCurrentMessage() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("About to save note ...");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.CANCEL) {
			return;
		}

		// Dialog dialog = new TextInputDialog("Bla");
		// dialog.setTitle("Enter a subject!");
		// dialog.setHeaderText("Enter some text, or use default value.");
		// Optional<String> result = dialog.showAndWait();
		
		
		final Note curMsg = this.noteCB.getSelectionModel().getSelectedItem();
		deleteNoteService.noteProperty().set(curMsg);
		deleteNoteService.reset();
		deleteNoteService.restart();
	}
	
	
	private void loadMessages(Note messageToOpen) {
		if (this.allRunning.getValue() == true) {
			return;
		}
		System.out.println(messageToOpen);
		newLoadTask.setNote(messageToOpen);
		newLoadTask.reset();
		newLoadTask.restart();
	}

	@Override
	public void start(Stage primaryStage) {
		
		newLoadTask.setOnSucceeded(e -> {
			noteCB.setItems(newLoadTask.getValue());
			if (newLoadTask.noteProperty().getValue() != null) {
				noteCB.getSelectionModel().select(newLoadTask.noteProperty().getValue());
			} else {
				noteCB.getSelectionModel().select(0);
			}
		});
		newNoteService.setOnSucceeded( e -> {
			System.out.println("Neu erstelle NAchricht");
			System.out.println(newNoteService.getValue());
			loadMessages(newNoteService.getValue());
		});
		openMessageTask.setOnSucceeded(e -> {
			myText.setHtmlText(openMessageTask.getValue());
		});
		deleteNoteService.setOnSucceeded( e -> {
			loadMessages( null );
		});

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
				
				if (newValue == null)
					return;
				openNote(newValue);
				
			}
		});
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem newMenu = new MenuItem("New");
		MenuItem reset   = new MenuItem("Reset");
		MenuItem loadMenu = new MenuItem("Reload");
		MenuItem exit = new MenuItem("Exit");

		Menu msgMenu = new Menu("Note");
		MenuItem delete = new MenuItem("Delete");
		MenuItem update  = new MenuItem("Update");
		
		menuBar.getMenus().add(menu);
		menuBar.getMenus().add(msgMenu);
		menu.getItems().addAll(loadMenu, reset, new SeparatorMenuItem(), exit);
		msgMenu.getItems().addAll(newMenu, delete, update);
			 
		
		
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

		reset.setOnAction( e -> {
			//resetProgressBar();
			
		});
		
		loadMenu.setOnAction(e -> {
			loadMessages(null);
		});

		exit.setOnAction(e -> {
			try {
				this.backend.destroy();
			} catch (MessagingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});

		update.setOnAction(e -> {

			saveCurrentMessage();

		});
		newMenu.setOnAction(e -> {
			createNewMessage();
		});
		delete.setOnAction(e -> {
			deleteCurrentMessage();
		});

		Scene myScene = new Scene(myPane);
		primaryStage.setScene(myScene);
		primaryStage.setWidth(800);
		primaryStage.setHeight(500);
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			System.err.println("Quitting application.");
		});

		loadMessages(null);
	}


	public void createNewMessage() {
		// TODO check for unsaved changes ...
		//this.myText.setDisable(true);

		Dialog dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("Enter some text, or use default value.");
		Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
		newNoteService.setSubject(entered);
		newNoteService.reset();
		newNoteService.restart();
	}

	public static void main(String[] args) {
		launch(args);
	}
}