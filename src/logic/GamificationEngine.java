package logic;

import db.StatsDAO;

public class GamificationEngine {
    private StatsDAO statsDAO;
    
    // XP Constants
    public static final int POMODORO_XP = 50;
    public static final int TASK_XP = 50;

    public GamificationEngine() {
        this.statsDAO = new StatsDAO();
    }

    /**
     * Awards XP to the currently logged-in user.
     * Streaks are now dynamically calculated in StatsPanel based on task completion dates,
     * so this method only needs to handle the XP transaction.
     */
    public void addXpAndUpdateStreak(int xpAmount) {
        int userId = UserSession.getInstance().getUserId();
        if (userId != -1) {
            statsDAO.addXp(userId, xpAmount);
            SystemLogger.log("GamificationEngine: +" + xpAmount + " XP awarded to Operator " + userId);
        }
    }

    /**
     * Fetches the current user's XP directly from the live StatsDAO.
     * Replaces the deprecated method that returned a static Stats object.
     */
    public int getCurrentUserStats() {
        if (UserSession.getInstance().isLoggedIn()) {
            int userId = UserSession.getInstance().getUserId();
            return statsDAO.getXp(userId);
        }
        return 0;
    }
}