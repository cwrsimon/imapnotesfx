package de.wesim.imapnotes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Consts {

	public static final String SHORTCUT_QUIT = "Shortcut+Q";
	public static final String SHORTCUT_SAVE = "Shortcut+S";
	public static final String SHORTCUT_FIND = "Shortcut+F";
	public static final String SHORTCUT_UNDO = "Shortcut+Z";
	public static final String SHORTCUT_REDO = "Shortcut+Shift+Z";
	public static final String SHORTCUT_FIND_NEXT = "Shortcut+K";
	public static final String SHORTCUT_FIND_PREV = "Shortcut+Shift+K";

	
	public static final Path APP_DIRECTORY = Paths.get(System.getProperty("user.home"))
			.resolve(".imapnotesfx");
	
    public static final Path LOG_FILE =  APP_DIRECTORY.resolve("app.log");

    public static final Path USER_CONFIGURATION_FILE = APP_DIRECTORY
    		.resolve("config.properties");

    public static final Path JSON_CONFIGURATION_FILE = APP_DIRECTORY
    		.resolve("imapnotes.json");

    public static final Path KEYSTORE_PATH = APP_DIRECTORY.resolve(
            "keystore.bin");

    public static final String KEYSTORE_SERVICE_NAME = "imapnotesfx";

    public static final String EMPTY_NOTE
            = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";

    public static final List<String> AVAILABLE_FONT_FAMILIES = List.of("sans-serif", "serif", "monospace", "arial", "courier");
	
	public static List<String> AVAILABLE_FONT_SIZE;
	
	static {
		AVAILABLE_FONT_SIZE = new ArrayList<>();
		for (int i=8; i<50; i++) {
			AVAILABLE_FONT_SIZE.add(String.format("%dpx", i));
		}
	}

	public static final String DEFAULT_FONT_SIZE = "17px";

	public static final String DEFAULT_FONT_FAMILY = "sans-serif";

}
