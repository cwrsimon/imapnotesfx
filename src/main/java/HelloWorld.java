import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;

import javax.mail.Message;
import javax.mail.MessagingException;

import de.wesim.imapnotes.IMAPBackend;
import de.wesim.imapnotes.LoadMessageTask;
import de.wesim.models.Note;
import javafx.scene.layout.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;

// Einarbeiten:
// http://code.makery.ch/library/javafx-8-tutorial/part2/
// Where to go from here:
// https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
public class HelloWorld extends Application {

	private List<Message> messages;
	// can be either Message or String ...
	// FIXME 
	private Note currentMessage;
	private IMAPBackend backend;// = new IMAPBackend();
	private final ComboBox<Note> noteCB = new ComboBox<>();
	private final HTMLEditor myText = new HTMLEditor();

	@Override
	public void init() throws Exception {
		super.init();
		this.backend = IMAPBackend.initNotesFolder("Notes/Playground");
	}

	private void loadMessages(Note messageToOpen) throws MessagingException {
		LoadMessageTask newLoadTask = new LoadMessageTask(backend);
		noteCB.getItems().clear();
		newLoadTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState,
					Worker.State newState) {
				if (newState == Worker.State.SUCCEEDED) {
					noteCB.setItems(newLoadTask.getValue());
					if (messageToOpen != null) {
						noteCB.getSelectionModel().select(messageToOpen);
					}
				}
			}
		});

		//noteCB.itemsProperty().bind(newLoadTask.valueProperty());

		new Thread(newLoadTask).start();

		//this.messages = this.backend.getMessages();	
		//noteCB.getItems().clear();
		//for (Message m : messages) {
		//		noteCB.getItems().add(m);
		//	}
	}

	@Override
	public void start(Stage primaryStage) {
		Button update = new Button("Save");
		Button delete = new Button("Delete");

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
		noteCB.setConverter(new StringConverter<Note>() {

			@Override
			public String toString(Note object) {
				if (object != null) {
					try {
						return object.getSubject();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "N/A";
					}
				}
				return null;
			}

			@Override
			public Note fromString(String string) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		noteCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Note>() {

			@Override
			public void changed(ObservableValue<? extends Note> observable, 
					Note oldValue, Note newValue) {
				//				if (oldValue == null) return;
				if (newValue == null)
					return;
				try {
					//				if (currentMessage != newValue) {
					openNote(newValue);
					//				}		
				} catch (IOException ex) {
					Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem newMenu = new MenuItem("New");
		MenuItem loadMenu = new MenuItem("Load Messages");

		MenuItem exit = new MenuItem("Exit");

		menuBar.getMenus().add(menu);
		menu.getItems().addAll(newMenu, loadMenu, new SeparatorMenuItem(), exit);
		//		exit.setOnAction(new EventHandler<ActionEvent>() {
		//		    public void handle(ActionEvent t) {
		//		        System.exit(0);
		//		    }
		//		});

		//		menu.
		HBox hbox = new HBox(update, noteCB, delete);

		BorderPane myPane = new BorderPane();
		myPane.setCenter(myText);
		myPane.setBottom(hbox);
		myPane.setTop(menuBar);

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
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("DELETE");
			alert.showAndWait();

			//deleteCurrentMessage();
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

	private void saveCurrentMessage() {
		if (currentMessage == null)
			return;

		final String newContent = myText.getHtmlText();
		System.out.println(newContent);
		this.myText.setDisable(true);
		//clearText();

		try {
			// TODO Bitte asynchron speichern!
			this.currentMessage.setContent(newContent);
			this.currentMessage.update(this.backend);

			//loadMessages(newMessage);
			//int indexOfNewMessage = this.noteCB.getItems().indexOf(newMessage);
			//if (indexOfNewMessage != -1) {
			//	this.noteCB.getSelectionModel().select(indexOfNewMessage);
			//}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.myText.setDisable(false);

	}

//	private void deleteCurrentMessage() {
//		if (currentMessage == null)
//			return;
//		myText.setDisable(true);
//		clearText();
//		if (currentMessage instanceof String) {
//			currentMessage = null;
//			this.myText.setDisable(true);
//		}
//		final Message msgObj = (Message) currentMessage;
//
//		try {
//			System.out.println("Deleting " + msgObj.getSubject());
//			//			int oldIndex = this.noteCB.getItems().indexOf(currentMessage);
//			this.backend.deleteMessage(msgObj);
//			this.currentMessage = null;
//			loadMessages(null);
//			//			this.noteCB.getItems().remove(oldIndex);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		selectFirst();
//	}

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

	public void openNote(Note m) throws IOException {
		this.currentMessage = m;
		try {
			this.currentMessage.load(this.backend);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			System.out.println("Opening " + m.getUuid());
			clearText();
			myText.setDisable(true);
			// TODO content asyncrhon laden !
			myText.setHtmlText(m.getContent());
			myText.setDisable(false);
		
	}

	private void clearText() {
		myText.setHtmlText("");
	}

	public static void main(String[] args) {
		launch(args);
	}
}