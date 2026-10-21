package models;

import java.time.LocalDate;

public class UserStats {
    private int userId;
    private int totalXp;
    private int currentStreak;
    private LocalDate lastCompletedDate;

    public UserStats(int userId, int totalXp, int currentStreak, LocalDate lastCompletedDate) {
        this.userId = userId;
        this.totalXp = totalXp;
        this.currentStreak = currentStreak;
        this.lastCompletedDate = lastCompletedDate;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public LocalDate getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(LocalDate lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }
}