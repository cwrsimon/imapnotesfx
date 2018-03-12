package de.wesim.imapnotes;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Consts {

    public static final Path USER_CONFIGURATION_FILE = Paths.get(System.getProperty("user.home"), 
        ".imapnotesfx.properties");
    public static final Path KEYSTORE_PATH = Paths.get(System.getProperty("user.home"),
        ".imapnotesfx.keystore"
    );

    public static final String KEYSTORE_SERVICE_NAME = "imapnotesfx";

    

}