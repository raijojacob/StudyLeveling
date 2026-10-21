package models;

import java.util.Date;

public class UserStats {
    private int totalXp;
    private int currentStreak;
    private Date lastCompletedDate;

    public UserStats() {
        this.totalXp = 0;
        this.currentStreak = 0;
        this.lastCompletedDate = null;
    }

    public UserStats(int totalXp, int currentStreak, Date lastCompletedDate) {
        this.totalXp = totalXp;
        this.currentStreak = currentStreak;
        this.lastCompletedDate = lastCompletedDate;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Date getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(Date lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }
}