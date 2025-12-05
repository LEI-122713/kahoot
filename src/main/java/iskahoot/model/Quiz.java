package iskahoot.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Representa um quiz (lista de perguntas) com nome opcional. */
public class Quiz implements Serializable {
    public String name;
    public List<Question> questions;

    public Quiz(String name, List<Question> questions) {
        this.name = name;
        this.questions = Objects.requireNonNullElseGet(questions, List::of);
    }

    public List<Question> safeQuestions() {
        return Collections.unmodifiableList(questions == null ? List.of() : questions);
    }
}
