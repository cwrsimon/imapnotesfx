package de.wesim.imapnotes.mainview.components;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import de.wesim.imapnotes.models.Configuration;
import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

// TODO 
// Syntax-Highlighting 
// Font-Größe konfigurabel machen
// Keyboard-Shortcuts für Historie
// Keyboard-Shortcurts ins Menü bringen ...

public class QuillEditor extends StackPane {
	final WebView webview = new WebView();
	private static final Logger log = LoggerFactory.getLogger(QuillEditor.class);

	public String getFullHTMLContent() {
		final String editingCode = (String) webview.getEngine().executeScript("getQuillContent();");

		return "<html><head></head><body contenteditable=\"true\">" + editingCode + "</body></html>";
	}

	public String getHTMLContent() {
		final String editingCode = (String) webview.getEngine().executeScript("getQuillContent();");
		return editingCode;
	}

	public String getHtmlText() {
		return this.getHTMLContent();
	}

	public void setHTMLContent(String content) {
		final String content_js = StringEscapeUtils.escapeEcmaScript(content);
		webview.getEngine().executeScript("setQuillContent('" + content_js + "');");
		setContentUpdate(false);
	}

	public void setHtmlText(String content) {
		//log.info("Setting neu content: {}", content);
		this.setHTMLContent(content);
	}

	private BooleanProperty contentUpdate = new SimpleBooleanProperty(false);
	
	public final BooleanProperty contentUpdateProperty() {
		 return contentUpdate; 
	}

	public void logMe(String message) {
		log.error("{}", message);
	}

	public final void setContentUpdate(boolean newValue) {	
		//	log.info("Updating content for  {} with {}", toString(), newValue); 
		contentUpdate.set(newValue);
	}

	public final boolean getContentUpdate() {
		return contentUpdate.get();
	}

	public void setCssStyle(String styleName, String value) {
		webview.getEngine().executeScript("document.getElementById('editor').style['" + styleName + "']='" 
			+ value + "';");
	}

	public QuillEditor(HostServices hostServices, String string, Configuration configuration) {
		final QuillEditor backReference = this;
		// TODO WebView muss sich dem verfügbaren Platz anpassen
		webview.setPrefSize(770, 325);
		webview.setMinSize(770, 325);
		webview.getEngine().setOnError( e-> {
			WebErrorEvent event = (WebErrorEvent) e;
			event.getException().printStackTrace();
			log.error("{}",e.getMessage() );
		});
		final String content = QuillEditor.class.getResource("/quill-editor.html").toExternalForm();
		webview.getEngine().load(content);
		// FIXME
		// https://bugs.java.com/view_bug.do?bug_id=8197790
		this.getChildren().add(webview);
		// TODO Hübscher machen ...
		webview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				if (newValue == State.SUCCEEDED) {
					JSObject window = (JSObject) webview.getEngine().executeScript("window");
					window.setMember("app", backReference);
					//webview.getEngine().executeScript("app.logMe('Gallo');");
					// TODO Auf diese Weise von außen Schriftfamilie und Schriftgröße anpassen ...
//					webview.getEngine().executeScript("document.getElementById('editor').style['font-family']='serif';");
					setCssStyle("font-family", configuration.getFontFamily());
					setCssStyle("font-size", configuration.getFontSize());
					setHtmlText(string);

					Element nodeList = webview.getEngine().getDocument().getElementById("editor");
					EventTarget eventTarget = (EventTarget) nodeList;
					eventTarget.addEventListener("click", new EventListener() {
						@Override
						public void handleEvent(Event evt) {
							EventTarget target = evt.getTarget();
							if (!(target instanceof HTMLAnchorElement))
								return;

							HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
							final String url = anchorElement.getHref();
							final String targetAttr = anchorElement.getTarget();
							final String className = anchorElement.getClassName();
							if (targetAttr.equals("_blank")) {
								evt.preventDefault();
							}
							if (className == null)
								return;
							if (className.length() > 0) {
								hostServices.showDocument(url);

							}
						}
					}, false);
				}
			}
		});
		// webview.setOnKeyPressed( e-> {
		// 	log.info("Key: {}", e.getCode().toString());
		// });
	}

    public void findString(String entered) {
        final String content_js = StringEscapeUtils.escapeEcmaScript(entered);
	webview.getEngine().executeScript("findQuillContent('" + content_js + "');");
    }
}
