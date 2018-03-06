package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;

public class MyListView extends ListView<Note> {

    private NoteController controller;

//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TabPane.html
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD
        // TODO Hier Kontextmenüs etc. hinzufügen
        
	public MyListView(NoteController controller) {
        this.controller = controller;
        this.setCellFactory(new ListCellFactory(this.controller));

        this.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Note>() {
			@Override
			public void changed(ObservableValue<? extends Note> observable, 
					Note oldValue, Note newValue) {
				if (oldValue == null) {
					System.out.println("oldValue:null");
				} else {
					System.out.println("oldValue:" + oldValue.getSubject());
				}
				if (newValue == null) {
					System.out.println("newValue:null");
				} else {
					System.out.println("newValue:" + newValue.getSubject());
				}
				if (newValue == null)
					return;
				controller.openNote(oldValue, newValue);
			}
		});

    }

}