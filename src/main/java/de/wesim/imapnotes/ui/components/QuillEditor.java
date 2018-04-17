package de.wesim.imapnotes.ui.components;

import org.apache.commons.text.StringEscapeUtils;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class QuillEditor extends StackPane {
  /** a webview used to encapsulate the CodeMirror JavaScript. */
  final WebView webview = new WebView();


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
