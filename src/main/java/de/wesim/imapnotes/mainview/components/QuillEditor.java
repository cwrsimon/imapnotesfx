package de.wesim.imapnotes.mainview.components;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import de.wesim.imapnotes.HasLogger;
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

// Check for updates:
// https://bugs.java.com/view_bug.do?bug_id=8197790
public class QuillEditor extends StackPane implements HasLogger {

	private class QuillEditorWhenLoadedListener implements ChangeListener<State> {

		private QuillEditor backReference;
		private HostServices hostServices;
		private Configuration configuration;
		private String editorContent;

		public QuillEditorWhenLoadedListener(QuillEditor caller, HostServices hostServices,
				Configuration configuration, String editorContent) {
			this.backReference = caller;
			this.hostServices = hostServices;
			this.configuration = configuration;
			this.editorContent = editorContent;
		}

		@Override
		public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
			if (newValue != State.SUCCEEDED) return;

			final JSObject window = (JSObject) webview.getEngine().executeScript("window");
			window.setMember("app", backReference);
			setCssStyle("font-family", this.configuration.getFontFamily());
			setCssStyle("font-size", this.configuration.getFontSize());
			setHtmlText(this.editorContent);

			Element nodeList = webview.getEngine().getDocument().getElementById("editor");
			EventTarget eventTarget = (EventTarget) nodeList;
			// event listener for opening external links
			eventTarget.addEventListener("click", evt -> {

				final EventTarget target = evt.getTarget();
				if (!(target instanceof HTMLAnchorElement))
					return;

				final HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
				final String url = anchorElement.getHref();
				final String targetAttr = anchorElement.getTarget();
				final String className = anchorElement.getClassName();
				if (targetAttr.equals("_blank")) {
					evt.preventDefault();
				}
				if (className == null)
					return;
				// open url with host system's default
				if (className.length() > 0) {
					hostServices.showDocument(url);
				}
				
			}, false);
		}
	}


	final WebView webview = new WebView();

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
		this.setHTMLContent(content);
	}

	private BooleanProperty contentUpdate = new SimpleBooleanProperty(false);

	public final BooleanProperty contentUpdateProperty() {
		return contentUpdate; 
	}

	public void logMe(String message) {
		getLogger().error("{}", message);
	}

	public final void setContentUpdate(boolean newValue) {	
		contentUpdate.set(newValue);
	}

	public final boolean getContentUpdate() {
		return contentUpdate.get();
	}

	public void setCssStyle(String styleName, String value) {
		webview.getEngine().executeScript("document.getElementById('editor').style['" + styleName + "']='" 
				+ value + "';");
	}

	public QuillEditor(HostServices hostServices, String editorContent, Configuration configuration) {
		final QuillEditor backReference = this;
		webview.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		webview.getEngine().setOnError( e-> {
			WebErrorEvent event = (WebErrorEvent) e;
			getLogger().error("{}", event.getException());
		});
		final String editorSource = QuillEditor.class.getResource("/quill-editor.html").toExternalForm();
		webview.getEngine().load(editorSource);
		this.getChildren().add(webview);
		// bootstrap quill editor
		webview.getEngine().getLoadWorker().stateProperty().addListener(
				new QuillEditorWhenLoadedListener(backReference, hostServices, configuration, editorContent));
	}

	public void findString(String entered) {
		final String content_js = StringEscapeUtils.escapeEcmaScript(entered);
		webview.getEngine().executeScript("findQuillContent('" + content_js + "');");
	}
}
