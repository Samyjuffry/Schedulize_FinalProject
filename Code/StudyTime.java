import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StudyTime {

    public void generateSchedule(ArrayList<Task> tasks, double hoursPerDay) {

        // Stop if no tasks are available
        if (tasks.isEmpty()) {
            System.out.println("\n  No tasks to schedule.");
            return;
        }

        // Create a copy of the task list and recalculate priorities
        ArrayList<Task> sortedList = new ArrayList<>(tasks);
        for (Task t : sortedList) {
            t.recalculatePriority();
        }
        // Sort tasks by priority
        Collections.sort(sortedList);

        // HashMap stores each task name and its remaining hours
        Map<String, Double> remainingHoursMap = new HashMap<>();

        // Put every task into the map with its estimated hours
        for (Task t : sortedList) {
            remainingHoursMap.put(t.getName(), t.getEstimatedHours());
        }

        // Calculate total hours needed and days required to finish the task
        double totalNeededHours = tasks.stream().mapToDouble(Task::getEstimatedHours).sum();
        double absoluteDaysNeeded = totalNeededHours / hoursPerDay;

        String divider = "─".repeat(70);
        System.out.println("\n" + divider);
        System.out.printf("  GENERATED STUDY PLAN (%.1f Hours Allocated Daily)%n", hoursPerDay);
        System.out.println(divider);

        // Warning if workload is too high. Occurs when absoluteDaysNeeded exceeds 30
        if (absoluteDaysNeeded > 30.0) {
            System.out.printf("  [WARNING] High total workload (%.1fh) discovered.%n", totalNeededHours);
            System.out.printf("  Requires ~%.1f calendar days. Think about increasing daily study limits.%n%n", absoluteDaysNeeded);
        }

        int currentDay = 1;
        int safetyMaxDaysLimit = 90; // Prevents an infinite loop scenario if hours remain stuck unallocated

        // Allocate study hours to tasks
        while (sortedList.stream().anyMatch(t -> remainingHoursMap.getOrDefault(t.getName(), 0.0) > 0.0)
               && currentDay <= safetyMaxDaysLimit) {
            
            double dailyAvailableBudget = hoursPerDay;
            boolean workLoggedForDay = false;
            StringBuilder logOutput = new StringBuilder();

            // Loops through each task in the sorted list
            for (Task task : sortedList) {
                if (dailyAvailableBudget <= 0.0) break;

                double remainingTaskHours = remainingHoursMap.getOrDefault(task.getName(), 0.0);
                if (remainingTaskHours <= 0.0) continue;

                // Allocate as much time as possible to the current task, without exceeding the daily budget
                double timeAllocated = Math.min(remainingTaskHours, dailyAvailableBudget);
                timeAllocated = Math.round(timeAllocated * 10.0) / 10.0; // Round to 1 decimal place
                if (timeAllocated <= 0.0) continue;

                if (!workLoggedForDay) {
                    logOutput.append(String.format("  Day %d:", currentDay));
                    workLoggedForDay = true;
                }

                logOutput.append(String.format("%n     ↳ %-30s Allocate: %.1fh", task.getName(), timeAllocated));

                // Update the remaining hours for the task
                remainingHoursMap.put(task.getName(), remainingTaskHours - timeAllocated);
                dailyAvailableBudget -= timeAllocated;
            }

            if (workLoggedForDay) {
                System.out.println(logOutput.toString());
            }
            currentDay++;
        }
        System.out.println(divider);
    }
}