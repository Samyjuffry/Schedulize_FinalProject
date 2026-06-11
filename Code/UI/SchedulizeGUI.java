import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.PriorityQueue;

public class SchedulizeGUI extends JFrame {

    private SchedulizeSystem systemEngine = new SchedulizeSystem();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField taskNameField;
    private JTextField deadlineField;
    private JTextField hoursField;
    private JTextField gradeField;
    private JTextField dailyStudyHoursField;

    private JComboBox<Task.Difficulty> difficultyBox;
    private JComboBox<Task.Type> typeBox;

    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextArea outputArea;

    public SchedulizeGUI() {
        setTitle("SCHEDULIZE");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createInputPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();

        JLabel title = new JLabel("SCHEDULIZE - Task Prioritization and Study Planner");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        panel.add(title);
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(16, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Task Input"));

        taskNameField = new JTextField();
        deadlineField = new JTextField();
        hoursField = new JTextField();
        gradeField = new JTextField();
        dailyStudyHoursField = new JTextField("3");

        difficultyBox = new JComboBox<>(Task.Difficulty.values());
        typeBox = new JComboBox<>(Task.Type.values());

        panel.add(new JLabel("Task Name:"));
        panel.add(taskNameField);

        panel.add(new JLabel("Deadline (DD/MM/YYYY):"));
        panel.add(deadlineField);

        panel.add(new JLabel("Estimated Hours:"));
        panel.add(hoursField);

        panel.add(new JLabel("Difficulty:"));
        panel.add(difficultyBox);

        panel.add(new JLabel("Grade Percentage:"));
        panel.add(gradeField);

        panel.add(new JLabel("Task Type:"));
        panel.add(typeBox);

        panel.add(new JLabel("Daily Study Hours:"));
        panel.add(dailyStudyHoursField);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(
                new String[]{"Task Name", "Type", "Deadline", "Hours", "Difficulty", "Grade", "Priority"},
                0
        );

        taskTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(taskTable);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane outputScroll = new JScrollPane(outputArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, outputScroll);
        splitPane.setDividerLocation(300);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Selected Task");
        JButton priorityButton = new JButton("View Priority Ranking");
        JButton studyPlanButton = new JButton("Generate Study Plan");
        JButton clearButton = new JButton("Clear Output");

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeSelectedTask());
        priorityButton.addActionListener(e -> displayPriorityRanking());
        studyPlanButton.addActionListener(e -> generateStudyPlan());
        clearButton.addActionListener(e -> outputArea.setText(""));

        panel.add(addButton);
        panel.add(removeButton);
        panel.add(priorityButton);
        panel.add(studyPlanButton);
        panel.add(clearButton);

        return panel;
    }

    private void addTask() {
        try {
            String name = taskNameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Task name cannot be empty.");
                return;
            }

            LocalDate deadline = LocalDate.parse(deadlineField.getText().trim(), dateFormatter);

            if (deadline.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Deadline cannot be in the past.");
                return;
            }

            double estimatedHours = Double.parseDouble(hoursField.getText().trim());
            double gradePercent = Double.parseDouble(gradeField.getText().trim());

            if (estimatedHours <= 0) {
                JOptionPane.showMessageDialog(this, "Estimated hours must be greater than 0.");
                return;
            }

            if (gradePercent < 0 || gradePercent > 100) {
                JOptionPane.showMessageDialog(this, "Grade percentage must be between 0 and 100.");
                return;
            }

            Task.Difficulty difficulty = (Task.Difficulty) difficultyBox.getSelectedItem();
            Task.Type type = (Task.Type) typeBox.getSelectedItem();

            Task task = new Task(name, deadline, estimatedHours, difficulty, gradePercent, type);
            systemEngine.addTask(task);

            refreshTable();
            clearInputFields();

            outputArea.append("Task added successfully: " + name + "\n");

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use DD/MM/YYYY.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Estimated hours and grade percentage must be numbers.");
        }
    }

    private void removeSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to remove.");
            return;
        }

        String taskName = tableModel.getValueAt(selectedRow, 0).toString();
        boolean removed = systemEngine.removeTask(taskName);

        if (removed) {
            refreshTable();
            outputArea.append("Task removed: " + taskName + "\n");
        } else {
            outputArea.append("Task could not be removed.\n");
        }
    }

    private void displayPriorityRanking() {
        if (systemEngine.isEmpty()) {
            outputArea.append("No tasks available.\n");
            return;
        }

        PriorityQueue<Task> queue = systemEngine.createPriorityQueue();

        outputArea.append("\n===== PRIORITY RANKING =====\n");

        int count = 1;

        while (!queue.isEmpty()) {
            Task task = queue.poll();

            outputArea.append(
                    count + ". " +
                            task.getName() +
                            " | " + task.getType() +
                            " | " + task.getDifficulty() +
                            " | " + task.getEstimatedHours() + "h" +
                            " | " + task.getGradePercent() + "%" +
                            " | " + task.getPriorityLabel() +
                            " | Score: " + String.format("%.2f", task.getPriorityScore()) +
                            "\n"
            );

            count++;
        }

        outputArea.append("============================\n");
    }

    private void generateStudyPlan() {
        try {
            double dailyHours = Double.parseDouble(dailyStudyHoursField.getText().trim());

            if (dailyHours <= 0 || dailyHours > 24) {
                JOptionPane.showMessageDialog(this, "Daily study hours must be between 0 and 24.");
                return;
            }

            if (systemEngine.isEmpty()) {
                outputArea.append("No tasks available to generate study plan.\n");
                return;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream captureOut = new PrintStream(outputStream);

            System.setOut(captureOut);

            systemEngine.generateStudyPlan(dailyHours);

            System.out.flush();
            System.setOut(originalOut);

            outputArea.append("\n" + outputStream.toString() + "\n");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Daily study hours must be a number.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Task task : systemEngine.getAllTasks()) {
            task.recalculatePriority();

            tableModel.addRow(new Object[]{
                    task.getName(),
                    task.getType(),
                    task.getDeadline().format(dateFormatter),
                    task.getEstimatedHours(),
                    task.getDifficulty(),
                    task.getGradePercent(),
                    task.getPriorityLabel() + " (" + String.format("%.2f", task.getPriorityScore()) + ")"
            });
        }
    }

    private void clearInputFields() {
        taskNameField.setText("");
        deadlineField.setText("");
        hoursField.setText("");
        gradeField.setText("");
        difficultyBox.setSelectedIndex(0);
        typeBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SchedulizeGUI().setVisible(true);
        });
    }
}