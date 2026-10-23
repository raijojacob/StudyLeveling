package ui;

import db.DocumentDAO;
import db.TaskDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.Task;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManagerPanel extends JPanel {
    private TaskDAO taskDAO;
    private JPanel taskListContainer;
    
    // Syllabus Preview Components
    private JTable previewTable;
    private DefaultTableModel previewTableModel;
    private File selectedPdfFile;

    public TaskManagerPanel() {
        this.taskDAO = new TaskDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. TOP HEADER
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER SPLIT (Left: Current Tasks | Right: Manual Add & Syllabus Preview)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCurrentTasksPanel(), createRightActionPanel());
        splitPane.setDividerLocation(520);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // Auto-refresh tasks from MySQL whenever this view tab is displayed
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshTaskList();
            }
        });

        refreshTaskList();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel("TASK MANAGER // OBJECTIVE CONTROL");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Manage manual study milestones or upload syllabus PDFs to preview and approve automated portions.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(140, 160, 175));

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);
        textGroup.add(titleLbl);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subLbl);

        header.add(textGroup, BorderLayout.WEST);
        return header;
    }

    // --- LEFT PANEL: CURRENT TASKS ---
    private JPanel createCurrentTasksPanel() {
        JPanel card = createHudCard();
        card.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("ACTIVE STUDY NODES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Color.WHITE);
        card.add(title, BorderLayout.NORTH);

        taskListContainer = new JPanel();
        taskListContainer.setLayout(new BoxLayout(taskListContainer, BoxLayout.Y_AXIS));
        taskListContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(taskListContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    public void refreshTaskList() {
        taskListContainer.removeAll();
        int userId = UserSession.getInstance().getUserId();
        
        List<Task> tasks = taskDAO.getTasksForUser(userId);

        if (tasks.isEmpty()) {
            JLabel empty = new JLabel("No active tasks found. Add manually or upload a syllabus.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(Color.GRAY);
            taskListContainer.add(empty);
        } else {
            int num = 1;
            for (Task task : tasks) {
                taskListContainer.add(createTaskRow(task, num++));
                taskListContainer.add(Box.createVerticalStrut(8));
            }
        }
        taskListContainer.revalidate();
        taskListContainer.repaint();
    }

    private JPanel createTaskRow(Task task, int index) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(task.isCompleted() ? new Color(34, 197, 94, 100) : new Color(40, 55, 70), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        // Prevent BoxLayout from stretching the row vertically
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JCheckBox check = new JCheckBox(String.format("0%d // %s", index, task.getTitle()), task.isCompleted());
        check.setFont(new Font("Segoe UI", Font.BOLD, 11));
        check.setForeground(task.isCompleted() ? new Color(100, 120, 135) : Color.WHITE);
        check.setOpaque(false);
        check.addActionListener(e -> {
            taskDAO.updateTaskCompletion(task.getId(), check.isSelected());
            if (check.isSelected()) {
                SystemLogger.log("Task verified: " + task.getTitle());
            }
            refreshTaskList();
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightActions.setOpaque(false);

        JLabel dueLbl = new JLabel("Due: " + (task.getDueDate() != null ? task.getDueDate().toString() : "Today"));
        dueLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dueLbl.setForeground(new Color(140, 160, 175));

        JButton deleteBtn = new JButton("✕");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        deleteBtn.setForeground(new Color(248, 113, 113));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorder(null);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            taskDAO.deleteTask(task.getId());
            SystemLogger.log("Task removed: " + task.getTitle());
            refreshTaskList();
        });

        rightActions.add(dueLbl);
        rightActions.add(Box.createHorizontalStrut(8));
        rightActions.add(deleteBtn);

        row.add(check, BorderLayout.CENTER);
        row.add(rightActions, BorderLayout.EAST);
        return row;
    }

    // --- RIGHT PANEL: MANUAL ADD & SYLLABUS PREVIEW ---
    private JPanel createRightActionPanel() {
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 15));
        right.setOpaque(false);

        // 1. Manual Task Addition Card
        JPanel manualCard = createHudCard();
        manualCard.setLayout(new BorderLayout(8, 8));

        JLabel manualTitle = new JLabel("ADD MANUAL TASK");
        manualTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        manualTitle.setForeground(Color.WHITE);
        manualCard.add(manualTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField titleField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().plusDays(1).toString());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        JLabel l1 = new JLabel("Title:"); l1.setForeground(Color.WHITE); form.add(l1, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; form.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        JLabel l2 = new JLabel("Due Date:"); l2.setForeground(Color.WHITE); form.add(l2, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; form.add(dateField, gbc);

        JButton addBtn = new JButton("+ CREATE TASK");
        addBtn.setBackground(new Color(28, 176, 246));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String dateStr = dateField.getText().trim();
            if (!title.isEmpty()) {
                int userId = UserSession.getInstance().getUserId();
                LocalDate dueDate = LocalDate.parse(dateStr);
                taskDAO.addTask(userId, title, dueDate);
                SystemLogger.log("Manual task added: " + title);
                titleField.setText("");
                refreshTaskList();
            }
        });

        gbc.gridx = 1; gbc.gridy = 2;
        form.add(addBtn, gbc);

        manualCard.add(form, BorderLayout.CENTER);

        // 2. Syllabus Import & Outline Preview Card
        JPanel syllabusCard = createHudCard();
        syllabusCard.setLayout(new BorderLayout(8, 8));

        JPanel sylHeader = new JPanel(new BorderLayout());
        sylHeader.setOpaque(false);
        JLabel sylTitle = new JLabel("SYLLABUS OUTLINE PREVIEW");
        sylTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sylTitle.setForeground(Color.WHITE);

        JButton uploadBtn = new JButton("UPLOAD PDF");
        uploadBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        uploadBtn.setBackground(new Color(34, 197, 94));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.addActionListener(e -> parseSyllabusToPreview());

        sylHeader.add(sylTitle, BorderLayout.WEST);
        sylHeader.add(uploadBtn, BorderLayout.EAST);
        syllabusCard.add(sylHeader, BorderLayout.NORTH);

        // Preview Table (Editable)
        previewTableModel = new DefaultTableModel(new String[]{"Module / Task Title", "Due Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Allows double-clicking to edit cells directly
            }
        };
        previewTable = new JTable(previewTableModel);
        previewTable.setBackground(new Color(15, 20, 24));
        previewTable.setForeground(Color.WHITE);
        previewTable.setGridColor(new Color(38, 52, 64));
        previewTable.setSelectionBackground(new Color(38, 52, 64));
        
        JScrollPane tableScroll = new JScrollPane(previewTable);
        tableScroll.getViewport().setBackground(new Color(15, 20, 24));
        syllabusCard.add(tableScroll, BorderLayout.CENTER);

        // Table Controls (Edit Date / Delete Row)
        JPanel tableControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableControls.setOpaque(false);
        tableControls.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JButton editDateBtn = new JButton("✎ Change Date");
        editDateBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        editDateBtn.setForeground(new Color(56, 189, 248));
        editDateBtn.setContentAreaFilled(false);
        editDateBtn.setBorder(null);
        editDateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editDateBtn.addActionListener(e -> {
            int row = previewTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a task to change its date.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String currentDate = (String) previewTableModel.getValueAt(row, 1);
            String newDate = JOptionPane.showInputDialog(this, "Enter new due date (YYYY-MM-DD):", currentDate);
            if (newDate != null && !newDate.trim().isEmpty()) {
                try {
                    LocalDate.parse(newDate.trim()); // Validate date formatting
                    previewTableModel.setValueAt(newDate.trim(), row, 1);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton deleteRowBtn = new JButton("✕ Remove Selected");
        deleteRowBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        deleteRowBtn.setForeground(new Color(248, 113, 113));
        deleteRowBtn.setContentAreaFilled(false);
        deleteRowBtn.setBorder(null);
        deleteRowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteRowBtn.addActionListener(e -> {
            int[] rows = previewTable.getSelectedRows();
            if (rows.length == 0) {
                JOptionPane.showMessageDialog(this, "Select a task to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Remove from bottom to top so row indices don't shift during deletion
            for (int i = rows.length - 1; i >= 0; i--) {
                previewTableModel.removeRow(rows[i]);
            }
        });

        tableControls.add(editDateBtn);
        tableControls.add(deleteRowBtn);

        // Group the table controls and approve button at the bottom
        JPanel bottomGroup = new JPanel(new BorderLayout());
        bottomGroup.setOpaque(false);
        bottomGroup.add(tableControls, BorderLayout.NORTH);

        JButton approveBtn = new JButton("APPROVE & SAVE OUTLINE");
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        approveBtn.setBackground(new Color(56, 189, 248));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        approveBtn.addActionListener(e -> approveAndSaveOutline());
        
        bottomGroup.add(approveBtn, BorderLayout.SOUTH);
        syllabusCard.add(bottomGroup, BorderLayout.SOUTH);

        right.add(manualCard);
        right.add(syllabusCard);
        return right;
    }

    private void parseSyllabusToPreview() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Syllabus", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPdfFile = chooser.getSelectedFile();
            try (PDDocument doc = Loader.loadPDF(selectedPdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc);
                String[] lines = text.split("\\r?\\n");
                
                StringBuilder contentBuilder = new StringBuilder();
                
                java.util.Set<String> ignoreList = java.util.Set.of(
                    "SEMESTER", "DIGITAL ELECTRONICS", "COURSE CODE", "CIE MARKS",
                    "TEACHING HOURS", "ESE MARKS", "CREDITS", "EXAM HOURS",
                    "PREREQUISITES", "COURSE TYPE", "COURSE OBJECTIVES", "SYLLABUS",
                    "MODULE", "SYLLABUS DESCRIPTION", "CONTACT", "PAGE", "HOURS"
                );

                for (String line : lines) {
                    String trimmed = line.trim();
                    String upper = trimmed.toUpperCase();
                    
                    if (trimmed.length() < 3 || trimmed.matches("^\\d+$") || trimmed.matches("^\\d+\\s+\\d+$")) {
                        continue; 
                    }
                    
                    boolean isNoise = false;
                    for (String noise : ignoreList) {
                        if (upper.startsWith(noise) || upper.contains("PAGE ")) {
                            isNoise = true;
                            break;
                        }
                    }
                    
                    if (!isNoise) {
                        contentBuilder.append(trimmed).append(" "); 
                    }
                }

                String cleanText = contentBuilder.toString().replaceAll("\\s+", " ");
                String[] sentences = cleanText.split("\\.");

                previewTableModel.setRowCount(0);
                int dayOffset = 1;
                int count = 0;

                for (String sentence : sentences) {
                    String topic = sentence.trim();
                    
                    if (topic.length() > 15) {
                        if (topic.length() > 120) {
                            topic = topic.substring(0, 117) + "...";
                        }
                        
                        previewTableModel.addRow(new Object[]{topic + ".", LocalDate.now().plusDays(dayOffset).toString()});
                        count++;
                        dayOffset += 1; 
                        
                        if (count >= 50) break; 
                    }
                }

                if (count == 0) {
                    previewTableModel.addRow(new Object[]{"Review Syllabus: " + selectedPdfFile.getName(), LocalDate.now().plusDays(1).toString()});
                }

                SystemLogger.log("Syllabus parsed into granular tasks: " + selectedPdfFile.getName());
                JOptionPane.showMessageDialog(this, "Deep Scan Complete!\nExtracted " + count + " individual study portions.", "Preview Ready", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error parsing PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void approveAndSaveOutline() {
        if (previewTable.isEditing()) {
            previewTable.getCellEditor().stopCellEditing();
        }

        int rows = previewTableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this, "No outline portions to approve. Upload a syllabus first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        int savedCount = 0;
        
        for (int i = 0; i < rows; i++) {
            String title = (String) previewTableModel.getValueAt(i, 0);
            String dateStr = (String) previewTableModel.getValueAt(i, 1);
            
            try {
                LocalDate date = LocalDate.parse(dateStr);
                taskDAO.addTask(userId, title, date);
                savedCount++;
            } catch (DateTimeParseException ex) {
                SystemLogger.log("Failed to save task: " + title + " (Invalid Date Format)");
            }
        }

        // SAVE DOCUMENT RECORD TO REPOSITORY TABLE
        if (selectedPdfFile != null && savedCount > 0) {
            DocumentDAO docDAO = new DocumentDAO();
            docDAO.insertDocument(userId, selectedPdfFile.getName(), savedCount);
        }

        SystemLogger.log("Approved and saved " + savedCount + " syllabus tasks.");
        JOptionPane.showMessageDialog(this, savedCount + " study tasks successfully approved and saved to database!", "Success", JOptionPane.INFORMATION_MESSAGE);
        previewTableModel.setRowCount(0);
        refreshTaskList();
    }

    private JPanel createHudCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(24, 32, 38));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return card;
    }
}