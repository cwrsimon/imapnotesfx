import de.wesim.imapnotes.ui.components.QuillEditor;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * An example application which demonstrates use of a
 * CodeMirror based JavaScript CodeEditor wrapped in
 * a JavaFX WebView.
 */
public class CodeEditorExample extends Application {
  // some sample code to be edited.
  static final private String editingCode =
    "Bla";

  public static void main(String[] args) { launch(args); }
  
  @Override
  public void start(Stage stage) throws Exception {
    // create the editing controls.
    Label title = new Label("Editing: CodeEditor.java");
    final Label labeledCode = new Label(editingCode);
    final QuillEditor editor = new QuillEditor();
    final Button revertEdits = new Button("Get");
    revertEdits.setOnAction(e -> {
      System.out.println(editor.getFullHTMLContent());
    });
    
    final Button copyCode = new Button(
      "set Content"
    );
    copyCode.setOnAction(e -> {
      editor.setHTMLContent("<p><a href=\"https://www.faz.net\">FAZ</a></p>");
    });

    // layout the scene.
    HBox hbox = new HBox(copyCode, revertEdits);
    hbox.setSpacing(10);

    final VBox layout =  new VBox(title, editor, hbox, labeledCode);
    layout.setSpacing(10);
   // layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");

    // display the scene.
    final Scene scene = new Scene(layout);
    stage.setScene(scene);
    stage.show();
  }
}