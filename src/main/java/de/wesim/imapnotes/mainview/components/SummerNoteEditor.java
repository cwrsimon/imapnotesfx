package de.wesim.imapnotes.mainview.components;

import com.sun.javafx.webkit.WebConsoleListener;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Configuration;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
// TODO An Summernote anpassen
// TODO Konsolidieren
// Vermutlich muss auf OpenJFX 12 geupgradet werden ...
public class SummerNoteEditor extends StackPane implements HasLogger {

    private final HostServices hostServices;

    void findItems(String entered) {
        final String content_js = StringEscapeUtils.escapeEcmaScript(entered);
        webview.getEngine().executeScript("findOccurrences('" + content_js + "');");
    }

    private class QuillEditorWhenLoadedListener implements ChangeListener<State> {

        private SummerNoteEditor backReference;
        private HostServices hostServices;
        private Configuration configuration;
        private String editorContent;

        public QuillEditorWhenLoadedListener(SummerNoteEditor caller, HostServices hostServices,
                Configuration configuration, String editorContent) {
            this.backReference = caller;
            this.hostServices = hostServices;
            this.configuration = configuration;
            this.editorContent = editorContent;
        }

        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            if (newValue != State.SUCCEEDED) {
                return;
            }

            final JSObject window = (JSObject) webview.getEngine().executeScript("window");
            window.setMember("app", backReference);
            if (this.configuration.getFontFamily() != null) {
                setCssStyle("font-family", this.configuration.getFontFamily());
            }
            if (this.configuration.getFontSize() != null) {
                setCssStyle("font-size", this.configuration.getFontSize());
            }
            setHtmlText(this.editorContent);
            // TODO Konsolidieren
            // zweiten Click-Listener implementieren
            //final String = "note-editable"
            var nodeList = webview.getEngine().getDocument().getElementsByTagName("div");
            Element foundElement = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element curElem = (Element) nodeList.item(i);
                String classValue = curElem.getAttribute("class");
                if (classValue == null) {
                    continue;
                }
                if (classValue.contains("note-editable")) { // note-link-popover
                    foundElement = curElem;
                    break;
                }
            }

            final EventTarget eventTarget = (EventTarget) foundElement;
            eventTarget.addEventListener("click", evt -> {

                final EventTarget target = evt.getTarget();
                if (!(target instanceof HTMLAnchorElement)) {
                    return;
                }

                final HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                final String url = anchorElement.getHref();
                final String targetAttr = anchorElement.getTarget();
                if (targetAttr.equals("_blank")) {
                    evt.preventDefault();
                }
            }, false);

            nodeList = webview.getEngine().getDocument().getElementsByTagName("div");
            foundElement = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element curElem = (Element) nodeList.item(i);
                String classValue = curElem.getAttribute("class");
                if (classValue == null) {
                    continue;
                }
                if (classValue.contains("note-link-popover")) { // note-link-popover
                    foundElement = curElem;
                    break;
                }
            }

            var eventTarget2 = (EventTarget) foundElement;
            eventTarget2.addEventListener("click", evt -> {

                final EventTarget target = evt.getTarget();
                if (!(target instanceof HTMLAnchorElement)) {
                    return;
                }

                final HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
                final String url = anchorElement.getHref();
                final String targetAttr = anchorElement.getTarget();
                if (targetAttr.equals("_blank")) {
                    evt.preventDefault();
                }
                // open url with host system's default
                if (url.length() > 0) {
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
        getLogger().info("Setting content...: {}", content_js);
        webview.getEngine().executeScript("setQuillContent('" + content_js + "');");
        setContentUpdate(false);
    }

    public void setHtmlText(String content) {
        this.setHTMLContent(content);
    }

    private final BooleanProperty contentUpdate = new SimpleBooleanProperty(false);

    public final BooleanProperty contentUpdateProperty() {
        return contentUpdate;
    }

    public void openURL(String url) {
        hostServices.showDocument(url);
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
        webview.getEngine().executeScript("setInlineStyle('"
                + styleName + "','" + value + "');");
    }

    public SummerNoteEditor(HostServices hostServices, String editorContent, Configuration configuration) {
        WebConsoleListener.setDefaultListener((WebView wv, String msg, int i, String source) -> {
            //TODO Konsolidieren
            getLogger().info("Console [{}, {}]: {}", source, i, msg);
        });
        final SummerNoteEditor backReference = this;

        webview.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        webview.getEngine().setOnError(e -> {
            WebErrorEvent event = (WebErrorEvent) e;
            getLogger().error("{}", event.getException());
        });
        final String jqueryURL = SummerNoteEditor.class.getResource("/jquery-3.2.1.slim.min.js").toExternalForm();
        final String summernoteLiteJSURL = SummerNoteEditor.class.getResource("/summernote-lite.js").toExternalForm();
        final String summernoteCSSURL = SummerNoteEditor.class.getResource("/summernote-lite.css").toExternalForm();
        final String markJSURL = SummerNoteEditor.class.getResource("/jquery.mark.min.js").toExternalForm();
        String htmlSource = null;
        try {
            htmlSource = new String(SummerNoteEditor.class.getResource("/summernote.html").openStream().readAllBytes(), "UTF-8");
            htmlSource = htmlSource.replace("%SUMMERNOTE_FONT%", SummerNoteEditor.class.getResource("/summernote.ttf").toExternalForm());
            htmlSource = htmlSource.replace("%JQUERY_URL%", jqueryURL);
            htmlSource = htmlSource.replace("%SUMMERNOTE_LITE_JS_URL%", summernoteLiteJSURL);
            htmlSource = htmlSource.replace("%SUMMERNOTE_LITE_CSS_URL%", summernoteCSSURL);
            htmlSource = htmlSource.replace("%MARK_JS_URL%", markJSURL);

            getLogger().info(htmlSource);
        } catch (IOException ex) {
            Logger.getLogger(SummerNoteEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        webview.getEngine().loadContent(htmlSource);
        this.getChildren().add(webview);

        webview.getEngine().getLoadWorker().stateProperty().addListener(
                new QuillEditorWhenLoadedListener(backReference, hostServices, configuration, editorContent));
        webview.getEngine().getLoadWorker().exceptionProperty().addListener((var ov, var t, var t1) -> {
            // TODO Konsolidieren ...
            getLogger().error("{}", t.getMessage(), t);
            getLogger().error("{}", t1.getMessage(), t1);
        });
        this.hostServices = hostServices;
    }
}
