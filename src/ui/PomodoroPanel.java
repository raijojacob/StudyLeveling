package ui;

import db.StatsDAO;
import db.TaskDAO;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Arc2D;
import java.util.List;

public class PomodoroPanel extends JPanel {
    private CircularTimerRing timerRing;
    private JLabel statusLabel;
    private JLabel heartLabel;
    private JComboBox<TaskItem> taskDropdown;
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton abandonBtn;

    private Timer swingTimer;
    private int remainingSeconds = 25 * 60; // 25 Minutes Default
    private int totalSeconds = 25 * 60;
    private boolean isRunning = false;
    private int pauseTokens = 3;

    private StatsDAO statsDAO;
    private TaskDAO taskDAO;

    public PomodoroPanel() {
        statsDAO = new StatsDAO();
        taskDAO = new TaskDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel("FOCUS TIMER ENGINE");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("// 25m FOCUS INTERVAL • AUTOMATED XP HARVEST");
        subLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subLbl.setForeground(new Color(56, 189, 248));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);
        titleGroup.add(titleLbl);
        titleGroup.add(subLbl);

        header.add(titleGroup, BorderLayout.WEST);

        // Pause Token Hearts Display
        heartLabel = new JLabel("PAUSES: ♥ ♥ ♥");
        heartLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
        heartLabel.setForeground(new Color(248, 113, 113));
        header.add(heartLabel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // 2. Central Clock Card
        JPanel centerCard = new JPanel(new GridBagLayout());
        centerCard.setBackground(new Color(24, 32, 38));
        centerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(6, 10, 6, 10);

        statusLabel = new JLabel("READY FOR FOCUS SESSION", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(140, 160, 175));
        centerCard.add(statusLabel, gbc);

        // Custom Circular Ring
        gbc.gridy = 1;
        timerRing = new CircularTimerRing();
        centerCard.add(timerRing, gbc);

        // Target Task Link Selector
        gbc.gridy = 2;
        JPanel taskPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        taskPanel.setOpaque(false);
        
        JLabel taskLbl = new JLabel("TARGET TASK:");
        taskLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        taskLbl.setForeground(new Color(140, 160, 175));

        taskDropdown = new JComboBox<>();
        taskDropdown.setPreferredSize(new Dimension(250, 32));
        taskDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        taskDropdown.setBackground(new Color(18, 24, 29));
        taskDropdown.setForeground(new Color(226, 232, 240));
        taskDropdown.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        
        // Auto-refresh tasks from DB whenever the dropdown is opened
        taskDropdown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                TaskItem selected = (TaskItem) taskDropdown.getSelectedItem();
                loadUserTasks();
                if (selected != null) {
                    for (int i = 0; i < taskDropdown.getItemCount(); i++) {
                        if (taskDropdown.getItemAt(i).id == selected.id) {
                            taskDropdown.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        // Custom Dark Styling for Dropdown Menu Items
        taskDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (isSelected) {
                    lbl.setBackground(new Color(38, 52, 64)); // Dark slate highlight
                    lbl.setForeground(new Color(56, 189, 248)); // Sapphire Cyan
                } else {
                    lbl.setBackground(new Color(24, 32, 38)); // Slate card background
                    lbl.setForeground(new Color(226, 232, 240));
                }
                return lbl;
            }
        });

        loadUserTasks();

        taskPanel.add(taskLbl);
        taskPanel.add(taskDropdown);
        centerCard.add(taskPanel, gbc);

        // 3. Upgraded Pill-Shaped Preset Buttons Bar
        gbc.gridy = 3;
        JPanel presetBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        presetBar.setOpaque(false);

        JButton p5 = createPresetPillButton("5m Test", 5 * 60);
        JButton p25 = createPresetPillButton("25m Standard", 25 * 60);
        JButton p50 = createPresetPillButton("50m Deep Work", 50 * 60);
        JButton pCustom = createCustomTimerPillButton("⚙ Custom");

        presetBar.add(p5);
        presetBar.add(p25);
        presetBar.add(p50);
        presetBar.add(pCustom);
        centerCard.add(presetBar, gbc);

        add(centerCard, BorderLayout.CENTER);

        // 4. Bottom Controls Bar
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controlBar.setOpaque(false);

        startBtn = new JButton("START FOCUS");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        startBtn.setBackground(new Color(34, 197, 94));
        startBtn.setForeground(Color.WHITE);
        startBtn.setPreferredSize(new Dimension(140, 40));
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> startTimer());

        pauseBtn = new JButton("PAUSE");
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pauseBtn.setBackground(new Color(245, 158, 11));
        pauseBtn.setForeground(Color.WHITE);
        pauseBtn.setPreferredSize(new Dimension(110, 40));
        pauseBtn.setEnabled(false);
        pauseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseBtn.addActionListener(e -> pauseTimer());

        abandonBtn = new JButton("RESET");
        abandonBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        abandonBtn.setBackground(new Color(40, 55, 70));
        abandonBtn.setForeground(new Color(200, 210, 220));
        abandonBtn.setPreferredSize(new Dimension(110, 40));
        abandonBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        abandonBtn.addActionListener(e -> resetTimer());

        controlBar.add(startBtn);
        controlBar.add(pauseBtn);
        controlBar.add(abandonBtn);

        add(controlBar, BorderLayout.SOUTH);

        // Auto-refresh tasks whenever user switches to this panel tab
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadUserTasks();
            }
        });

        // Internal Swing Timer setup (1 second tick)
        swingTimer = new Timer(1000, e -> tick());
    }

    public void loadUserTasks() {
        taskDropdown.removeAllItems();
        taskDropdown.addItem(new TaskItem(-1, "-- General Study Session --"));

        int userId = UserSession.getInstance().getUserId();
        if (userId != -1) {
            List<Task> tasks = taskDAO.getTasksForUser(userId);
            for (Task t : tasks) {
                if (!t.isCompleted()) {
                    taskDropdown.addItem(new TaskItem(t.getId(), t.getTitle()));
                }
            }
        }
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            swingTimer.start();
            statusLabel.setText("● FOCUS SESSION ACTIVE");
            statusLabel.setForeground(new Color(34, 197, 94));
            timerRing.setRingColor(new Color(56, 189, 248)); // Sapphire Cyan
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);

            logic.SystemLogger.log("Focus Session started (" + (totalSeconds / 60) + "m timer)");
        }
    }

    private void pauseTimer() {
        if (isRunning && pauseTokens > 0) {
            isRunning = false;
            swingTimer.stop();
            pauseTokens--;
            updateHeartDisplay();

            statusLabel.setText("⏸ SESSION PAUSED (" + pauseTokens + " PAUSES LEFT)");
            statusLabel.setForeground(new Color(245, 158, 11));
            timerRing.setRingColor(new Color(245, 158, 11)); // Amber Warning
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);

            logic.SystemLogger.log("Focus Session paused (" + pauseTokens + " pauses left)");
        } else if (pauseTokens <= 0) {
            showHudMessage("Pause Limit", "No pause tokens remaining for this session!", new Color(245, 158, 11));
        }
    }

    private void resetTimer() {
        swingTimer.stop();
        isRunning = false;
        remainingSeconds = totalSeconds;
        pauseTokens = 3;
        updateClockDisplay();
        updateHeartDisplay();

        statusLabel.setText("READY FOR FOCUS SESSION");
        statusLabel.setForeground(new Color(140, 160, 175));
        timerRing.setRingColor(new Color(56, 189, 248));
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        loadUserTasks(); // Refresh task dropdown
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateClockDisplay();
        } else {
            swingTimer.stop();
            isRunning = false;

            // 1. Session Completed -> Award XP to user
            int userId = UserSession.getInstance().getUserId();
            if (userId != -1) {
                statsDAO.addXp(userId, 50);
            }

            // 2. Auto-complete linked task if one was selected
            TaskItem selected = (TaskItem) taskDropdown.getSelectedItem();
            if (selected != null && selected.id != -1) {
                taskDAO.updateTaskCompletion(selected.id, true);
            }

            logic.SystemLogger.log("Focus Session completed (+50 XP harvested)");

            statusLabel.setText("✓ SESSION COMPLETE! +50 XP HARVESTED");
            statusLabel.setForeground(new Color(34, 197, 94));
            timerRing.setRingColor(new Color(34, 197, 94));
            
            showHudMessage("Target Reached", "Focus Session Complete!<br>+50 XP added to your database profile.", new Color(34, 197, 94));
            resetTimer();
        }
    }

    private void showHudMessage(String title, String message, Color accentColor) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(24, 32, 38)); // Slate Card Background
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLbl = new JLabel("⚠ " + title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(accentColor);

        JLabel msgLbl = new JLabel("<html><div style='width:240px; color: #E2E8F0;'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton okBtn = new JButton("ACKNOWLEDGE");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        okBtn.setBackground(new Color(28, 176, 246)); // Sapphire Blue
        okBtn.setForeground(Color.WHITE);
        okBtn.setFocusPainted(false);
        okBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(msgLbl, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Custom Styled HUD Dialog for Setting Custom Timer Duration
    private void showCustomTimerDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Custom Timer", true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(24, 32, 38)); // Dark obsidian card background
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1), // Cyan outline
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel titleLbl = new JLabel("⚙ SET CUSTOM TIMER");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(new Color(56, 189, 248));

        JLabel promptLbl = new JLabel("Enter focus duration in minutes (1 - 180):");
        promptLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        promptLbl.setForeground(new Color(226, 232, 240));

        JTextField inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputField.setBackground(new Color(18, 24, 29));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(new Color(56, 189, 248));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        contentPanel.setOpaque(false);
        contentPanel.add(promptLbl);
        contentPanel.add(inputField);

        JButton confirmBtn = new JButton("CONFIRM");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        confirmBtn.setBackground(new Color(34, 197, 94)); // Green
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cancelBtn.setBackground(new Color(40, 55, 70));
        cancelBtn.setForeground(new Color(200, 210, 220));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        confirmBtn.addActionListener(e -> {
            String input = inputField.getText().trim();
            if (!input.isEmpty()) {
                try {
                    int mins = Integer.parseInt(input);
                    if (mins > 0 && mins <= 180) {
                        totalSeconds = mins * 60;
                        remainingSeconds = totalSeconds;
                        updateClockDisplay();
                        dialog.dispose();
                    } else {
                        promptLbl.setText("Duration must be between 1 and 180 minutes!");
                        promptLbl.setForeground(new Color(248, 113, 113));
                    }
                } catch (NumberFormatException ex) {
                    promptLbl.setText("Please enter a valid number!");
                    promptLbl.setForeground(new Color(248, 113, 113));
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setSize(360, dialog.getPreferredSize().height);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateClockDisplay() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerRing.setTimeText(String.format("%02d:%02d", mins, secs));
    }

    private void updateHeartDisplay() {
        StringBuilder sb = new StringBuilder("PAUSES: ");
        for (int i = 0; i < pauseTokens; i++) {
            sb.append("♥ ");
        }
        for (int i = pauseTokens; i < 3; i++) {
            sb.append("♡ ");
        }
        heartLabel.setText(sb.toString().trim());
    }

    // Helper: Creates larger, pill-shaped preset buttons
    private JButton createPresetPillButton(String text, int seconds) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(new Color(38, 52, 64)); // Dark slate pill background
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FlatLaf Pill Shape Arc
        btn.putClientProperty("JButton.arc", 999);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 72, 88), 1),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));

        btn.addActionListener(e -> {
            if (!isRunning) {
                totalSeconds = seconds;
                remainingSeconds = seconds;
                updateClockDisplay();
            }
        });
        return btn;
    }

    // Helper: Creates the Custom Timer Pill button
    private JButton createCustomTimerPillButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(56, 189, 248)); // Sapphire cyan text
        btn.setBackground(new Color(24, 32, 38));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // FlatLaf Pill Shape Arc
        btn.putClientProperty("JButton.arc", 999);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));

        btn.addActionListener(e -> {
            if (!isRunning) {
                showCustomTimerDialog();
            }
        });
        return btn;
    }

    // Task Item Helper Class for Dropdown
    private static class TaskItem {
        int id;
        String title;

        TaskItem(int id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    // Custom Component for Drawing the Antialiased Circular Progress Arc
    private class CircularTimerRing extends JComponent {
        private String timeText = "25:00";
        private Color ringColor = new Color(56, 189, 248);

        public CircularTimerRing() {
            setPreferredSize(new Dimension(230, 230));
        }

        public void setTimeText(String text) {
            this.timeText = text;
            repaint();
        }

        public void setRingColor(Color color) {
            this.ringColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) - 16;
            int x = (width - size) / 2;
            int y = (height - size) / 2;
            int strokeWidth = 8;

            // Background Track Ring
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(38, 52, 64));
            g2.drawOval(x, y, size, size);

            // Active Progress Arc
            double progress = totalSeconds > 0 ? (double) remainingSeconds / totalSeconds : 0.0;
            double angle = 360.0 * progress;
            g2.setColor(ringColor);
            g2.draw(new Arc2D.Double(x, y, size, size, 90, angle, Arc2D.OPEN));

            // Time Text in Center
            g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
            g2.setColor(ringColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (width - fm.stringWidth(timeText)) / 2;
            int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(timeText, textX, textY);

            g2.dispose();
        }
    }
}