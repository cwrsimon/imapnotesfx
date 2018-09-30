package de.wesim.imapnotes.mainview;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.mainview.components.AboutBox;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.I18NService;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

@Component
public class MainView extends BorderPane implements HasLogger {

    @Autowired
    private ApplicationContext context;
	
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

    @Autowired
	private MenuItem find;
    
    @Autowired
	private MenuItem newNote;

    @Autowired
	private MenuItem newFolder;

	public MainView () {
		super();
	}

	@PostConstruct
	public void init() {
		find.setText(i18N.getTranslation("find_menu_item"));
		update.setText(i18N.getTranslation("save_menu_item"));
		switchAccountMenuItem.setText(i18N.getTranslation("switch_menu_item"));
		exit.setText(i18N.getTranslation("exit_menu_item"));
		reloadMenuTask.setText(i18N.getTranslation("reload_menu_item"));
		preferences.setText(i18N.getTranslation("preferences_menu_item"));
		newNote.setText(i18N.getTranslation("new_note_menu_item"));
		newFolder.setText(i18N.getTranslation("new_folder_menu_item"));
		
		final MenuBar menuBar = new MenuBar();
		final Menu menu = new Menu(i18N.getTranslation("file_menu"));
		final Menu editMenu = new Menu(i18N.getTranslation("edit_menu"));
		final Menu helpMenu = new Menu(i18N.getTranslation("help_menu"));
		final MenuItem about = new MenuItem(i18N.getTranslation("about_menu"));

        editMenu.getItems().add(find);
                
		menuBar.getMenus().add(menu);
        menuBar.getMenus().add(editMenu);
        menuBar.getMenus().add(helpMenu);
        helpMenu.getItems().add(about);
        
		menu.getItems().addAll(newNote, newFolder, update,
								new SeparatorMenuItem(), reloadMenuTask, switchAccountMenuItem, 
								new SeparatorMenuItem(), preferences,
								new SeparatorMenuItem(), exit);

		final GridPane statusPane = new GridPane();
		statusPane.setPadding(new Insets(5));
		statusPane.add(account, 0, 0);
		statusPane.add(status, 1, 0);		
		GridPane.setHalignment(status, HPos.RIGHT);
		ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
		column1.setPercentWidth(50);
		column2.setPercentWidth(50);        
        statusPane.getColumnConstraints().addAll(column1, column2);


		final SplitPane sp = new SplitPane(new StackPane(noteCB), tp);
		sp.setOrientation(Orientation.HORIZONTAL);
		sp.setDividerPositions(0.3);
		
		setCenter(sp);
		setBottom(statusPane);
		setTop(menuBar);
		
		// TODO in den Controller verlagern
		about.setOnAction( e-> {
			final AboutBox aboutBox = context.getBean(AboutBox.class);
			aboutBox.showAndWait();
		});

	}
}
