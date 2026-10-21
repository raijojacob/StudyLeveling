package ui;

import db.TaskDAO;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DailyDashboardPanel extends JPanel {
    private JPanel taskListPanel;
    private JTextField taskInputField;
    private TaskDAO taskDAO;

    public DailyDashboardPanel() {
        taskDAO = new TaskDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Top Header Area
        JLabel titleLabel = new JLabel("Daily Study Path");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 2. Center Task List (Scrollable)
        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Quick-Add Inbox Area
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        taskInputField = new JTextField();
        taskInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskInputField.setToolTipText("Enter a new task for today...");

        JButton addButton = new JButton("Add Task");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        inputPanel.add(taskInputField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        
        // Add padding to bottom input panel
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(inputPanel, BorderLayout.SOUTH);

        // Event Listeners
        addButton.addActionListener(e -> addNewTask());
        taskInputField.addActionListener(e -> addNewTask()); // Pressing Enter also adds task

        // Load tasks on startup
        refreshTasks();
    }

    private void addNewTask() {
        String title = taskInputField.getText().trim();
        if (title.isEmpty()) return;

        int userId = UserSession.getInstance().getUserId();
        if (userId != -1) {
            Task newTask = new Task(0, userId, title, "", LocalDate.now(), false, false);
            taskDAO.createTask(newTask);
            taskInputField.setText("");
            refreshTasks();
        }
    }

    public void refreshTasks() {
        taskListPanel.removeAll();
        int userId = UserSession.getInstance().getUserId();
        
        if (userId != -1) {
            List<Task> tasks = taskDAO.getTasksForUser(userId);
            
            if (tasks.isEmpty()) {
                JLabel emptyLabel = new JLabel("No tasks for today. Add one below!");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                taskListPanel.add(emptyLabel);
            } else {
                for (Task task : tasks) {
                    JCheckBox checkBox = new JCheckBox(task.getTitle(), task.isCompleted());
                    checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    checkBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                    
                    // Toggle completion status in DB when clicked
                    checkBox.addActionListener(e -> {
                        task.setCompleted(checkBox.isSelected());
                        taskDAO.updateTaskCompletion(task.getId(), checkBox.isSelected());
                    });
                    
                    taskListPanel.add(checkBox);
                    taskListPanel.add(Box.createVerticalStrut(5));
                }
            }
        }
        
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }
}