package logic;

import db.StatsDAO;
import models.UserStats;

public class GamificationEngine {
    
    private static final int POMODORO_XP = 50;
    private StatsDAO statsDAO;

    public GamificationEngine() {
        this.statsDAO = new StatsDAO();
    }

    public void rewardPomodoroCompletion() {
        if (UserSession.getInstance().isLoggedIn()) {
            statsDAO.addXpAndUpdateStreak(POMODORO_XP);
        }
    }
    
    public UserStats getCurrentUserStats() {
        if (UserSession.getInstance().isLoggedIn()) {
            return statsDAO.getUserStats();
        }
        return null;
    }
}