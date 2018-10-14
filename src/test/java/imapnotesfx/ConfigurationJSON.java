package imapnotesfx;

import java.util.UUID;

import com.google.gson.Gson;

import de.wesim.imapnotes.models.Configuration;
import de.wesim.imapnotes.models.Note;
import de.wesim.imapnotes.services.ConfigurationService;

public class ConfigurationJSON {

	public static void main(String[] args) {
		final ConfigurationService cs = new ConfigurationService();
		final Configuration conf = cs.readConfig();
		
		Gson gson = new Gson();
		String json = gson.toJson(conf);
		System.out.println(json);
		
		String noteContent = "{\"uuid\":\"f89c378c-356f-429a-8dc8-78c87737a8bb\",\"content\":\"\\u003chtml\\u003eblaöäüß\\u003c/html\\u003e\",\"isFolder\":false}" ;
		gson = new Gson();
		Note bla = gson.fromJson(noteContent, Note.class);
		System.out.println(bla.toString());
	}

}
