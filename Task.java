import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // To measure time differences

public class Task implements Comparable<Task> {

    // Uses enum to categorize task difficulty and type
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Type { ASSIGNMENT, PROJECT, QUIZ, EXAM, OTHER }

    // Task attributes
    private String name;
    private LocalDate deadline;
    private double estimatedHours;
    private Difficulty difficulty;
    private double gradePercent;
    private Type type;
    private double priorityScore;

    public Task(String name, LocalDate deadline, double estimatedHours, Difficulty difficulty, double gradePercent, Type type) {
        this.name = name;
        this.deadline = deadline;
        this.estimatedHours = estimatedHours;
        this.difficulty = difficulty;
        this.gradePercent = gradePercent;
        this.type = type;
        recalculatePriority();
    }

    // To recalculate the priority score from "PriorityCalculator" class
    public void recalculatePriority() {
        this.priorityScore = PriorityCalculator.calculate(this);
    }

    // Convert numeric form of priority score into words
    public String getPriorityLabel() {
        if (this.priorityScore > 4.0) return "HIGH";
        if (this.priorityScore > 2.0) return "MEDIUM";
        return "LOW";
    }

    // Returns the number of days left until the deadline
    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), this.deadline);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
        recalculatePriority();
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
        recalculatePriority();
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        recalculatePriority();
    }

    public double getGradePercent() {
        return gradePercent;
    }

    public void setGradePercent(double gradePercent) {
        this.gradePercent = gradePercent;
        recalculatePriority();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    // Allows tasks to be sorted automatically by priority score
    @Override
    public int compareTo(Task other) {
        // Descending order, highest priority score comes first
        return Double.compare(other.priorityScore, this.priorityScore);
    }

    // Converts task object into string representation
    @Override
    public String toString() {
        long dl = getDaysLeft();
        String deadlineStr = (dl < 0) ? "OVERDUE" : dl + "d left";

        return String.format(
                "%-25s | %-12s | %5.1fh | %-7s | %5.1f%% | %s (%s)",
                name, type, estimatedHours, difficulty, gradePercent, getPriorityLabel(), deadlineStr
        );
    }
}