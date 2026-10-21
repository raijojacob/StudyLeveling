package ui;

import logic.GamificationEngine;
import models.UserStats;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {
    private JLabel xpLabel;
    private JLabel streakLabel;
    private JLabel dateLabel;
    private GamificationEngine gamificationEngine;

    public StatsPanel() {
        gamificationEngine = new GamificationEngine();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Your Gamer Profile & Stats", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // Stats Display Cards/Labels
        xpLabel = new JLabel("Total XP: 0", SwingConstants.CENTER);
        xpLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        xpLabel.setForeground(new Color(189, 147, 249)); // Purple XP theme

        streakLabel = new JLabel("Current Streak: 0 Days", SwingConstants.CENTER);
        streakLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        streakLabel.setForeground(new Color(80, 250, 123)); // Green streak theme

        dateLabel = new JLabel("Last Activity: None", SwingConstants.CENTER);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.LIGHT_GRAY);

        JButton refreshBtn = new JButton("Refresh Stats");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> loadStats());

        // Layout Assembly
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(xpLabel, gbc);

        gbc.gridy = 2;
        add(streakLabel, gbc);

        gbc.gridy = 3;
        add(dateLabel, gbc);

        gbc.gridy = 4;
        add(refreshBtn, gbc);

        // Load data immediately on open
        loadStats();
    }

    public void loadStats() {
        UserStats stats = gamificationEngine.getCurrentUserStats();
        if (stats != null) {
            xpLabel.setText("Total XP: " + stats.getTotalXp());
            streakLabel.setText("Current Streak: " + stats.getCurrentStreak() + " Days");
            dateLabel.setText("Last Activity: " + (stats.getLastCompletedDate() != null ? stats.getLastCompletedDate().toString() : "None"));
        }
    }
}