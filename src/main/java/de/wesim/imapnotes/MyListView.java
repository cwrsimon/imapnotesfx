package de.wesim.imapnotes;

import de.wesim.imapnotes.models.Note;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class MyListView extends ListView<Note> {

    private NoteController controller;
	private boolean overrideOpening;

	//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html
		//https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TabPane.html
		// https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm#CACCFEFD
        // TODO Hier Kontextmenüs etc. hinzufügen
        
	public MyListView(NoteController controller) {
        this.controller = controller;
		this.setCellFactory(new ListCellFactory(controller));
//		this.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
//			MouseEvent me = (MouseEvent) e;
//			//Note selected = getItem();
//
//			System.out.println("Mouse-Event:" + e.getClass().getName());
//			//System.out.println("Mouse-Event:" + selected.getSubject());
//			e.consume();
//		});
		
		this.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Note>() {
			@Override
			public void changed(ObservableValue<? extends Note> observable, 
					Note oldValue, Note newValue) {
						if (overrideOpening) {
							overrideOpening = false;
							return;
						}
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
				//controller.openNote(oldValue, newValue);
			}
		});
		this.overrideOpening = false;
    }
	public void toggleOverrideOpening() {
		this.overrideOpening = true;
	}
}