import java.util.ArrayList;
import java.util.PriorityQueue;

public class SchedulizeSystem {
    // ArrayList to store all task objects
    private ArrayList<Task> taskList;

    // To initialize the task list
    public SchedulizeSystem() {
        this.taskList = new ArrayList<>();
    }

    public void addTask(Task task) {
        taskList.add(task);
    }


    public boolean removeTask(String name) {
        return taskList.removeIf(t -> t.getName().equalsIgnoreCase(name.trim()));
    }

    public ArrayList<Task> getAllTasks() {
        return this.taskList;
    }

    public boolean isEmpty() {
        return taskList.isEmpty();
    }

    // Creates a priority queue to automate task prioritization
    public PriorityQueue<Task> createPriorityQueue() {
        PriorityQueue<Task> queue = new PriorityQueue<>();
        for (Task t : taskList) {
            // Recalculate priority score for each task
            t.recalculatePriority();
            queue.add(t);
        }
        return queue;
    }

    // Displays the current prioritized task list
    public void displayPrioritizedTasks() {
        if (isEmpty()) {
            System.out.println("\n  No task available.");
            return;
        }

        // Create a priority queue for task prioritization
        PriorityQueue<Task> queue = createPriorityQueue();
        String divider = "─".repeat(85);

        System.out.println("\n" + divider);
        System.out.printf("  %-3s | %-25s | %-12s | %-6s | %-7s | %-6s | %s%n",
                "No", "Task Name", "Type", "Hours", "Diff", "Grade", "Priority Status");
        System.out.println(divider);

        int counter = 1;

        // Remove tasks one by one from queue
        // Highest priority appears first
        while (!queue.isEmpty()) {
            Task t = queue.poll();
            System.out.printf("  %02d  | %-25s | %-12s | %5.1fh | %-7s | %5.1f%% | %s (Score: %.2f)%n",
                    counter++, t.getName(), t.getType(), t.getEstimatedHours(),
                    t.getDifficulty(), t.getGradePercent(), t.getPriorityLabel(), t.getPriorityScore());
        }
        System.out.println(divider);
    }

    // Generate daily study plan
    public void generateStudyPlan(double studyHoursPerDay) {
        StudyTime planner = new StudyTime();
        planner.generateSchedule(this.taskList, studyHoursPerDay);
    }
}