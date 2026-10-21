package ui;

import db.StatsDAO;
import logic.UserSession;

import javax.swing.*;
import java.awt.*;

public class PomodoroPanel extends JPanel {
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JLabel heartLabel;
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton abandonBtn;

    private Timer swingTimer;
    private int remainingSeconds = 25 * 60; // 25 Minutes Default
    private boolean isRunning = false;
    private int pauseTokens = 3;

    private StatsDAO statsDAO;

    public PomodoroPanel() {
        statsDAO = new StatsDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

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
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        statusLabel = new JLabel("READY FOR FOCUS SESSION", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(140, 160, 175));
        centerCard.add(statusLabel, gbc);

        gbc.gridy = 1;
        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        timerLabel.setForeground(new Color(56, 189, 248)); // Sapphire Cyan
        centerCard.add(timerLabel, gbc);

        // Quick Preset Buttons
        gbc.gridy = 2;
        JPanel presetBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        presetBar.setOpaque(false);

        JButton p5 = createPresetButton("5m Test", 5 * 60);
        JButton p25 = createPresetButton("25m Standard", 25 * 60);
        JButton p50 = createPresetButton("50m Deep Work", 50 * 60);

        presetBar.add(p5);
        presetBar.add(p25);
        presetBar.add(p50);
        centerCard.add(presetBar, gbc);

        add(centerCard, BorderLayout.CENTER);

        // 3. Bottom Controls Bar
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

        // Internal Swing Timer setup (1 second tick)
        swingTimer = new Timer(1000, e -> tick());
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            swingTimer.start();
            statusLabel.setText("● FOCUS SESSION ACTIVE");
            statusLabel.setForeground(new Color(34, 197, 94));
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);

            // Log session start event
            logic.SystemLogger.log("Focus Session started (25m timer)");
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
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);

            // Log session pause event
            logic.SystemLogger.log("Focus Session paused (" + pauseTokens + " pauses left)");
        } else if (pauseTokens <= 0) {
            showHudMessage("Pause Limit", "No pause tokens remaining for this session!", new Color(245, 158, 11));
        }
    }

    private void resetTimer() {
        swingTimer.stop();
        isRunning = false;
        remainingSeconds = 25 * 60;
        pauseTokens = 3;
        updateClockDisplay();
        updateHeartDisplay();

        statusLabel.setText("READY FOR FOCUS SESSION");
        statusLabel.setForeground(new Color(140, 160, 175));
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateClockDisplay();
        } else {
            swingTimer.stop();
            isRunning = false;

            // Session Completed -> Award XP to user
            int userId = UserSession.getInstance().getUserId();
            if (userId != -1) {
                statsDAO.addXp(userId, 50);
            }

            // Log session completion event
            logic.SystemLogger.log("Focus Session completed (+50 XP harvested)");

            statusLabel.setText("✓ SESSION COMPLETE! +50 XP HARVESTED");
            statusLabel.setForeground(new Color(34, 197, 94));
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

    private void updateClockDisplay() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
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

    private JButton createPresetButton(String text, int seconds) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(170, 185, 200));
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(50, 65, 80), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (!isRunning) {
                remainingSeconds = seconds;
                updateClockDisplay();
            }
        });
        return btn;
    }
}