package de.wesim.imapnotes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class MyScene extends Scene {

    public MyScene(Parent root) {
        super(root);
        setFontSize((Pane) root);
    }

    public static void setFontSize(Pane pane) {
        if (System.getProperties().containsKey("fx-font-size")) {
            final String newStyle = "-fx-font-size: " + System.getProperty("fx-font-size") + ";";
            pane.setStyle(newStyle);
        }
    }
}
