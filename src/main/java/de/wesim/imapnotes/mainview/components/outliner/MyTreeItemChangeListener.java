package de.wesim.imapnotes.mainview.components.outliner;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

public class MyTreeItemChangeListener implements ChangeListener<Boolean> {

    private MainViewController controller;

	public MyTreeItemChangeListener(MainViewController controller) {
        this.controller = controller;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
            Boolean newValue) {
    	
        if (!newValue) {
            return;
        }

        // get TreeItem object for which the event was triggered 
        final BooleanProperty bb = (BooleanProperty) observable;
        final TreeItem<Note> callee = (TreeItem<Note>) bb.getBean();
        if (callee.getChildren().size() != 1)
            return;
        
        // ignore leaves ...
        if (callee.getChildren().get(0).getValue() != null)
            return;
        // ... and only open folders
        this.controller.openFolder(callee);
    }
}