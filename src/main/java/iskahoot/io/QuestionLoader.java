package iskahoot.io;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import iskahoot.model.QuestionsFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class QuestionLoader {
    public static QuestionsFile loadFromResource(String path){
        try {
            InputStream in = QuestionLoader.class.getResourceAsStream(path);
            if (in == null) throw new RuntimeException("Resource not found: " + path);
            try (JsonReader r = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return new Gson().fromJson(r, QuestionsFile.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load questions: " + e.getMessage(), e);
        }
    }
}
