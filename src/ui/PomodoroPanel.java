package ui;

import logic.PomodoroEngine;
import logic.PomodoroEngine.PomodoroListener;

import javax.swing.*;
import java.awt.*;

public class PomodoroPanel extends JPanel implements PomodoroListener {
    private PomodoroEngine engine;
    
    private JLabel timeDisplay;
    private JLabel pauseInfoLabel;
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton abandonBtn;

    public PomodoroPanel() {
        engine = new PomodoroEngine(this);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Title
        JLabel titleLabel = new JLabel("Deep Focus Session", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        // 2. Giant Clock Display
        timeDisplay = new JLabel(engine.getFormattedTime(), SwingConstants.CENTER);
        timeDisplay.setFont(new Font("Segoe UI", Font.BOLD, 120)); // Massive font
        timeDisplay.setForeground(new Color(80, 250, 123)); // Neon green theme for active timer

        // 3. Pause Tracker
        pauseInfoLabel = new JLabel("Pauses remaining: 3", SwingConstants.CENTER);
        pauseInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pauseInfoLabel.setForeground(Color.LIGHT_GRAY);

        // 4. Control Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        startBtn = new JButton("Start Focus");
        pauseBtn = new JButton("Pause");
        abandonBtn = new JButton("Abandon Session");

        // Style buttons
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        abandonBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        abandonBtn.setForeground(new Color(255, 85, 85)); // Red warning text
        pauseBtn.setEnabled(false); // Can't pause before starting
        abandonBtn.setEnabled(false); // Can't abandon before starting

        buttonPanel.add(startBtn);
        buttonPanel.add(pauseBtn);
        buttonPanel.add(abandonBtn);

        // 5. Layout Assembly
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(timeDisplay, gbc);

        gbc.gridy = 2;
        add(pauseInfoLabel, gbc);

        gbc.gridy = 3;
        add(buttonPanel, gbc);

        // 6. Button Listeners
        startBtn.addActionListener(e -> handleStart());
        pauseBtn.addActionListener(e -> handlePause());
        abandonBtn.addActionListener(e -> handleAbandon());
    }

    private void handleStart() {
        engine.start();
        startBtn.setEnabled(false);
        abandonBtn.setEnabled(true);
        updatePauseButtonState();
        timeDisplay.setForeground(new Color(80, 250, 123)); // Active green
    }

    private void handlePause() {
        if (engine.pause()) {
            startBtn.setEnabled(true);
            startBtn.setText("Resume");
            pauseBtn.setEnabled(false);
            timeDisplay.setForeground(new Color(241, 250, 140)); // Paused yellow
        }
    }

    private void handleAbandon() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Abandoning this session means you earn 0 XP. Are you sure?",
            "Abandon Session",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            engine.abandon();
            resetUI();
        }
    }

    private void resetUI() {
        startBtn.setEnabled(true);
        startBtn.setText("Start Focus");
        pauseBtn.setEnabled(false);
        abandonBtn.setEnabled(false);
        timeDisplay.setForeground(Color.WHITE); // Default white
    }

    private void updatePauseButtonState() {
        pauseBtn.setEnabled(engine.isRunning() && engine.getPausesRemaining() > 0);
    }

    // --- PomodoroListener Interface Methods ---

    @Override
    public void onTick(String formattedTime) {
        timeDisplay.setText(formattedTime);
    }

    @Override
    public void onPauseStateChanged(int pausesRemaining) {
        pauseInfoLabel.setText("Pauses remaining: " + pausesRemaining);
        if (pausesRemaining == 0) {
            pauseInfoLabel.setText("No pauses remaining!");
            pauseInfoLabel.setForeground(new Color(255, 85, 85)); // Red
        } else {
            pauseInfoLabel.setForeground(Color.LIGHT_GRAY);
        }
    }

    @Override
    public void onTimerComplete() {
        resetUI();
        timeDisplay.setForeground(new Color(189, 147, 249)); // Victory purple
        
        // Reward the user and save to database
        logic.GamificationEngine engine = new logic.GamificationEngine();
        engine.rewardPomodoroCompletion();
        
        JOptionPane.showMessageDialog(this, "Session Complete! You earned +50 XP and updated your streak!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}