package ui;

import db.TaskDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.Task;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DailyDashboardPanel extends JPanel {
    private TaskDAO taskDAO;
    private JPanel listContainer;

    public DailyDashboardPanel() {
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

        JLabel titleLabel = new JLabel("TASK MANAGER // TODAY'S WORK");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Manage manual objectives or import syllabus PDFs for automated portion division.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(140, 160, 175));

        textGroup.add(titleLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subLabel);

        // Action Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton addTaskBtn = new JButton("+ ADD TASK");
        addTaskBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addTaskBtn.setBackground(new Color(28, 176, 246)); // Sapphire Blue
        addTaskBtn.setForeground(Color.WHITE);
        addTaskBtn.setFocusPainted(false);
        addTaskBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addTaskBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTaskBtn.addActionListener(e -> openAddTaskDialog());

        JButton uploadSyllabusBtn = new JButton("UPLOAD SYLLABUS PDF");
        uploadSyllabusBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        uploadSyllabusBtn.setBackground(new Color(34, 197, 94)); // Emerald Green
        uploadSyllabusBtn.setForeground(Color.WHITE);
        uploadSyllabusBtn.setFocusPainted(false);
        uploadSyllabusBtn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        uploadSyllabusBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadSyllabusBtn.addActionListener(e -> uploadAndParseSyllabus());

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

    private void refreshTaskList() {
        listContainer.removeAll();
        int userId = UserSession.getInstance().getUserId();
        List<Task> tasks = taskDAO.getTasksForUser(userId);

        if (tasks.isEmpty()) {
            JLabel emptyLbl = new JLabel("No tasks found. Click '+ ADD TASK' or upload a syllabus PDF to generate portions.");
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

        JLabel statusBadge = new JLabel(task.isCompleted() ? "✓ COMPLETED" : "DUE: TODAY");
        statusBadge.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        statusBadge.setForeground(task.isCompleted() ? new Color(34, 197, 94) : new Color(56, 189, 248));

        row.add(check, BorderLayout.CENTER);
        row.add(statusBadge, BorderLayout.EAST);
        return row;
    }

    private void openAddTaskDialog() {
        JTextField titleField = new JTextField(20);
        JTextField dueDateField = new JTextField("Today", 10);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Task Title / Objective:"));
        panel.add(titleField);
        panel.add(new JLabel("Due Date (e.g., Today, 2026-10-25):"));
        panel.add(dueDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Manual Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            if (!title.isEmpty()) {
                int userId = UserSession.getInstance().getUserId();
                taskDAO.addTask(userId, title, dueDate);
                SystemLogger.log("Manual task created: " + title);
                refreshTaskList();
            }
        }
    }

    private void uploadAndParseSyllabus() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Syllabus Files", "pdf"));
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PDDocument document = Loader.loadPDF(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);

                // Automatically divide text into portions/chapters (splitting by newlines or keywords)
                String[] lines = text.split("\\r?\\n");
                int userId = UserSession.getInstance().getUserId();
                int count = 0;

                for (String line : lines) {
                    String trimmed = line.trim();
                    // Detect potential modules, chapters, or syllabus headings
                    if (trimmed.toLowerCase().contains("module") || trimmed.toLowerCase().contains("unit") || trimmed.toLowerCase().contains("chapter") || (trimmed.length() > 5 && trimmed.length() < 60 && Character.isUpperCase(trimmed.charAt(0)))) {
                        taskDAO.addTask(userId, "Syllabus Part: " + trimmed, "Syllabus Target");
                        count++;
                        if (count >= 10) break; // Limit auto-generation per upload batch
                    }
                }

                if (count == 0 && lines.length > 0) {
                    // Fallback: create generic tasks if specific headings aren't matched
                    taskDAO.addTask(userId, "Review Syllabus: " + file.getName(), "Syllabus Target");
                }

                SystemLogger.log("Syllabus parsed successfully: " + file.getName() + " (" + Math.max(count, 1) + " portions created)");
                JOptionPane.showMessageDialog(this, "Syllabus parsed and divided into study tasks successfully!", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshTaskList();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to parse PDF file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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