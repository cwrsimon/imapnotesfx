import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class CodeEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  final WebView webview = new WebView();


  public String getHTMLContent() {
    final String editingCode = (String) webview.getEngine().
          executeScript("getQuillContent();");
    return editingCode;
  }

  public void setHTMLContent(String content) {
    webview.getEngine().executeScript("setQuillContent('" + content +  "');");
  }

  CodeEditor() {

    webview.setPrefSize(650, 325);
    webview.setMinSize(650, 325);
    String content = CodeEditor.class.getResource("bla.html").toExternalForm();
    webview.getEngine().load(content);

    this.getChildren().add(webview);
  }
}
