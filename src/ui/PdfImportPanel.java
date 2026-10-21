package ui;

import db.TaskDAO;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

public class PdfImportPanel extends JPanel {
    private JTextField filePathField;
    private JTextArea previewTextArea;
    private TaskDAO taskDAO;

    public PdfImportPanel() {
        taskDAO = new TaskDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Title
        JLabel titleLabel = new JLabel("Syllabus PDF Import & Auto-Planner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 2. Center Panel (File Selector & Preview Area)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton browseBtn = new JButton("Browse PDF");
        browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        fileRow.add(filePathField, BorderLayout.CENTER);
        fileRow.add(browseBtn, BorderLayout.EAST);

        previewTextArea = new JTextArea("Select a syllabus PDF to extract milestones and generate tasks automatically...");
        previewTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        previewTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(previewTextArea);

        centerPanel.add(fileRow, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generateBtn = new JButton("Generate Study Tasks");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(generateBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners
        browseBtn.addActionListener(e -> selectFile());
        generateBtn.addActionListener(e -> generateTasksFromSyllabus());
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            previewTextArea.setText("Loaded syllabus: " + selectedFile.getName() + "\n\nReady to parse modules and schedule target dates!");
        }
    }

    private void generateTasksFromSyllabus() {
        String path = filePathField.getText();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a PDF file first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = logic.UserSession.getInstance().getUserId();
        if (userId != -1) {
            File pdfFile = new File(path);
            logic.SyllabusParser parser = new logic.SyllabusParser();
            java.util.List<String> extractedTasks = parser.extractMilestones(pdfFile);

            if (extractedTasks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No modules or chapters found in this PDF.", "Parse Result", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Stagger the target dates for the generated tasks
            int dayOffset = 1;
            for (String taskTitle : extractedTasks) {
                models.Task newTask = new models.Task(
                    0, 
                    userId, 
                    taskTitle, 
                    "Auto-extracted from " + pdfFile.getName(), 
                    java.time.LocalDate.now().plusDays(dayOffset), 
                    false, 
                    true
                );
                taskDAO.createTask(newTask);
                dayOffset += 2; // Schedule one module every 2 days
            }

            JOptionPane.showMessageDialog(this, "Success! Extracted " + extractedTasks.size() + " tasks into your Daily Path.", "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            previewTextArea.setText(previewTextArea.getText() + "\n\n[SUCCESS] Added " + extractedTasks.size() + " actual tasks to your database!");
        }
    }
}