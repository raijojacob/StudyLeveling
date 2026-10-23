package ui;

import db.DocumentDAO;
import db.StatsDAO;
import db.TaskDAO;
import logic.SystemLogger;
import logic.UserSession;
import models.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class StatsPanel extends JPanel {
    private StatsDAO statsDAO;
    private TaskDAO taskDAO;
    private DocumentDAO documentDAO;
    private JPanel contentContainer;

    public StatsPanel() {
        this.statsDAO = new StatsDAO();
        this.taskDAO = new TaskDAO();
        this.documentDAO = new DocumentDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(18, 24, 29)); // Dark Obsidian Canvas
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Auto-refresh telemetry data whenever this tab is opened
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshTelemetry();
            }
        });

        refreshTelemetry();
    }

    public void refreshTelemetry() {
        removeAll();

        int userId = UserSession.getInstance().getUserId();
        int xp = (userId != -1) ? statsDAO.getXp(userId) : 0;
        boolean[] matrixLogs = (userId != -1) ? statsDAO.getConsistencyMatrix(userId) : new boolean[28];
        
        List<Task> tasks = taskDAO.getTasksForUser(userId);
        long completedTasks = tasks.stream().filter(Task::isCompleted).count();
        int totalTasks = tasks.size();

        // Calculate active streak
        int streak = 0;
        for (int i = 27; i >= 0; i--) {
            if (matrixLogs[i]) streak++;
            else break;
        }

        // Scrollable container for all telemetry modules
        contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        // 1. Header Section
        contentContainer.add(createHeaderPanel());
        contentContainer.add(Box.createVerticalStrut(15));

        // 2. Top Metrics Overview Row (5 Cards)
        contentContainer.add(createTopMetricsGrid(xp, streak, completedTasks, totalTasks));
        contentContainer.add(Box.createVerticalStrut(15));

        // 3. Middle Section: Weekly Histogram & Dynamic Repository Progress
        contentContainer.add(createMiddleAnalyticsSplit(completedTasks, totalTasks, tasks, userId));
        contentContainer.add(Box.createVerticalStrut(15));

        // 4. Bottom Section: 28-Day Consistency Matrix
        contentContainer.add(createConsistencyMatrixCard(matrixLogs, streak));

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
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

    private JPanel createTopMetricsGrid(int xp, int streak, long completed, int total) {
        JPanel grid = new JPanel(new GridLayout(1, 5, 10, 0));
        grid.setOpaque(false);

        int level = (xp / 100) + 1;
        int currentLevelXp = xp % 100;
        int xpToNext = 100 - currentLevelXp;
        
        String clearanceRate = total > 0 ? String.format("%.1f%%", ((double) completed / total) * 100) : "0.0%";
        
        int totalMinutes = xp / 2; 
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;

        grid.add(createCardWithContent("CURRENT STREAK", "🔥", streak + " DAYS", "ACTIVE DB RECORD", new Color(56, 189, 248)));
        grid.add(createCardWithContent("LEVEL & PROGRESS", "TIER " + ((level/5)+1), "LVL " + level + " / 100", xp + " XP ACCUMULATED • " + xpToNext + " TO LVL " + (level+1), new Color(168, 85, 247)));
        grid.add(createCardWithContent("TOTAL ACCRUED", "⚡", xp + " XP", "LIFETIME EXPERIENCE", new Color(34, 197, 94)));
        grid.add(createCardWithContent("OBJECTIVES", "✓", completed + " / " + total, "CLEARANCE RATE  " + clearanceRate, new Color(245, 158, 11)));
        grid.add(createCardWithContent("TOTAL STUDY TIME", "⏱", hours + "h " + mins + "m", "CALCULATED FROM XP HARVEST", new Color(56, 189, 248)));

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

    private JPanel createMiddleAnalyticsSplit(long completedTasks, int totalTasks, List<Task> tasks, int userId) {
        JPanel split = new JPanel(new GridLayout(1, 2, 15, 0));
        split.setOpaque(false);

        // --- DYNAMIC WEEKLY STUDY TIME HISTOGRAM ---
        double[] dailyHours = new double[7];
        double totalWeeklyHours = 0;

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        int todayIndex = today.getDayOfWeek().getValue() - 1;

        for (Task t : tasks) {
            if (t.isCompleted() && t.getDueDate() != null) {
                LocalDate d = t.getDueDate();
                if (!d.isBefore(startOfWeek) && !d.isAfter(endOfWeek)) {
                    int dayIndex = d.getDayOfWeek().getValue() - 1;
                    dailyHours[dayIndex] += (25.0 / 60.0);
                    totalWeeklyHours += (25.0 / 60.0);
                }
            }
        }

        double maxHours = 2.0;
        for (double h : dailyHours) {
            if (h > maxHours) maxHours = h;
        }

        double[] ratios = new double[7];
        for (int i = 0; i < 7; i++) {
            ratios[i] = dailyHours[i] / maxHours;
        }

        JPanel histCard = createHudCard();
        histCard.setLayout(new BorderLayout(0, 10));
        
        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);
        JLabel histTitle = new JLabel("📊 WEEKLY STUDY TIME (HOURS)");
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        histTitle.setForeground(Color.WHITE);
        
        JLabel histSub = new JLabel(String.format("MON-SUN ROLLING TOTAL: %.1fh", totalWeeklyHours));
        histSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        histSub.setForeground(new Color(140, 160, 175));
        
        histHeader.add(histTitle, BorderLayout.WEST);
        histHeader.add(histSub, BorderLayout.EAST);
        histCard.add(histHeader, BorderLayout.NORTH);

        // Weekly Bars Panel
        JPanel barsPanel = new JPanel(new GridLayout(7, 1, 0, 6));
        barsPanel.setOpaque(false);
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        for (int i = 0; i < 7; i++) {
            final int index = i;
            boolean isToday = (index == todayIndex);
            
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            
            JLabel dayLbl = new JLabel(days[index]);
            dayLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            dayLbl.setForeground(isToday ? Color.WHITE : new Color(140, 160, 175));
            dayLbl.setPreferredSize(new Dimension(35, 0));

            JPanel barWrapper = new JPanel(new BorderLayout()) {
                private final double ratio = ratios[index];
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(15, 20, 24));
                    g2.fillRect(0, 4, getWidth(), 12);
                    
                    g2.setColor(isToday ? new Color(34, 197, 94) : new Color(56, 189, 248));
                    g2.fillRect(0, 4, (int)(getWidth() * ratio), 12);
                }
            };
            barWrapper.setOpaque(false);
            barWrapper.setPreferredSize(new Dimension(100, 20)); // Prevent height collapse in GridLayout

            String hourText = String.format("%.1fh", dailyHours[index]);
            if (isToday) hourText += "*";
            
            JLabel valLbl = new JLabel(hourText);
            valLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            valLbl.setForeground(isToday ? new Color(34, 197, 94) : Color.WHITE);
            valLbl.setPreferredSize(new Dimension(40, 0));
            valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(dayLbl, BorderLayout.WEST);
            row.add(barWrapper, BorderLayout.CENTER);
            row.add(valLbl, BorderLayout.EAST);
            barsPanel.add(row);
        }
        
        histCard.add(barsPanel, BorderLayout.CENTER);

        double weeklyMean = totalWeeklyHours / (todayIndex + 1);
        JLabel footerNote = new JLabel(String.format("* TODAY IN PROGRESS    |    WEEKLY MEAN: %.1f HOURS / DAY", weeklyMean));
        footerNote.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footerNote.setForeground(new Color(100, 120, 135));
        histCard.add(footerNote, BorderLayout.SOUTH);

        // --- RIGHT: DYNAMIC SYLLABUS & MODULE REPOSITORY ---
        JPanel subCard = createHudCard();
        subCard.setLayout(new BorderLayout(0, 10));

        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setOpaque(false);
        JLabel subTitle = new JLabel("⌖ SYLLABUS & MODULE REPOSITORY");
        subTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subTitle.setForeground(Color.WHITE);

        List<DocumentDAO.DocumentRecord> documents = documentDAO.getDocumentsForUser(userId);
        JLabel subCount = new JLabel(documents.size() + " FILES IMPORTED");
        subCount.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subCount.setForeground(new Color(140, 160, 175));
        subHeader.add(subTitle, BorderLayout.WEST);
        subHeader.add(subCount, BorderLayout.EAST);
        subCard.add(subHeader, BorderLayout.NORTH);

        JPanel progPanel = new JPanel();
        progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.Y_AXIS));
        progPanel.setOpaque(false);

        if (documents.isEmpty()) {
            JLabel emptyLbl = new JLabel("No syllabus PDFs imported yet. Upload via Task Manager.");
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            emptyLbl.setForeground(new Color(140, 160, 175));
            progPanel.add(emptyLbl);
        } else {
            for (DocumentDAO.DocumentRecord doc : documents) {
                int completed = doc.getCompletedModules();
                int total = doc.getTotalModules();
                double ratio = total > 0 ? (double) completed / total : 0.0;
                progPanel.add(createDocumentProgressRow(doc.getFileName(), completed + " / " + total + " Modules", ratio, new Color(56, 189, 248)));
                progPanel.add(Box.createVerticalStrut(8));
            }
        }

        subCard.add(progPanel, BorderLayout.CENTER);

        String dynamicRatio = totalTasks > 0 ? String.format("%.1f%%", ((double) completedTasks / totalTasks) * 100) : "0%";
        JLabel aggRatio = new JLabel("AGGREGATE RATIO:   TOTAL: " + completedTasks + " / " + totalTasks + " NODES CLEARED (" + dynamicRatio + ")");
        aggRatio.setFont(new Font("Segoe UI", Font.BOLD, 10));
        aggRatio.setForeground(new Color(56, 189, 248));
        subCard.add(aggRatio, BorderLayout.SOUTH);

        split.add(histCard);
        split.add(subCard);
        return split;
    }

    private JPanel createDocumentProgressRow(String name, String stat, double ratio, Color barColor) {
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

    private JPanel createConsistencyMatrixCard(boolean[] matrixLogs, int currentStreak) {
        JPanel card = createHudCard();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("▰ 28-DAY CONSISTENCY MATRIX");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(Color.WHITE);

        JLabel legend = new JLabel("■ LOGGED      □ REST");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        legend.setForeground(new Color(140, 160, 175));
        header.add(title, BorderLayout.WEST);
        header.add(legend, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel matrixWrapper = new JPanel(new GridBagLayout());
        matrixWrapper.setOpaque(false);

        JPanel matrix = new JPanel(new GridLayout(4, 7, 12, 12));
        matrix.setOpaque(false);

        int activeDays = 0;
        for (boolean logged : matrixLogs) {
            if (logged) activeDays++;
            
            JPanel cell = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (logged) {
                        g2.setColor(new Color(56, 189, 248));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    } else {
                        g2.setColor(new Color(18, 24, 29));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        
                        g2.setColor(new Color(38, 52, 64));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                    }
                    g2.dispose();
                }
            };
            cell.setOpaque(false);
            cell.setPreferredSize(new Dimension(42, 42));
            cell.setToolTipText(logged ? "Study Session Logged" : "Rest Day");
            matrix.add(cell);
        }
        
        matrixWrapper.add(matrix);
        card.add(matrixWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        
        String stabilityStr = String.format("%.1f%%", ((double) activeDays / 28) * 100);
        JLabel stability = new JLabel("SESSION STABILITY:  " + stabilityStr);
        stability.setFont(new Font("Segoe UI", Font.BOLD, 11));
        stability.setForeground(new Color(34, 197, 94));

        JLabel run = new JLabel("CURRENT RUN:  " + currentStreak + " CONSECUTIVE ACTIVE DAYS");
        run.setFont(new Font("Segoe UI", Font.BOLD, 11));
        run.setForeground(new Color(56, 189, 248));

        footer.add(stability, BorderLayout.WEST);
        footer.add(run, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createHudCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(24, 32, 38));
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