package de.wesim.imapnotes.ui.components;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

// Was es editierbar werden soll:
// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/tree-view.htm#BABDEADA
public class ListCellFactory implements Callback<TreeView<Note>, TreeCell<Note>>{

    private NoteController caller;

	public ListCellFactory(NoteController caller) {
        this.caller = caller;
    }


	@Override
	public TreeCell<Note> call(TreeView<Note> param) {
		return new MyTreeView(caller);
	}

}