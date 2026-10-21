package ui;

import db.DocumentDAO;
import db.StatsDAO;
import db.TaskDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private MainFrame parentFrame;
    private TaskDAO taskDAO;
    private StatsDAO statsDAO;
    private DocumentDAO documentDAO;

    public DashboardPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.taskDAO = new TaskDAO();
        this.statsDAO = new StatsDAO();
        this.documentDAO = new DocumentDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. TOP STATUS BAR & GREETING
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER CONTENT (Split: Left Task Engine / Right Quick Repos & Event Log)
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);

        // Top Metrics Grid (4 Bento Cards)
        mainContent.add(createMetricsGrid(), BorderLayout.NORTH);

        // Center Split (Tasks vs Repository/Logs)
        JPanel splitPanel = new JPanel(new GridBagLayout());
        splitPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Left Column: Today's Work Nodes (65% width)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.65; gbc.weighty = 1.0;
        splitPanel.add(createTodaysWorkPanel(), gbc);

        // Right Column: Repository & Event Log (35% width)
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35; gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 15, 0, 0);
        splitPanel.add(createRightSidePanel(), gbc);

        mainContent.add(splitPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        String username = UserSession.getInstance().getUsername();
        if (username == null || username.isEmpty()) username = "OPERATOR_01";

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        JLabel greetingLabel = new JLabel("Good morning, " + username + ".");
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        greetingLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("All local study logs synchronized with database. Objective nodes active.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(140, 160, 175));

        textGroup.add(greetingLabel);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subLabel);

        header.add(textGroup, BorderLayout.WEST);
        return header;
    }

    private JPanel createMetricsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        int userId = logic.UserSession.getInstance().getUserId();
        int xp = (userId != -1) ? statsDAO.getXp(userId) : 0;

        int streak = 0;
        if (userId != -1) {
            boolean[] matrixLogs = statsDAO.getConsistencyMatrix(userId);
            for (int i = 27; i >= 0; i--) {
                if (matrixLogs[i]) streak++;
                else break;
            }
        }

        int level = (xp / 100) + 1;

        List<Task> tasks = taskDAO.getTasksForUser(userId);
        long completedCount = tasks.stream().filter(Task::isCompleted).count();
        int totalTasks = tasks.size();

        grid.add(createMetricCard("CURRENT LEVEL", "LEVEL " + level, xp + " / " + (level * 100) + " XP", "PROGRESSION TO NEXT LVL", new Color(56, 189, 248)));
        grid.add(createMetricCard("CURRENT STREAK", streak + " DAYS", "STABLE RECORD", "RECORD TIER: " + streak + " DAYS", new Color(245, 158, 11)));
        
        String rate = totalTasks > 0 ? (int)(((double)completedCount / totalTasks) * 100) + "% RATE" : "0% RATE";
        grid.add(createMetricCard("TODAY'S OBJECTIVES", completedCount + "/" + totalTasks + " COMPLETE", rate, "EXP HARVEST: +" + (completedCount * 50), new Color(34, 197, 94)));
        grid.add(createMetricCard("TOTAL STUDY TIME", (xp * 2) + "m", "LIFETIME LOGGED", "ACTIVE SESSION: Today " + (completedCount * 25) + "m", new Color(56, 189, 248)));
        
        return grid;
    }

    private JPanel createMetricCard(String title, String value, String sub, String footer, Color accentColor) {
        JPanel card = createHudCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(130, 150, 165));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(accentColor);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(170, 185, 200));

        JLabel footLbl = new JLabel(footer);
        footLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        footLbl.setForeground(new Color(100, 120, 135));

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valLbl);
        card.add(subLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(footLbl);

        return card;
    }

    private JPanel createTodaysWorkPanel() {
        JPanel panel = createHudCard();
        panel.setLayout(new BorderLayout(10, 10));

        // Header Container
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topRow.setOpaque(false);

        JLabel iconLbl = new JLabel("🗲");
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        iconLbl.setForeground(new Color(56, 189, 248));

        JLabel title = new JLabel("TODAY'S WORK NODES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);

        topRow.add(iconLbl);
        topRow.add(title);

        // Highlighted Solid Blue Start Focus Button
        JButton viewAllBtn = new JButton("START FOCUS");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAllBtn.setBackground(new Color(28, 176, 246)); // Sapphire Blue
        viewAllBtn.setForeground(Color.WHITE);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Redirect to Focus Timer View
        viewAllBtn.addActionListener(e -> parentFrame.switchView("POMODORO"));

        headerContainer.add(topRow, BorderLayout.WEST);
        headerContainer.add(viewAllBtn, BorderLayout.EAST);

        panel.add(headerContainer, BorderLayout.NORTH);

        // Task List Nodes
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        int userId = UserSession.getInstance().getUserId();
        List<Task> tasks = taskDAO.getTasksForUser(userId);

        if (tasks.isEmpty()) {
            JLabel emptyLbl = new JLabel("No study nodes active for today. Import a syllabus to generate targets.");
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLbl.setForeground(Color.GRAY);
            listContainer.add(emptyLbl);
        } else {
            int nodeNum = 1;
            for (Task task : tasks) {
                listContainer.add(createTaskNodeRow(task, nodeNum++));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom Pomodoro Bar
        JPanel timerBar = new JPanel(new BorderLayout());
        timerBar.setOpaque(false);
        timerBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 45, 55)));

        JLabel timerLbl = new JLabel("⏱ POMODORO ENGINE: READY (25m Focus / 5m Break)");
        timerLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 11));
        timerLbl.setForeground(new Color(140, 160, 175));

        JButton quick25Btn = new JButton("START SESSION");
        quick25Btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quick25Btn.setBackground(new Color(34, 197, 94));
        quick25Btn.setForeground(Color.WHITE);
        quick25Btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quick25Btn.addActionListener(e -> parentFrame.switchView("POMODORO"));

        timerBar.add(timerLbl, BorderLayout.WEST);
        timerBar.add(quick25Btn, BorderLayout.EAST);
        panel.add(timerBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTaskNodeRow(Task task, int index) {
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
                SystemLogger.log("Objective verified: " + task.getTitle() + " (+50 XP)");
            } else {
                SystemLogger.log("Objective unchecked: " + task.getTitle());
            }
        });

        JLabel statusBadge = new JLabel(task.isCompleted() ? "✓ VERIFIED (+50 XP)" : "READY");
        statusBadge.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        statusBadge.setForeground(task.isCompleted() ? new Color(34, 197, 94) : new Color(56, 189, 248));

        row.add(check, BorderLayout.CENTER);
        row.add(statusBadge, BorderLayout.EAST);
        return row;
    }

    private JPanel createRightSidePanel() {
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 15));
        right.setOpaque(false);

        // 1. Quick Repository (Dynamic database read)
        JPanel repoCard = createHudCard();
        repoCard.setLayout(new BorderLayout(10, 10));

        JLabel repoTitle = new JLabel("QUICK REPOSITORY");
        repoTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        repoTitle.setForeground(Color.WHITE);
        repoCard.add(repoTitle, BorderLayout.NORTH);

        JPanel docList = new JPanel();
        docList.setLayout(new BoxLayout(docList, BoxLayout.Y_AXIS));
        docList.setOpaque(false);

        int userId = UserSession.getInstance().getUserId();
        List<DocumentDAO.DocumentRecord> documents = documentDAO.getDocumentsForUser(userId);

        if (documents.isEmpty()) {
            JLabel emptyDocs = new JLabel("No files uploaded yet. Click below to import.");
            emptyDocs.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyDocs.setForeground(new Color(120, 140, 155));
            docList.add(emptyDocs);
        } else {
            for (DocumentDAO.DocumentRecord doc : documents) {
                docList.add(createDocItem(doc.getFileName(), doc.getCompletedModules(), doc.getTotalModules()));
                docList.add(Box.createVerticalStrut(8));
            }
        }

        repoCard.add(docList, BorderLayout.CENTER);

        JButton uploadBtn = new JButton("+ UPLOAD FILE");
        uploadBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        uploadBtn.setForeground(new Color(56, 189, 248));
        uploadBtn.setContentAreaFilled(false);
        uploadBtn.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.addActionListener(e -> parentFrame.switchView("PDF_IMPORT"));
        repoCard.add(uploadBtn, BorderLayout.SOUTH);

        // 2. System Event Log
        JPanel logCard = createHudCard();
        logCard.setLayout(new BorderLayout(10, 10));

        JLabel logTitle = new JLabel("SYSTEM EVENT LOG");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logTitle.setForeground(Color.WHITE);
        logCard.add(logTitle, BorderLayout.NORTH);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        logArea.setBackground(new Color(12, 16, 20));
        logArea.setForeground(new Color(34, 197, 94));

        SystemLogger.registerTextArea(logArea);

        logCard.add(new JScrollPane(logArea), BorderLayout.CENTER);

        right.add(repoCard);
        right.add(logCard);
        return right;
    }

    private JPanel createDocItem(String name, int completedModules, int totalModules) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel iconLbl = new JLabel("▤ ");
        iconLbl.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        iconLbl.setForeground(new Color(56, 189, 248));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nameLbl.setForeground(new Color(220, 230, 240));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(iconLbl);
        titleRow.add(nameLbl);

        JProgressBar progressBar = new JProgressBar(0, totalModules);
        progressBar.setValue(completedModules);
        progressBar.setPreferredSize(new Dimension(180, 5));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        progressBar.setForeground(new Color(56, 189, 248));
        progressBar.setBackground(new Color(30, 42, 52));
        progressBar.setBorderPainted(false);

        int percentage = (totalModules > 0) ? (int)(((double) completedModules / totalModules) * 100) : 0;
        JLabel metaLbl = new JLabel(completedModules + "/" + totalModules + " Modules Complete (" + percentage + "%)");
        metaLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        metaLbl.setForeground(new Color(120, 140, 155));

        JPanel bottomGroup = new JPanel();
        bottomGroup.setLayout(new BoxLayout(bottomGroup, BoxLayout.Y_AXIS));
        bottomGroup.setOpaque(false);
        bottomGroup.add(progressBar);
        bottomGroup.add(Box.createVerticalStrut(3));
        bottomGroup.add(metaLbl);

        p.add(titleRow, BorderLayout.NORTH);
        p.add(bottomGroup, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createHudCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(24, 32, 38));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return card;
    }
}