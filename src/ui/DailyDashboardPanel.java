package ui;

import db.TaskDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DailyDashboardPanel extends JPanel {
    private TaskDAO taskDAO;
    private JPanel listContainer;
    private MainFrame parentFrame; // Added parent reference for routing

    public DailyDashboardPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.taskDAO = new TaskDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. TOP HEADER & ACTION BUTTONS
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER TASK LIST CONTAINER
        add(createTaskListPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("DAILY TASKS // OBJECTIVE PATH");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Review daily milestones. Add manual tasks or upload syllabus PDFs via Task Manager.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(140, 160, 175));

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subLabel);

        // Action Buttons Bar -> Redirects to Task Manager ("PDF_IMPORT")
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton addTaskBtn = new JButton("+ ADD TASK");
        addTaskBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addTaskBtn.setBackground(new Color(28, 176, 246)); // Sapphire Blue
        addTaskBtn.setForeground(Color.WHITE);
        addTaskBtn.setFocusPainted(false);
        addTaskBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addTaskBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTaskBtn.addActionListener(e -> parentFrame.switchView("PDF_IMPORT"));

        JButton uploadSyllabusBtn = new JButton("UPLOAD SYLLABUS PDF");
        uploadSyllabusBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        uploadSyllabusBtn.setBackground(new Color(34, 197, 94)); // Emerald Green
        uploadSyllabusBtn.setForeground(Color.WHITE);
        uploadSyllabusBtn.setFocusPainted(false);
        uploadSyllabusBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        uploadSyllabusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadSyllabusBtn.addActionListener(e -> parentFrame.switchView("PDF_IMPORT"));

        btnBar.add(addTaskBtn);
        btnBar.add(uploadSyllabusBtn);

        header.add(textGroup, BorderLayout.WEST);
        header.add(btnBar, BorderLayout.EAST);
        return header;
    }

    private JPanel createTaskListPanel() {
        JPanel card = createHudCard();
        card.setLayout(new BorderLayout(10, 10));

        JLabel cardTitle = new JLabel("ACTIVE STUDY NODES & PORTIONS");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cardTitle.setForeground(Color.WHITE);
        card.add(cardTitle, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        refreshTaskList();

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    public void refreshTaskList() {
        listContainer.removeAll();
        int userId = UserSession.getInstance().getUserId();
        List<Task> tasks = taskDAO.getTasksForUser(userId);

        if (tasks.isEmpty()) {
            JLabel emptyLbl = new JLabel("No tasks found. Click '+ ADD TASK' to manage targets in Task Manager.");
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLbl.setForeground(Color.GRAY);
            listContainer.add(emptyLbl);
        } else {
            int nodeNum = 1;
            for (Task task : tasks) {
                listContainer.add(createTaskRow(task, nodeNum++));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createTaskRow(Task task, int index) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(task.isCompleted() ? new Color(34, 197, 94, 100) : new Color(40, 55, 70), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JCheckBox check = new JCheckBox(String.format("0%d // %s", index, task.getTitle()), task.isCompleted());
        check.setFont(new Font("Segoe UI", Font.BOLD, 12));
        check.setForeground(task.isCompleted() ? new Color(100, 120, 135) : Color.WHITE);
        check.setOpaque(false);
        check.addActionListener(e -> {
            taskDAO.updateTaskCompletion(task.getId(), check.isSelected());
            check.setForeground(check.isSelected() ? new Color(100, 120, 135) : Color.WHITE);
            if (check.isSelected()) {
                SystemLogger.log("Task verified: " + task.getTitle() + " (+50 XP)");
            } else {
                SystemLogger.log("Task unverified: " + task.getTitle());
            }
            refreshTaskList();
        });

        JLabel statusBadge = new JLabel(task.isCompleted() ? "✓ COMPLETED" : "DUE: " + (task.getDueDate() != null ? task.getDueDate().toString() : "Today"));
        statusBadge.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        statusBadge.setForeground(task.isCompleted() ? new Color(34, 197, 94) : new Color(56, 189, 248));

        row.add(check, BorderLayout.CENTER);
        row.add(statusBadge, BorderLayout.EAST);
        return row;
    }

    private JPanel createHudCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(24, 32, 38)); // Slate Card
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }
}