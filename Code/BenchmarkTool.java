import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

public class BenchmarkTool {

    private static Scanner benchmarkScanner = new Scanner(System.in);

    // Average several runs so the result is more stable than one single run.
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;

    // Prevents the JVM from completely ignoring loops whose results are not printed.
    private static volatile double benchmarkSink = 0;

    public static void runBenchmarkMenu() {
        runBenchmarkMenu(benchmarkScanner);
    }

    public static void runBenchmarkMenu(Scanner sharedScanner) {
        benchmarkScanner = sharedScanner;

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
        ArrayList<Task> tasks = new ArrayList<>(size);

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
            Thread.sleep(80);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static BenchmarkResult measure(Runnable operation) {
        // Warm-up runs reduce first-run JVM/JIT noise.
        for (int i = 0; i < WARMUP_RUNS; i++) {
            operation.run();
        }

        double totalTimeMs = 0;
        double totalMemoryKb = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {
            runGarbageCollector();
            long memoryBefore = getUsedMemory();
            long startTime = System.nanoTime();

            operation.run();

            long endTime = System.nanoTime();
            long memoryAfter = getUsedMemory();

            totalTimeMs += (endTime - startTime) / 1_000_000.0;
            totalMemoryKb += Math.max(0, memoryAfter - memoryBefore) / 1024.0;
        }

        return new BenchmarkResult(totalTimeMs / MEASURED_RUNS, totalMemoryKb / MEASURED_RUNS);
    }

    private static void printBenchmarkHeader(String title, int taskCount) {
        System.out.println("\n" + "-".repeat(100));
        System.out.println(title);
        System.out.println("-".repeat(100));
        System.out.println("Simulated tasks: " + taskCount);
        System.out.println("Measured runs averaged: " + MEASURED_RUNS + " (plus " + WARMUP_RUNS + " warm-up runs)");
        System.out.printf("%-30s | %-15s | %-15s | %-20s%n", "Operation", "Time (ms)", "Memory (KB)", "Big-O");
        System.out.println("-".repeat(100));
    }

    private static void printOperation(String operation, BenchmarkResult result, String bigO) {
        System.out.printf("%-30s | %-15.4f | %-15.2f | %-20s%n",
                operation, result.timeMs, result.memoryKb, bigO);
    }

    private static void benchmarkArrayList(int taskCount) {
        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);
        ArrayListResult result = testArrayListDetailed(dummyTasks);

        printBenchmarkHeader("ARRAYLIST TASK STORAGE BENCHMARK", taskCount);
        printOperation("Add Tasks", result.addResult, "O(1) average");
        printOperation("Access / Traverse Tasks", result.traverseResult, "O(n)");
        printOperation("Remove Middle Tasks", result.removeResult, "O(n)");
        System.out.println("-".repeat(100));
        printOperation("TOTAL", result.getTotalResult(), "Mixed");

        System.out.println("\nComplexity Analysis:");
        System.out.println("Add task              : O(1) average");
        System.out.println("Access by index       : O(1)");
        System.out.println("Traverse all tasks    : O(n)");
        System.out.println("Remove from middle    : O(n)");
    }

    private static void benchmarkPriorityQueue(int taskCount) {
        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);
        PriorityQueueResult result = testPriorityQueueDetailed(dummyTasks);

        printBenchmarkHeader("PRIORITYQUEUE TASK RANKING BENCHMARK", taskCount);
        printOperation("Insert Ranked Tasks", result.insertResult, "O(log n) each");
        printOperation("Peek Highest Priority", result.peekResult, "O(1)");
        printOperation("Poll All Ranked Tasks", result.pollResult, "O(log n) each");
        System.out.println("-".repeat(100));
        printOperation("TOTAL", result.getTotalResult(), "O(n log n)");

        System.out.println("\nComplexity Analysis:");
        System.out.println("Insert task           : O(log n)");
        System.out.println("Peek highest priority : O(1)");
        System.out.println("Poll highest priority : O(log n)");
        System.out.println("Full ranking output   : O(n log n)");
    }

    private static void benchmarkHashMap(int taskCount) {
        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);
        HashMapResult result = testHashMapDetailed(dummyTasks);

        printBenchmarkHeader("HASHMAP REMAINING STUDY TIME BENCHMARK", taskCount);
        printOperation("Put Remaining Hours", result.putResult, "O(1) average");
        printOperation("Get Remaining Hours", result.getResult, "O(1) average");
        printOperation("Update Remaining Hours", result.updateResult, "O(1) average");
        printOperation("Remove Entries", result.removeResult, "O(1) average");
        System.out.println("-".repeat(100));
        printOperation("TOTAL", result.getTotalResult(), "O(n) total");

        System.out.println("\nComplexity Analysis:");
        System.out.println("Put remaining hours   : O(1) average");
        System.out.println("Get remaining hours   : O(1) average");
        System.out.println("Update remaining time : O(1) average");
        System.out.println("Remove entry          : O(1) average");
        System.out.println("Ordering              : Not maintained");
    }

    private static void runAllBenchmarks(int taskCount) {
        System.out.println("\n" + "=".repeat(105));
        System.out.println("SCHEDULIZE SIDE-BY-SIDE BENCHMARK COMPARISON");
        System.out.println("=".repeat(105));
        System.out.println("Simulated tasks: " + taskCount);
        System.out.println("Measured runs averaged: " + MEASURED_RUNS + " (plus " + WARMUP_RUNS + " warm-up runs)");

        ArrayList<Task> dummyTasks = generateDummyTasks(taskCount);

        ArrayListResult arrayListResult = testArrayListDetailed(dummyTasks);
        PriorityQueueResult priorityQueueResult = testPriorityQueueDetailed(dummyTasks);
        HashMapResult hashMapResult = testHashMapDetailed(dummyTasks);

        BenchmarkResult arrayTotal = arrayListResult.getTotalResult();
        BenchmarkResult priorityTotal = priorityQueueResult.getTotalResult();
        BenchmarkResult hashTotal = hashMapResult.getTotalResult();

        System.out.println("\n" + "-".repeat(105));
        System.out.printf("%-20s | %-35s | %-15s | %-15s | %-15s%n",
                "Data Structure", "Operations Included", "Time (ms)", "Memory (KB)", "Big-O");
        System.out.println("-".repeat(105));

        System.out.printf("%-20s | %-35s | %-15.4f | %-15.2f | %-15s%n",
                "ArrayList", "Add + Traverse + Remove", arrayTotal.timeMs, arrayTotal.memoryKb, "Mixed");

        System.out.printf("%-20s | %-35s | %-15.4f | %-15.2f | %-15s%n",
                "PriorityQueue", "Insert + Peek + Poll", priorityTotal.timeMs, priorityTotal.memoryKb, "O(n log n)");

        System.out.printf("%-20s | %-35s | %-15.4f | %-15.2f | %-15s%n",
                "HashMap", "Put + Get + Update + Remove", hashTotal.timeMs, hashTotal.memoryKb, "O(n) total");

        System.out.println("-".repeat(105));

        String fastest = findFastest(arrayTotal.timeMs, priorityTotal.timeMs, hashTotal.timeMs);
        String lowestMemory = findLowestMemory(arrayTotal.memoryKb, priorityTotal.memoryKb, hashTotal.memoryKb);

        System.out.println("\nSummary:");
        System.out.println("Fastest total time       : " + fastest);
        System.out.println("Lowest measured memory   : " + lowestMemory);

        System.out.println("\nConclusion:");
        System.out.println("- ArrayList is suitable for storing, traversing, and removing task records.");
        System.out.println("- PriorityQueue is suitable for ranking tasks by priority score.");
        System.out.println("- HashMap is suitable for tracking remaining study hours using task names.");
        System.out.println("- Memory results may show 0.00 KB for small task counts because JVM memory allocation is not perfectly precise.");
    }

    private static String findFastest(double arrayListTime, double priorityQueueTime, double hashMapTime) {
        if (arrayListTime <= priorityQueueTime && arrayListTime <= hashMapTime) {
            return "ArrayList";
        } else if (priorityQueueTime <= arrayListTime && priorityQueueTime <= hashMapTime) {
            return "PriorityQueue";
        } else {
            return "HashMap";
        }
    }

    private static String findLowestMemory(double arrayListMemory, double priorityQueueMemory, double hashMapMemory) {
        if (arrayListMemory <= priorityQueueMemory && arrayListMemory <= hashMapMemory) {
            return "ArrayList";
        } else if (priorityQueueMemory <= arrayListMemory && priorityQueueMemory <= hashMapMemory) {
            return "PriorityQueue";
        } else {
            return "HashMap";
        }
    }

    private static ArrayListResult testArrayListDetailed(ArrayList<Task> dummyTasks) {
        BenchmarkResult addResult = measure(() -> {
            ArrayList<Task> taskList = new ArrayList<>();
            for (Task task : dummyTasks) {
                taskList.add(task);
            }
            benchmarkSink += taskList.size();
        });

        BenchmarkResult traverseResult = measure(() -> {
            ArrayList<Task> taskList = new ArrayList<>(dummyTasks);
            double totalPriority = 0;
            for (Task task : taskList) {
                totalPriority += task.getPriorityScore();
            }
            benchmarkSink += totalPriority;
        });

        BenchmarkResult removeResult = measure(() -> {
            ArrayList<Task> taskList = new ArrayList<>(dummyTasks);
            while (!taskList.isEmpty()) {
                taskList.remove(taskList.size() / 2);
            }
            benchmarkSink += taskList.size();
        });

        return new ArrayListResult(addResult, traverseResult, removeResult);
    }

    private static PriorityQueueResult testPriorityQueueDetailed(ArrayList<Task> dummyTasks) {
        BenchmarkResult insertResult = measure(() -> {
            PriorityQueue<Task> queue = new PriorityQueue<>();
            for (Task task : dummyTasks) {
                task.recalculatePriority();
                queue.add(task);
            }
            benchmarkSink += queue.size();
        });

        BenchmarkResult peekResult = measure(() -> {
            PriorityQueue<Task> queue = new PriorityQueue<>();
            for (Task task : dummyTasks) {
                task.recalculatePriority();
                queue.add(task);
            }
            Task highestPriorityTask = queue.peek();
            if (highestPriorityTask != null) {
                benchmarkSink += highestPriorityTask.getPriorityScore();
            }
        });

        BenchmarkResult pollResult = measure(() -> {
            PriorityQueue<Task> queue = new PriorityQueue<>();
            for (Task task : dummyTasks) {
                task.recalculatePriority();
                queue.add(task);
            }
            double totalPriority = 0;
            while (!queue.isEmpty()) {
                totalPriority += queue.poll().getPriorityScore();
            }
            benchmarkSink += totalPriority;
        });

        return new PriorityQueueResult(insertResult, peekResult, pollResult);
    }

    private static HashMapResult testHashMapDetailed(ArrayList<Task> dummyTasks) {
        BenchmarkResult putResult = measure(() -> {
            HashMap<String, Double> remainingHoursMap = new HashMap<>();
            for (Task task : dummyTasks) {
                remainingHoursMap.put(task.getName(), task.getEstimatedHours());
            }
            benchmarkSink += remainingHoursMap.size();
        });

        BenchmarkResult getResult = measure(() -> {
            HashMap<String, Double> remainingHoursMap = createRemainingHoursMap(dummyTasks);
            double totalHours = 0;
            for (Task task : dummyTasks) {
                totalHours += remainingHoursMap.get(task.getName());
            }
            benchmarkSink += totalHours;
        });

        BenchmarkResult updateResult = measure(() -> {
            HashMap<String, Double> remainingHoursMap = createRemainingHoursMap(dummyTasks);
            for (Task task : dummyTasks) {
                double currentHours = remainingHoursMap.get(task.getName());
                remainingHoursMap.put(task.getName(), Math.max(0, currentHours - 1));
            }
            benchmarkSink += remainingHoursMap.size();
        });

        BenchmarkResult removeResult = measure(() -> {
            HashMap<String, Double> remainingHoursMap = createRemainingHoursMap(dummyTasks);
            for (Task task : dummyTasks) {
                remainingHoursMap.remove(task.getName());
            }
            benchmarkSink += remainingHoursMap.size();
        });

        return new HashMapResult(putResult, getResult, updateResult, removeResult);
    }

    private static HashMap<String, Double> createRemainingHoursMap(ArrayList<Task> dummyTasks) {
        HashMap<String, Double> remainingHoursMap = new HashMap<>();
        for (Task task : dummyTasks) {
            remainingHoursMap.put(task.getName(), task.getEstimatedHours());
        }
        return remainingHoursMap;
    }

    private static class BenchmarkResult {
        double timeMs;
        double memoryKb;

        BenchmarkResult(double timeMs, double memoryKb) {
            this.timeMs = timeMs;
            this.memoryKb = memoryKb;
        }

        static BenchmarkResult total(BenchmarkResult... results) {
            double totalTimeMs = 0;
            double totalMemoryKb = 0;

            for (BenchmarkResult result : results) {
                totalTimeMs += result.timeMs;
                totalMemoryKb += result.memoryKb;
            }

            return new BenchmarkResult(totalTimeMs, totalMemoryKb);
        }
    }

    private static class ArrayListResult {
        BenchmarkResult addResult;
        BenchmarkResult traverseResult;
        BenchmarkResult removeResult;

        ArrayListResult(BenchmarkResult addResult, BenchmarkResult traverseResult, BenchmarkResult removeResult) {
            this.addResult = addResult;
            this.traverseResult = traverseResult;
            this.removeResult = removeResult;
        }

        BenchmarkResult getTotalResult() {
            return BenchmarkResult.total(addResult, traverseResult, removeResult);
        }
    }

    private static class PriorityQueueResult {
        BenchmarkResult insertResult;
        BenchmarkResult peekResult;
        BenchmarkResult pollResult;

        PriorityQueueResult(BenchmarkResult insertResult, BenchmarkResult peekResult, BenchmarkResult pollResult) {
            this.insertResult = insertResult;
            this.peekResult = peekResult;
            this.pollResult = pollResult;
        }

        BenchmarkResult getTotalResult() {
            return BenchmarkResult.total(insertResult, peekResult, pollResult);
        }
    }

    private static class HashMapResult {
        BenchmarkResult putResult;
        BenchmarkResult getResult;
        BenchmarkResult updateResult;
        BenchmarkResult removeResult;

        HashMapResult(BenchmarkResult putResult, BenchmarkResult getResult, BenchmarkResult updateResult, BenchmarkResult removeResult) {
            this.putResult = putResult;
            this.getResult = getResult;
            this.updateResult = updateResult;
            this.removeResult = removeResult;
        }

        BenchmarkResult getTotalResult() {
            return BenchmarkResult.total(putResult, getResult, updateResult, removeResult);
        }
    }
}
