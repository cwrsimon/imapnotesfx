package de.wesim.imapnotes.ui.components;

import de.wesim.imapnotes.NoteController;
import de.wesim.imapnotes.models.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

public class MyTreeItemChangeListener implements ChangeListener<Boolean> {

    private NoteController controller;

	public MyTreeItemChangeListener(NoteController controller) {
        this.controller = controller;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
            Boolean newValue) {
        if (!newValue) {
            return;
        }

        BooleanProperty bb = (BooleanProperty) observable;

        TreeItem<Note> callee = (TreeItem<Note>) bb.getBean();
        if (callee.getChildren().size() != 1)
            return;
        // nur bei einem einzigen leeren Kind
        if (callee.getChildren().get(0).getValue() != null)
            return;
        this.controller.openFolder(callee);
    }
}