import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    // UI Constants
    private static final String UI_BORDER = "═".repeat(60);

    // Date format used by the system
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Scanner to read user's input
    private static Scanner consoleScanner = new Scanner(System.in);

    // Main system engine
    private static SchedulizeSystem systemEngine = new SchedulizeSystem();

    // Main entry point
    public static void main(String[] args) {

        System.out.println("\n" + UI_BORDER);
        System.out.println("     WELCOME TO SCHEDULIZE");
        System.out.println(UI_BORDER);

        double dailyStudyHours = fallbackReadDouble("Set study hours per day: ", 0.5, 24.0);

        while (true) {
            displayInteractiveMenu(dailyStudyHours);
            int administrativeChoice = fallbackReadInt("Select Choice: ");

            switch (administrativeChoice) {
                case 1 -> triggerInteractiveAddTask();
                case 2 -> triggerInteractiveRemoveTask();
                case 3 -> systemEngine.displayPrioritizedTasks();
                case 4 -> systemEngine.generateStudyPlan(dailyStudyHours);
                case 5 -> dailyStudyHours = fallbackReadDouble("Update study hours per day: ", 0.5, 24.0);
                case 6 -> BenchmarkTool.runBenchmarkMenu(consoleScanner);
                case 7 -> {
                    System.out.println("\n  Goodbye!");
                    consoleScanner.close();
                    return;
                }
                default -> System.out.println("  Invalid choice range. Choose an option between (1-7).");
            }
        }
    }

    private static void displayInteractiveMenu(double currentConfigHours) {
        System.out.println("\n  [1] Add New Task");
        System.out.println("  [2] Remove Existing Task");
        System.out.println("  [3] View Priority Ranking Queue Summary");
        System.out.println("  [4] Generate Study Plan");
        System.out.println("  [5] Reconfigure Daily Available Study Hours (Current: " + currentConfigHours + "h)");
        System.out.println("  [6] Complexity Analyser & Benchmark Tool");
        System.out.println("  [7] Exit");
    }

    private static void triggerInteractiveAddTask() {
        System.out.println("\n--- ADD NEW TASK ---");

        System.out.print("Enter Task Name: ");
        String name = consoleScanner.nextLine().trim();

        while (name.isEmpty()) {
            System.out.print("Task name cannot be empty. Try again: ");
            name = consoleScanner.nextLine().trim();
        }

        LocalDate targetDeadline = null;

        while (targetDeadline == null) {
            System.out.print("Enter deadline date (Format: DD/MM/YYYY): ");
            String rawDateInput = consoleScanner.nextLine().trim();

            try {
                LocalDate parsedDate = LocalDate.parse(rawDateInput, DATE_PARSER);

                if (parsedDate.isBefore(LocalDate.now())) {
                    System.out.println("  [Notice] Input date is in the past. Try again.");
                    continue;
                }

                targetDeadline = parsedDate;

            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use DD/MM/YYYY.");
            }
        }

        double estimatedHours = fallbackReadDouble("Enter estimated completion time in hours: ", 0.1, 1000.0);

        Task.Difficulty difficulty = readDifficulty();

        double gradePercent = fallbackReadDouble("Enter grade percentage: ", 0.0, 100.0);

        Task.Type type = readTaskType();

        Task newTask = new Task(name, targetDeadline, estimatedHours, difficulty, gradePercent, type);

        systemEngine.addTask(newTask);

        System.out.println("\n  Task added successfully!");
    }

    private static void triggerInteractiveRemoveTask() {
        if (systemEngine.isEmpty()) {
            System.out.println("\n  No task available to remove.");
            return;
        }

        System.out.print("\nEnter task name to remove: ");
        String taskName = consoleScanner.nextLine().trim();

        boolean removed = systemEngine.removeTask(taskName);

        if (removed) {
            System.out.println("  Task removed successfully.");
        } else {
            System.out.println("  Task not found.");
        }
    }

    private static Task.Difficulty readDifficulty() {
        while (true) {
            System.out.println("\nSelect difficulty:");
            System.out.println("  [1] EASY");
            System.out.println("  [2] MEDIUM");
            System.out.println("  [3] HARD");

            int choice = fallbackReadInt("Choose difficulty (1-3): ");

            switch (choice) {
                case 1:
                    return Task.Difficulty.EASY;
                case 2:
                    return Task.Difficulty.MEDIUM;
                case 3:
                    return Task.Difficulty.HARD;
                default:
                    System.out.println("  Invalid choice. Choose 1-3.");
            }
        }
    }

    private static Task.Type readTaskType() {
        while (true) {
            System.out.println("\nSelect task type:");
            System.out.println("  [1] ASSIGNMENT");
            System.out.println("  [2] PROJECT");
            System.out.println("  [3] QUIZ");
            System.out.println("  [4] EXAM");
            System.out.println("  [5] OTHER");

            int choice = fallbackReadInt("Choose task type (1-5): ");

            switch (choice) {
                case 1:
                    return Task.Type.ASSIGNMENT;
                case 2:
                    return Task.Type.PROJECT;
                case 3:
                    return Task.Type.QUIZ;
                case 4:
                    return Task.Type.EXAM;
                case 5:
                    return Task.Type.OTHER;
                default:
                    System.out.println("  Invalid choice. Choose 1-5.");
            }
        }
    }

    private static int fallbackReadInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                String input = consoleScanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Enter a whole number.");
            }
        }
    }

    private static double fallbackReadDouble(String prompt, double lowerConstraint, double upperConstraint) {
        while (true) {
            System.out.print(prompt);

            try {
                String input = consoleScanner.nextLine().trim();
                double value = Double.parseDouble(input);

                if (value < lowerConstraint || value > upperConstraint) {
                    System.out.println("  Input must be between " + lowerConstraint + " and " + upperConstraint + ".");
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Enter a number.");
            }
        }
    }
}