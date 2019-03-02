package de.wesim.imapnotes.services;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.wesim.imapnotes.HasLogger;
import de.wesim.imapnotes.models.Configuration;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ConfigurationService implements HasLogger {

    @Autowired
    private Path jsonConfigFile;

    private Configuration config = new Configuration();

    public void writeConfig() {
        getLogger().info("Writing config ...");
        final Gson gson = new Gson();
        final String json = gson.toJson(this.config);

        try {
            Files.write(jsonConfigFile, json.getBytes("UTF-8"));
        } catch (IOException e) {
            getLogger().error("Writing configuration file to '{}' failed.", jsonConfigFile, e);
        }
    }

    public void refresh() {
        getLogger().info("Reading config ...");

        final Gson gson = new Gson();
        final Configuration newConfig;
        try {
            newConfig = gson.fromJson(Files.newBufferedReader(jsonConfigFile), Configuration.class);
            this.config = newConfig;
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            getLogger().error("Reading configuration file from '{}' failed.", jsonConfigFile, e);
        }
    }

    public Configuration getConfig() {
        return config;
    }

}
