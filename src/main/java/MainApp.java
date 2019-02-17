
import de.wesim.imapnotes.JFXMain;
import java.io.IOException;

// Run this class from within your IDE.
public class MainApp {

	public static void main(String[] args) throws IOException {
		// improved font rendering with anti aliasing under Linux
                System.setProperty("prism.lcdtext", "false");
      		System.setProperty("prism.subpixeltext", "false");
                
		// call actual JavaFX main app
		// thanks to https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
		JFXMain.main(args);
	}
}
