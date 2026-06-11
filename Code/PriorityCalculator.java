import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PriorityCalculator {

    public static double calculate(Task task) {

        //To calculate how many days left until deadline
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), task.getDeadline());
        double effectiveDays = Math.max(daysLeft, 0.1);

        // 1. Urgency metric
        // The closer the deadline, the higher the urgency
        double urgency = 10.0 / effectiveDays;

        // 2. Impact metric
        // Higher percentage, means higher importance
        double impact = (task.getGradePercent() / 100.0) * 10.0;

        // 3. Effort metric
        int diffWeight = switch (task.getDifficulty()) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
        };
        // Effort score is based on estimated hours and difficulty
        // More difficult and longer tasks, gets higher effort value
        double effort = (task.getEstimatedHours() / 8.0) * diffWeight;

        // 40% Urgency, 40% Impact, 20% Effort
        return (urgency * 0.4) + (impact * 0.4) + (effort * 0.2);
    }
}