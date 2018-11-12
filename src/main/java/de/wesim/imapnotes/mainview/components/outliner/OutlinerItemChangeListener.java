package de.wesim.imapnotes.mainview.components.outliner;

import de.wesim.imapnotes.mainview.MainViewController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

public class OutlinerItemChangeListener implements ChangeListener<Boolean> {

    private MainViewController controller;

	public OutlinerItemChangeListener(MainViewController controller) {
        this.controller = controller;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
            Boolean newValue) {
    	
        if (!newValue) {
            return;
        }

        // get TreeItem object for which the event was triggered 
        // https://stackoverflow.com/questions/14236666/how-to-get-current-treeitem-reference-which-is-expanding-by-user-click-in-javafx#14241151
        final BooleanProperty bb = (BooleanProperty) observable;
        @SuppressWarnings("unchecked")
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