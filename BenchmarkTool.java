import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

public class BenchmarkTool {

    private static final Scanner benchmarkScanner = new Scanner(System.in);

    public static void runBenchmarkMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(75));
            System.out.println("        SCHEDULIZE COMPLEXITY ANALYSER & BENCHMARK TOOL");
            System.out.println("=".repeat(75));

            System.out.println("\nSelect a Data Structure to evaluate:");
            System.out.println("1. ArrayList Task Storage");
            System.out.println("2. PriorityQueue Task Ranking");
            System.out.println("3. HashMap Remaining Study Time Tracker");
            System.out.println("4. Run All Structures Side-by-Side Comparison");
            System.out.println("5. Back to Main Menu");

            int choice = readInt("\nEnter your choice (1-5): ");

            if (choice == 5) {
                System.out.println("\nReturning to main menu...");
                return;
            }

            int taskCount = readInt("Enter the number of requests/tasks to simulate: ");

            if (taskCount <= 0) {
                System.out.println("Task count must be greater than 0.");
                continue;
            }

            switch (choice) {
                case 1 -> benchmarkArrayList(taskCount);
                case 2 -> benchmarkPriorityQueue(taskCount);
                case 3 -> benchmarkHashMap(taskCount);
                case 4 -> runAllBenchmarks(taskCount);
                default -> System.out.println("\nInvalid choice.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(benchmarkScanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }

    private static ArrayList<Task> generateDummyTasks(int size) {
        ArrayList<Task> tasks = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            Task.Difficulty difficulty;

            if (i % 3 == 0) {
                difficulty = Task.Difficulty.HARD;
            } else if (i % 2 == 0) {
                difficulty = Task.Difficulty.MEDIUM;
            } else {
                difficulty = Task.Difficulty.EASY;
            }

            Task.Type type;

            if (i % 5 == 0) {
                type = Task.Type.EXAM;
            } else if (i % 4 == 0) {
                type = Task.Type.QUIZ;
            } else if (i % 3 == 0) {
                type = Task.Type.PROJECT;
            } else {
                type = Task.Type.ASSIGNMENT;
            }

            Task task = new Task(
                    "Task " + i,
                    LocalDate.now().plusDays((i % 30) + 1),
                    (i % 10) + 1,
                    difficulty,
                    (i % 40) + 10,
                    type
            );

            tasks.add(task);
        }

        return tasks;
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static void runGarbageCollector() {
        System.gc();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void benchmarkArrayList(int taskCount) {
        System.out.println("\n" + "-".repeat(85));
        System.out.println("ARRAYLIST TASK STORAGE BENCHMARK");
        System.out.println("-".repeat(85));

        System.out.println("Simulated tasks: " + taskCount);

        runGarbageCollector();
        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        ArrayList<Task> taskList = new ArrayList<>();

        for (Task task : generateDummyTasks(taskCount)) {
            taskList.add(task);
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        double timeMs = (endTime - startTime) / 1_000_000.0;
        double memoryKb = (memoryAfter - memoryBefore) / 1024.0;

        System.out.printf("%-25s | %-15s | %-15s%n", "Operation", "Time (ms)", "Memory (KB)");
        System.out.println("-".repeat(85));
        System.out.printf("%-25s | %-15.4f | %-15.2f%n", "Add Tasks", timeMs, memoryKb);

        System.out.println("\nComplexity Analysis:");
        System.out.println("Add task              : O(1) average");
        System.out.println("Access by index       : O(1)");
        System.out.println("Traverse all tasks    : O(n)");
        System.out.println("Remove from middle    : O(n)");
    }

    private static void benchmarkPriorityQueue(int taskCount) {
        System.out.println("\n" + "-".repeat(85));
        System.out.println("PRIORITYQUEUE TASK RANKING BENCHMARK");
        System.out.println("-".repeat(85));

        System.out.println("Simulated tasks: " + taskCount);

        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);

        runGarbageCollector();
        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        PriorityQueue<Task> queue = new PriorityQueue<>();

        for (Task task : dummyTasks) {
            task.recalculatePriority();
            queue.add(task);
        }

        while (!queue.isEmpty()) {
            queue.poll();
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        double timeMs = (endTime - startTime) / 1_000_000.0;
        double memoryKb = (memoryAfter - memoryBefore) / 1024.0;

        System.out.printf("%-25s | %-15s | %-15s%n", "Operation", "Time (ms)", "Memory (KB)");
        System.out.println("-".repeat(85));
        System.out.printf("%-25s | %-15.4f | %-15.2f%n", "Add + Poll", timeMs, memoryKb);

        System.out.println("\nComplexity Analysis:");
        System.out.println("Insert task           : O(log n)");
        System.out.println("Poll highest priority : O(log n)");
        System.out.println("Peek highest priority : O(1)");
        System.out.println("Build queue           : O(n log n)");
    }

    private static void benchmarkHashMap(int taskCount) {
        System.out.println("\n" + "-".repeat(85));
        System.out.println("HASHMAP REMAINING STUDY TIME BENCHMARK");
        System.out.println("-".repeat(85));

        System.out.println("Simulated tasks: " + taskCount);

        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);

        runGarbageCollector();
        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        HashMap<String, Double> remainingHoursMap = new HashMap<>();

        for (Task task : dummyTasks) {
            remainingHoursMap.put(task.getName(), task.getEstimatedHours());
        }

        for (Task task : dummyTasks) {
            double currentHours = remainingHoursMap.get(task.getName());
            remainingHoursMap.put(task.getName(), Math.max(0, currentHours - 1));
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        double timeMs = (endTime - startTime) / 1_000_000.0;
        double memoryKb = (memoryAfter - memoryBefore) / 1024.0;

        System.out.printf("%-25s | %-15s | %-15s%n", "Operation", "Time (ms)", "Memory (KB)");
        System.out.println("-".repeat(85));
        System.out.printf("%-25s | %-15.4f | %-15.2f%n", "Put + Update", timeMs, memoryKb);

        System.out.println("\nComplexity Analysis:");
        System.out.println("Put remaining hours   : O(1) average");
        System.out.println("Get remaining hours   : O(1) average");
        System.out.println("Update remaining time : O(1) average");
        System.out.println("Remove entry          : O(1) average");
        System.out.println("Ordering              : Not maintained");
    }

    private static void runAllBenchmarks(int taskCount) {
        System.out.println("\n" + "=".repeat(95));
        System.out.println("SCHEDULIZE SIDE-BY-SIDE BENCHMARK COMPARISON");
        System.out.println("=".repeat(95));

        System.out.println("Simulated tasks: " + taskCount);

        BenchmarkResult arrayListResult = testArrayList(taskCount);
        BenchmarkResult priorityQueueResult = testPriorityQueue(taskCount);
        BenchmarkResult hashMapResult = testHashMap(taskCount);

        System.out.println("\n" + "-".repeat(95));
        System.out.printf("%-25s | %-25s | %-15s | %-15s | %-15s%n",
                "Data Structure", "Operation", "Time (ms)", "Memory (KB)", "Big-O");
        System.out.println("-".repeat(95));

        System.out.printf("%-25s | %-25s | %-15.4f | %-15.2f | %-15s%n",
                "ArrayList", "Add Tasks", arrayListResult.timeMs, arrayListResult.memoryKb, "O(1) avg add");

        System.out.printf("%-25s | %-25s | %-15.4f | %-15.2f | %-15s%n",
                "PriorityQueue", "Add + Poll", priorityQueueResult.timeMs, priorityQueueResult.memoryKb, "O(log n)");

        System.out.printf("%-25s | %-25s | %-15.4f | %-15.2f | %-15s%n",
                "HashMap", "Put + Update", hashMapResult.timeMs, hashMapResult.memoryKb, "O(1) avg");

        System.out.println("-".repeat(95));

        System.out.println("\nConclusion:");
        System.out.println("- ArrayList is suitable for storing and displaying all tasks.");
        System.out.println("- PriorityQueue is suitable for ranking tasks by priority score.");
        System.out.println("- HashMap is suitable for tracking remaining study hours using task names.");
    }

    private static BenchmarkResult testArrayList(int taskCount) {
        runGarbageCollector();

        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        ArrayList<Task> taskList = new ArrayList<>();

        for (Task task : generateDummyTasks(taskCount)) {
            taskList.add(task);
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        return new BenchmarkResult(
                (endTime - startTime) / 1_000_000.0,
                (memoryAfter - memoryBefore) / 1024.0
        );
    }

    private static BenchmarkResult testPriorityQueue(int taskCount) {
        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);

        runGarbageCollector();

        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        PriorityQueue<Task> queue = new PriorityQueue<>();

        for (Task task : dummyTasks) {
            task.recalculatePriority();
            queue.add(task);
        }

        while (!queue.isEmpty()) {
            queue.poll();
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        return new BenchmarkResult(
                (endTime - startTime) / 1_000_000.0,
                (memoryAfter - memoryBefore) / 1024.0
        );
    }

    private static BenchmarkResult testHashMap(int taskCount) {
        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);

        runGarbageCollector();

        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();

        HashMap<String, Double> remainingHoursMap = new HashMap<>();

        for (Task task : dummyTasks) {
            remainingHoursMap.put(task.getName(), task.getEstimatedHours());
        }

        for (Task task : dummyTasks) {
            double currentHours = remainingHoursMap.get(task.getName());
            remainingHoursMap.put(task.getName(), Math.max(0, currentHours - 1));
        }

        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();

        return new BenchmarkResult(
                (endTime - startTime) / 1_000_000.0,
                (memoryAfter - memoryBefore) / 1024.0
        );
    }

    private static class BenchmarkResult {
        double timeMs;
        double memoryKb;

        BenchmarkResult(double timeMs, double memoryKb) {
            this.timeMs = timeMs;
            this.memoryKb = memoryKb;
        }
    }
}