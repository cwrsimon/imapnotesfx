package de.wesim.imapnotes.ui.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

// Was es editierbar werden soll:
// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/tree-view.htm#BABDEADA
@Component
public class ListCellFactory implements Callback<TreeView<Note>, TreeCell<Note>>{

	@Autowired
	@Qualifier("noteController")
    private NoteController caller;

	public ListCellFactory() {
      //  this.caller = caller;
    }


	@Override
	public TreeCell<Note> call(TreeView<Note> param) {
		return new MyTreeCell(caller);
	}

}