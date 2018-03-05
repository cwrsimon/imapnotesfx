package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;

public class MyListView extends ListView<Note> {

    private HelloWorld caller;

//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TabPane.html
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD
        // TODO Hier Kontextmenüs etc. hinzufügen
        
	public MyListView(HelloWorld caller) {
        this.caller = caller;
        this.setCellFactory(new ListCellFactory(caller));

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
				caller.openNote(oldValue, newValue);
			}
		});

    }

}