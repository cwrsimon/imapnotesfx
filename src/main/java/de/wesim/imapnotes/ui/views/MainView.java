package de.wesim.imapnotes.ui.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.ui.components.MyListView;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class MainView extends BorderPane {

	private static final Logger logger = LoggerFactory.getLogger(MainView.class);

	private final ProgressBar p1 = new ProgressBar();
	private final Label status = new Label();
	//private final Label running = new Label();
	private final Label account = new Label();
	private final TabPane tp = new TabPane();

	private MenuItem reloadMenuTask;
	private MenuItem exit;
	private MenuItem update;
	private MenuItem switchAccountMenuItem;
	private TreeView<Note> noteCB;

	private MenuItem preferences;


	public MainView () {
		super();

		switchAccountMenuItem   = new Menu("Switch Account ...");
		reloadMenuTask = new MenuItem("Reload");
		preferences = new Menu("Preferences");

		exit = new MenuItem("Exit");
		exit.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));

		update  = new MenuItem("Save current Note");
		update.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
	}

	public void init(NoteController noteController) {
		noteCB = new MyListView(noteController);
		noteCB.setShowRoot(false);
						
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem about = new MenuItem("About");

		menuBar.getMenus().add(menu);
		menu.getItems().addAll(about, new SeparatorMenuItem(), reloadMenuTask, update, switchAccountMenuItem,
									new SeparatorMenuItem(),  preferences, exit);

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
		
		noteCB.setPrefWidth(150);
		
		this.tp.setMinWidth(500);
		this.tp.setPrefWidth(500);
		final SplitPane sp = new SplitPane(new StackPane(noteCB),    tp);
		sp.setOrientation(Orientation.HORIZONTAL);
		sp.setDividerPositions(0.3);
		
		setCenter(sp);
		setBottom(hbox);
		setTop(menuBar);
		
		about.setOnAction( e-> {
			Alert alert = new Alert(AlertType.INFORMATION, "ImapNotesFX 0.0.1");
			alert.setHeaderText("Irgendein Header Text");
			alert.setTitle("Irgendein Titel");
			alert.showAndWait();
		});

	}

	public MenuItem getReloadMenuItem() {
		return this.reloadMenuTask;
	}

	public MenuItem getExit() {
		return this.exit;
	}

	public MenuItem getUpdate() {
		return this.update;
	}

	public MenuItem getPreferences() {
		return this.preferences;
	}

	public MenuItem getSwitchAccountMenuItem() {
		return this.switchAccountMenuItem;
	}

	public ProgressBar getP1() {
		return this.p1;
	}

	public Label getStatus() {
		return this.status;
	}

	public Label getAccount() {
		return this.account;
	}

	public TabPane getTP() {
		return this.tp;
	}

	public TreeView<Note> getNoteCB() {
		return this.noteCB;
	}
}
