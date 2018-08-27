package de.wesim.imapnotes.mainview;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.I18NService;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

@Component
public class MainView extends BorderPane implements HasLogger {

	@Autowired
	private I18NService i18N;

	@Autowired
	private ProgressBar p1;

	@Autowired
	private Label status;
	
	@Autowired
	private Label account;
	
	@Autowired
	private TabPane tp;
	
	@Autowired
	@Qualifier("myListView")
	private TreeView<Note> noteCB;

	@Autowired
	private MenuItem reloadMenuTask;

	@Autowired
	private MenuItem exit;

	@Autowired
	private MenuItem update;
	
	@Autowired
	private MenuItem switchAccountMenuItem;

	@Autowired
	private MenuItem preferences;


	public MainView () {
		super();
	}

	@PostConstruct
	public void init() {
						
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu(i18N.getTranslation("file_menu"));
		MenuItem about = new MenuItem(i18N.getTranslation("about_menu"));

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
}
