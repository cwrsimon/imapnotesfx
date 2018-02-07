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
import de.wesim.imapnotes.OpenMessageTask;
import de.wesim.imapnotes.SaveMessageTask;
import de.wesim.models.Note;
import javafx.scene.layout.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
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
import javafx.concurrent.Worker;

// Einarbeiten:
// http://code.makery.ch/library/javafx-8-tutorial/part2/
// Where to go from here:
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
public class HelloWorld extends Application {

	// FIXME 
	private Note currentMessage;
	private IMAPBackend backend;// = new IMAPBackend();
	private final ListView<Note> noteCB = new ListView<>();
	private final HTMLEditor myText = new HTMLEditor();
	private final ProgressBar p1 = new ProgressBar(0);
		
	
	@Override
	public void init() throws Exception {
		super.init();
		this.backend = IMAPBackend.initNotesFolder("Notes/Playground");
	}
	
	private void openNote(Note m) {
			System.out.println("Opening " +m.toString());
			
			clearText();
			myText.setDisable(true);
						
			OpenMessageTask newTask = new OpenMessageTask(backend, m);
			newTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
				@Override
				public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState,
						Worker.State newState) {
					if (newState == Worker.State.SUCCEEDED) {						
						myText.setHtmlText(newTask.getValue());
						myText.setDisable(false);
					}
				}
			});
			p1.progressProperty().unbind();
			p1.progressProperty().bind(newTask.progressProperty());

			new Thread(newTask).run();
		
	}

	
	private void saveCurrentMessage() {
		if (currentMessage == null)
			return;

		final String newContent = myText.getHtmlText();
		System.out.println(newContent);
		
		this.currentMessage.setContent(newContent);
		SaveMessageTask saveMessageTask = new SaveMessageTask(backend, this.currentMessage);
		p1.progressProperty().unbind();
		p1.progressProperty().bind(saveMessageTask.progressProperty());
		new Thread(saveMessageTask).run();
	}

	// TODO einen Thread-Executor einführen???
	private void deleteCurrentMessage() {
		myText.setDisable(true);
		
		System.out.println("Deleting " + currentMessage);
	

		DeleteMessageTask newLoadTask = new DeleteMessageTask(backend, this.currentMessage);
		//noteCB.getItems().clear();
		newLoadTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState,
					Worker.State newState) {
				if (newState == Worker.State.SUCCEEDED) {
					noteCB.setItems(newLoadTask.getValue());
					noteCB.getSelectionModel().select(0);
					myText.setDisable(false);
				}
			}
		});

		new Thread(newLoadTask).start();
	}
	
	
	private void loadMessages(Note messageToOpen) {
		LoadMessageTask newLoadTask = new LoadMessageTask(backend);
		//noteCB.getItems().clear();
		newLoadTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState,
					Worker.State newState) {
				if (newState == Worker.State.SUCCEEDED) {
					noteCB.setItems(newLoadTask.getValue());
					if (messageToOpen != null) {
						noteCB.getSelectionModel().select(messageToOpen);
					} else {
						noteCB.getSelectionModel().select(0);
					}
				}
			}
		});
		new Thread(newLoadTask).start();
	}

	@Override
	public void start(Stage primaryStage) {

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
				currentMessage = newValue;
				openNote(newValue);
				
			}
		});
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem newMenu = new MenuItem("New");
		MenuItem reset   = new MenuItem("Reset");
		MenuItem loadMenu = new MenuItem("Load Messages");
		MenuItem exit = new MenuItem("Exit");

		Menu msgMenu = new Menu("Note");
		MenuItem delete = new MenuItem("Delete");
		MenuItem update  = new MenuItem("Update");
		
		menuBar.getMenus().add(menu);
		menuBar.getMenus().add(msgMenu);
		menu.getItems().addAll(loadMenu, reset, new SeparatorMenuItem(), exit);
		msgMenu.getItems().addAll(newMenu, delete, update);
		 
		HBox hbox = new HBox(p1);
		
		
		BorderPane myPane = new BorderPane();
		myPane.setCenter(myText);
		myPane.setBottom(hbox);
		myPane.setLeft(noteCB);
		myPane.setTop(menuBar);

		reset.setOnAction( e -> {
			p1.progressProperty().unbind();
			p1.setProgress(-1);
		});
		
		loadMenu.setOnAction(e -> {
			try {
				loadMessages(null);
			} catch (Exception exception) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Laden fehlgeschlagen");
				//			saveCurrentMessage();
				alert.showAndWait();
			}
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
//			Alert alert = new Alert(Alert.AlertType.INFORMATION);
//			alert.setTitle("Creating new message");
//			alert.showAndWait();

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
		//try {
		//loadMessages();
		//} catch (MessagingException e1) {
		// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}
		myText.setDisable(true);
		selectFirst();
	}

	private void selectFirst() {
		if (this.noteCB.getItems().size() > 0) {
			this.noteCB.getSelectionModel().select(0);
		}
	}

	


	public void createNewMessage() {
		// TODO check for unsaved changes ...
		this.myText.setDisable(true);
		clearText();

//		final String newContent = myText.getHtmlText();
		Dialog dialog = new TextInputDialog("Bla");
		dialog.setTitle("Enter a subject!");
		dialog.setHeaderText("Enter some text, or use default value.");
		Optional<String> result = dialog.showAndWait();
		String entered = "N/A";
		if (result.isPresent()) {
			entered = result.get();
		}
//		this.currentMessage = entered;
		//		this.noteCB.getItems().add(this.currentMessage);
		// TODO bitte asyncrhon
		try {
			final Note newNote = Note.createNewNote(this.backend, entered);
			loadMessages(newNote);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.myText.setDisable(false);
		this.myText.requestFocus();
	}

	
	private void clearText() {
		myText.setHtmlText("");
	}

	public static void main(String[] args) {
		launch(args);
	}
}