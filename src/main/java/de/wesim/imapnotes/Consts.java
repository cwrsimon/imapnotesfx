package de.wesim.imapnotes;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Consts {

	public static final Path APP_DIRECTORY = Paths.get(System.getProperty("user.home"))
			.resolve(".imapnotesfx");
	
    public static final Path LOG_FILE =  APP_DIRECTORY.resolve("app.log");

    public static final Path USER_CONFIGURATION_FILE = APP_DIRECTORY
    		.resolve("config.properties");
    
    public static final Path KEYSTORE_PATH = APP_DIRECTORY.resolve(
            ".imapnotesfx.keystore");

    public static final String KEYSTORE_SERVICE_NAME = "imapnotesfx";

    public static final String EMPTY_NOTE
            = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";

    // private String parse(String htmlContent) {
    // final String plainContent = Jsoup.parse(htmlContent).text();
    // return plainContent;
    // }
}
