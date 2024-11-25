package hdvtdev.academ.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonConfigManager {

    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonConfigManager(Path configPath) {
        this.configPath = configPath;
        try {
            if (Files.notExists(configPath)) {
                Files.createFile(configPath);
                Files.writeString(configPath, "{}");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String property) {
        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            return root.get(property).getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("Property " + property + " not found");
        }
        return null;
    }

    public void setProperty(String property, String value) {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            JsonObject root = new JsonObject();
            root.addProperty(property, value);
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }









}
