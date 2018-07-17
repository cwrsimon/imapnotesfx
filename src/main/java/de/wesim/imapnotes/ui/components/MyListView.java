package de.wesim.imapnotes.ui.components;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@Component
public class MyListView extends TreeView<Note> {

	//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TabPane.html
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD
    
	private ListCellFactory listCellFactory;
	
	public MyListView() {
		super(new TreeItem<Note>());
		setShowRoot(false);
		setPrefWidth(150);
    }
	
	@PostConstruct
	public void init() {
		this.setCellFactory(listCellFactory);
	}

}