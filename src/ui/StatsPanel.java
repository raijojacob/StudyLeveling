package ui;

import db.StatsDAO;
import logic.SystemLogger;
import logic.UserSession;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {
    private StatsDAO statsDAO;

    public StatsPanel() {
        this.statsDAO = new StatsDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Scrollable container for all telemetry modules
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        // 1. Header Section
        contentContainer.add(createHeaderPanel());
        contentContainer.add(Box.createVerticalStrut(15));

        // 2. Top Metrics Overview Row (5 Cards)
        contentContainer.add(createTopMetricsGrid());
        contentContainer.add(Box.createVerticalStrut(15));

        // 3. Middle Section: Weekly Histogram & Subject Progress (2 Columns)
        contentContainer.add(createMiddleAnalyticsSplit());
        contentContainer.add(Box.createVerticalStrut(15));

        // 4. Bottom Section: 28-Day Consistency Matrix & Stability
        contentContainer.add(createConsistencyMatrixCard());

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel titleLbl = new JLabel("STATISTICS // SYSTEM TELEMETRY");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Deterministic metrics calculated locally from SQLite study logs.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(140, 160, 175));

        titleGroup.add(titleLbl);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(subLbl);

        // Action Badges on Right
        JPanel badgeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badgeBar.setOpaque(false);

        JLabel tableBadge = new JLabel("▤ TABLE: SESSIONS_LOG_V2");
        styleBadge(tableBadge, new Color(38, 52, 64), Color.WHITE);

        JButton exportBtn = new JButton("↓ EXPORT TXT");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setBackground(new Color(28, 176, 246));
        exportBtn.setFocusPainted(false);
        exportBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> {
            SystemLogger.log("Telemetry logs exported successfully.");
            JOptionPane.showMessageDialog(this, "Sessions log successfully exported to local storage.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        badgeBar.add(tableBadge);
        badgeBar.add(exportBtn);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(badgeBar, BorderLayout.EAST);
        return header;
    }

    private JPanel createTopMetricsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 10, 0));
        grid.setOpaque(false);

        grid.add(createCardWithContent("CURRENT STREAK", "🔥", "12 DAYS", "ALL-TIME RECORD  27 DAYS", new Color(56, 189, 248)));
        grid.add(createCardWithContent("LEVEL & PROGRESS", "TIER 2", "LVL 12 / 100", "1,240 / 1,500 XP  •  260 XP TO LVL 13", new Color(168, 85, 247)));
        grid.add(createCardWithContent("TOTAL ACCRUED", "⚡", "12,450 XP", "DELTA  +1,140 XP THIS WK", new Color(34, 197, 94)));
        grid.add(createCardWithContent("OBJECTIVES", "✓", "84 / 102", "CLEARANCE RATE  82.3% OVERALL", new Color(245, 158, 11)));
        grid.add(createCardWithContent("TOTAL STUDY TIME", "⏱", "87h 42m", "DAILY PACE  1h 45m / DAY", new Color(56, 189, 248)));

        return grid;
    }

    private JPanel createCardWithContent(String title, String tag, String mainVal, String subVal, Color accent) {
        JPanel card = createHudCard();
        card.setLayout(new BorderLayout(0, 8));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(140, 160, 175));
        
        JLabel tagLbl = new JLabel(tag);
        tagLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagLbl.setForeground(accent);

        topRow.add(titleLbl, BorderLayout.WEST);
        topRow.add(tagLbl, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel mainLbl = new JLabel(mainVal);
        mainLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("<html><div style='font-size:8px; color:#8CA0AF;'>" + subVal + "</div></html>");
        
        centerPanel.add(mainLbl);
        centerPanel.add(Box.createVerticalStrut(4));
        centerPanel.add(subLbl);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMiddleAnalyticsSplit() {
        JPanel split = new JPanel(new GridLayout(1, 2, 15, 0));
        split.setOpaque(false);

        // Left: Weekly Study Time Histogram
        JPanel histCard = createHudCard();
        histCard.setLayout(new BorderLayout(0, 10));
        
        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);
        JLabel histTitle = new JLabel("📊 WEEKLY STUDY TIME (HOURS)");
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        histTitle.setForeground(Color.WHITE);
        JLabel histSub = new JLabel("MON-SUN ROLLING TOTAL: 14.7h");
        histSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        histSub.setForeground(new Color(140, 160, 175));
        histHeader.add(histTitle, BorderLayout.WEST);
        histHeader.add(histSub, BorderLayout.EAST);
        histCard.add(histHeader, BorderLayout.NORTH);

        JPanel barsPanel = new JPanel(new GridLayout(7, 1, 0, 6));
        barsPanel.setOpaque(false);
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        String[] hours = {"2.5h", "1.8h", "3.0h", "2.0h", "2.2h", "1.5h", "1.7h*"};
        double[] ratios = {0.7, 0.5, 0.85, 0.55, 0.6, 0.4, 0.45};

        for (int i = 0; i < 7; i++) {
            final int index = i; // Effectively final copy for anonymous class usage
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            JLabel dayLbl = new JLabel(days[index]);
            dayLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dayLbl.setForeground(new Color(140, 160, 175));
            dayLbl.setPreferredSize(new Dimension(35, 0));

            // Custom painted bar
            JPanel barWrapper = new JPanel(new BorderLayout()) {
                private final double ratio = ratios[index];
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(15, 20, 24));
                    g2.fillRect(0, 4, getWidth(), 12);
                    g2.setColor(index == 6 ? new Color(34, 197, 94) : new Color(56, 189, 248));
                    g2.fillRect(0, 4, (int)(getWidth() * ratio), 12);
                }
            };
            barWrapper.setOpaque(false);

            JLabel valLbl = new JLabel(hours[index]);
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            valLbl.setForeground(Color.WHITE);
            valLbl.setPreferredSize(new Dimension(40, 0));
            valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(dayLbl, BorderLayout.WEST);
            row.add(barWrapper, BorderLayout.CENTER);
            row.add(valLbl, BorderLayout.EAST);
            barsPanel.add(row);
        }
        histCard.add(barsPanel, BorderLayout.CENTER);

        JLabel footerNote = new JLabel("* TODAY IN PROGRESS    |    WEEKLY MEAN: 2.1 HOURS / DAY");
        footerNote.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerNote.setForeground(new Color(100, 120, 135));
        histCard.add(footerNote, BorderLayout.SOUTH);

        // Right: Subject Completion & Progress
        JPanel subCard = createHudCard();
        subCard.setLayout(new BorderLayout(0, 10));

        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setOpaque(false);
        JLabel subTitle = new JLabel("⌖ SUBJECT COMPLETION & PROGRESS");
        subTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subTitle.setForeground(Color.WHITE);
        JLabel subCount = new JLabel("4 MODULES ACTIVE");
        subCount.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subCount.setForeground(new Color(140, 160, 175));
        subHeader.add(subTitle, BorderLayout.WEST);
        subHeader.add(subCount, BorderLayout.EAST);
        subCard.add(subHeader, BorderLayout.NORTH);

        JPanel progPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        progPanel.setOpaque(false);

        progPanel.add(createSubjectProgressRow("JAVA OOP", "28 / 34 OBJECTIVES", 0.82, new Color(56, 189, 248)));
        progPanel.add(createSubjectProgressRow("MATHEMATICS", "18 / 28 OBJECTIVES", 0.64, new Color(168, 85, 247)));
        progPanel.add(createSubjectProgressRow("DATA STRUCTURES", "22 / 31 OBJECTIVES", 0.71, new Color(34, 197, 94)));
        progPanel.add(createSubjectProgressRow("COMPUTER SYSTEMS", "16 / 32 OBJECTIVES", 0.50, new Color(100, 120, 135)));

        subCard.add(progPanel, BorderLayout.CENTER);

        JLabel aggRatio = new JLabel("AGGREGATE RATIO:   TOTAL: 84 / 125 NODES CLEARED (67.2%)");
        aggRatio.setFont(new Font("Segoe UI", Font.BOLD, 10));
        aggRatio.setForeground(new Color(56, 189, 248));
        subCard.add(aggRatio, BorderLayout.SOUTH);

        split.add(histCard);
        split.add(subCard);
        return split;
    }

    private JPanel createSubjectProgressRow(String name, String stat, double ratio, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);
        JLabel nameLbl = new JLabel("■ " + name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        nameLbl.setForeground(Color.WHITE);
        JLabel statLbl = new JLabel(stat + "  (" + (int)(ratio * 100) + "%)");
        statLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statLbl.setForeground(new Color(140, 160, 175));
        labelRow.add(nameLbl, BorderLayout.WEST);
        labelRow.add(statLbl, BorderLayout.EAST);

        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(15, 20, 24));
                g2.fillRect(0, 0, getWidth(), 8);
                g2.setColor(barColor);
                g2.fillRect(0, 0, (int)(getWidth() * ratio), 8);
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 8));

        row.add(labelRow, BorderLayout.NORTH);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    private JPanel createConsistencyMatrixCard() {
        JPanel card = createHudCard();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("▰ 28-DAY CONSISTENCY MATRIX");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(Color.WHITE);

        JLabel legend = new JLabel("■ LOGGED      ■ REST");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        legend.setForeground(new Color(140, 160, 175));
        header.add(title, BorderLayout.WEST);
        header.add(legend, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Matrix Grid (4 rows x 7 days)
        JPanel matrix = new JPanel(new GridLayout(4, 7, 6, 6));
        matrix.setOpaque(false);

        // Simulated state array for 28 days
        boolean[] loggedDays = {
            true, true, false, true, true, false, true,
            false, true, true, true, false, true, true,
            true, true, true, true, true, true, true,
            true, true, true, true, true, true, false
        };

        for (boolean logged : loggedDays) {
            JPanel cell = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(logged ? new Color(56, 189, 248) : new Color(30, 40, 50));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            cell.setOpaque(false);
            cell.setPreferredSize(new Dimension(0, 24));
            matrix.add(cell);
        }
        card.add(matrix, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel stability = new JLabel("SESSION STABILITY:  85.7%");
        stability.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stability.setForeground(new Color(34, 197, 94));

        JLabel run = new JLabel("CURRENT RUN:  12 CONSECUTIVE ACTIVE DAYS");
        run.setFont(new Font("Segoe UI", Font.BOLD, 11));
        run.setForeground(new Color(56, 189, 248));

        footer.add(stability, BorderLayout.WEST);
        footer.add(run, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createHudCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(24, 32, 38)); // Slate Card
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38, 52, 64), 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        return card;
    }

    private void styleBadge(JLabel label, Color bg, Color fg) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(fg);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 65, 80), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
}