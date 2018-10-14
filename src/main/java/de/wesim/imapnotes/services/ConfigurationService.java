package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.wesim.imapnotes.Consts;
import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Configuration;

@Service
public class ConfigurationService implements HasLogger {


	public void writeConfig(Configuration config) {
		getLogger().info("Writing config ...");
		Gson gson = new Gson();
		String json = gson.toJson(config);

		try {
			Files.write(Consts.JSON_CONFIGURATION_FILE, json.getBytes("UTF-8"));
		} catch (IOException e) {
			getLogger().error("Writing configuration file to '{}' failed.", Consts.JSON_CONFIGURATION_FILE, e );
		}

	}

    public Configuration readConfig() {
		getLogger().info("Writing config ...");

		Gson gson = new Gson();
		Configuration newConfig;
		try {
			newConfig = gson.fromJson(Files.newBufferedReader(Consts.JSON_CONFIGURATION_FILE), Configuration.class);
			return newConfig;
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return null;
    }

}