package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

// Was es editierbar werden soll:
// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/tree-view.htm#BABDEADA
public class ListCellFactory implements Callback<ListView<Note>, ListCell<Note>>{

    private NoteController caller;

	public ListCellFactory(NoteController caller) {
        this.caller = caller;
    }

	@Override
	public ListCell<Note> call(ListView<Note> arg0) {
		return new ListCellImpl(caller);
	}

}