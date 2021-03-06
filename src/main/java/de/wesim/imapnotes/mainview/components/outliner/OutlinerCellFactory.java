package de.wesim.imapnotes.mainview.components.outliner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

@Component
public class OutlinerCellFactory implements Callback<TreeView<Note>, TreeCell<Note>>{

	@Autowired
	@Qualifier("mainViewController")
    private MainViewController caller;
	
	@Autowired
    private ApplicationContext context;
    
	public OutlinerCellFactory() {
    }


	@Override
	public TreeCell<Note> call(TreeView<Note> param) {
		return context.getBean(OutlinerTreeCell.class, caller);
	}

}