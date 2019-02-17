package de.wesim.imapnotes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Consts {

    	

    

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
