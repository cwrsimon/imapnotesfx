package de.wesim.imapnotes.ui.components;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class QuillEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  final WebView webview = new WebView();
  private static final Logger log = LoggerFactory.getLogger(QuillEditor.class);


  public String getFullHTMLContent() {
    final String editingCode = (String) webview.getEngine().
          executeScript("getQuillContent();");
    return "<html><head></head><body contenteditable=\"true\">" + editingCode + "</body></html>";
  }

  public String getHTMLContent() {
    final String editingCode = (String) webview.getEngine().
          executeScript("getQuillContent();");
    return editingCode;
  }
  
  public String getHtmlText() {
	    return this.getHTMLContent();
	  }

  public void setHTMLContent(String content) {
    final String content_js = StringEscapeUtils.escapeEcmaScript(content);
    webview.getEngine().executeScript("setQuillContent('" + content_js +  "');");
  }
  
  public void setHtmlText(String content) {
    log.info("Setting neu content: {}", content);
    //final String newContent = "<div><b>Kündigungen</b></div><div>&nbsp;Thalia.de</div><div><br></div><div>R&amp;V : vor dem 1.5. 2017!</div><div>Huk Auslandskrankenversicherung demnächst!!! ?</div><div><br></div><div>Facebook (endgültig!)</div><div>Yahoo</div><div>Wesim -&gt; Alternative ?</div><p><ul><li>CWRSimon@gmail.com</li></ul></p><p><br></p><div>ticketonline</div><div>die zeit</div><div><br></div>";
	  this.setHTMLContent(content);
  }

  public QuillEditor() {

    webview.setPrefSize(650, 325);
    webview.setMinSize(650, 325);
    String content = QuillEditor.class.getResource("/quill-editor.html").toExternalForm();
    webview.getEngine().load(content);

    this.getChildren().add(webview);
  }
}
