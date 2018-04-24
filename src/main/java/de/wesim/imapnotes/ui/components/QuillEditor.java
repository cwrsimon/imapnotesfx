package de.wesim.imapnotes.ui.components;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;

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

    Element nodeList = webview.getEngine().getDocument().getElementById("editor");
    // for (int i = 0; i < nodeList.getLength(); i++)
    // {
    //     Node node= nodeList.item(i);
        EventTarget eventTarget = (EventTarget) nodeList;
        eventTarget.addEventListener("click", new EventListener()
        {
            @Override
            public void handleEvent(Event evt)
            {
                EventTarget target = evt.getCurrentTarget();
                System.out.println("Und klick target:"+target.getClass().getName());
                System.out.println("Und klick target:"+evt.getTarget().getClass().getName());

                // HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                // String href = anchorElement.getHref();
                // //handle opening URL outside JavaFX WebView
                // System.out.println(href);
                evt.preventDefault();
            }
        }, false);

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
    // webview.getEngine().setWe
   // webview.getEngine().locationProperty().
    // webview.getEngine().locationProperty().addListener(new ChangeListener<String>() {

		//   @Override
    //   public void changed(ObservableValue<? extends String> observable, 
    //                           String oldValue, String newValue) {
    //     System.out.println(oldValue);
    //     System.out.println(newValue);
    //    // webview.getEngine().getLoadWorker().cancel();
    //     return;
		//   }
    // });

   
           // }
    //  webview.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {

		// @Override
		// public WebEngine call(PopupFeatures param) {
      
    //   //System.out.println("Popup Handler:" + param.);
		// 	return null;
		// }
      
    // });
  }
}
