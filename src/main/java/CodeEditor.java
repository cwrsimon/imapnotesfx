import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class CodeEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  final WebView webview = new WebView();

  /** a snapshot of the code to be edited kept for easy initilization and reversion of editable code. */
  private String editingCode;

  private final String editingTemplate =
    "<!doctype html>" +
    "<html>" +
    "<head>" +
     "  <link rel=\"stylesheet\" href=\"http://localhost:8080/richeditor/src/main/assets/normalize.css\">" +
     "  <link rel=\"stylesheet\" href=\"http://localhost:8080/richeditor/src/main/assets/style.css\">" +

     // "  <script src=\"http://codemirror.net/lib/codemirror.js\"></script>" +
    // "  <script src=\"http://codemirror.net/mode/clike/clike.js\"></script>" +
    "</head>" +
    "<body>" +
     "<form><div id=\"code\" name=\"code\" contenteditable=\"true\">\n" +
    "${code}" +
    "</div></form>" +
    "<script>" +
    " var editor = document.getElementByID(\"code\"); "+
    "   " + 
    " // var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {" +
    "  //  lineNumbers: true," +
    "  //  matchBrackets: true," +
    " //   mode: \"text/x-java\"" +
    " // });" +
    "</script>" +
    "</body>" +
    "</html>";

  /** applies the editing template to the editing code to create the html+javascript source for a code editor. */
  private String applyEditingTemplate() {
    return editingTemplate.replace("${code}", editingCode);
  }

  /** sets the current code in the editor and creates an editing snapshot of the code which can be reverted to. */
  public void setCode(String newCode) {
    this.editingCode = newCode;
    webview.getEngine().loadContent(applyEditingTemplate());
  }

  /** returns the current code in the editor and updates an editing snapshot of the code which can be reverted to. */
  public String getCodeAndSnapshot() {
    Object editingCode = webview.getEngine().
          executeScript("document.getSelection();");
    System.out.println("Selection:"+editingCode);

     editingCode = webview.getEngine().
      executeScript("document.execCommand('createLink', false, document.getSelection());");

    System.out.println(editingCode);
    return String.valueOf(editingCode);
  }

  /** revert edits of the code to the last edit snapshot taken. */
  public void revertEdits() {
    Object editingCode = webview.getEngine().
          executeScript("document.getElementById(\"code\").innerHTML;");
    
        System.out.println(editingCode);

    // System.out.println(webview.getEngine().getDocument().getDocumentElement().getTagName());
    //setCode(editingCode);
  }

  /**
   * Create a new code editor.
   * @param editingCode the initial code to be edited in the code editor.
   */
  CodeEditor(String editingCode) {
    this.editingCode = editingCode;

    webview.setPrefSize(650, 325);
    webview.setMinSize(650, 325);
    webview.getEngine().loadContent(applyEditingTemplate());

    this.getChildren().add(webview);
  }
}
