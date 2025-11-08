package iskahoot.model;

import java.util.List;

public class Question {
    public String question;
    public int points;
    public int correct; // 0-based index
    public java.util.List<String> options;
}
