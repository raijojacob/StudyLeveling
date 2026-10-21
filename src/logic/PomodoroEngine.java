package logic;

import javax.swing.Timer;

public class PomodoroEngine {
    
    // Interface to let the engine talk back to the UI
    public interface PomodoroListener {
        void onTick(String formattedTime);
        void onTimerComplete();
        void onPauseStateChanged(int pausesRemaining);
    }

    private static final int SESSION_SECONDS = 25 * 60; // 25 minutes
    private static final int MAX_PAUSES = 3;

    private int secondsRemaining;
    private int pausesRemaining;
    private boolean isRunning;
    private Timer timer;
    private PomodoroListener listener;

    public PomodoroEngine(PomodoroListener listener) {
        this.listener = listener;
        resetEngine();

        // The timer triggers this block of code exactly once every 1000 milliseconds (1 second)
        timer = new Timer(1000, e -> tick());
    }

    private void tick() {
        if (secondsRemaining > 0) {
            secondsRemaining--;
            listener.onTick(getFormattedTime());
        } else {
            timer.stop();
            isRunning = false;
            listener.onTimerComplete();
        }
    }

    public void start() {
        if (!isRunning && secondsRemaining > 0) {
            timer.start();
            isRunning = true;
        }
    }

    public boolean pause() {
        if (isRunning && pausesRemaining > 0) {
            timer.stop();
            isRunning = false;
            pausesRemaining--;
            listener.onPauseStateChanged(pausesRemaining);
            return true;
        }
        return false; // Cannot pause (either already paused or out of pauses)
    }

    public void abandon() {
        timer.stop();
        resetEngine();
        listener.onTick(getFormattedTime());
        listener.onPauseStateChanged(pausesRemaining);
    }

    private void resetEngine() {
        secondsRemaining = SESSION_SECONDS;
        pausesRemaining = MAX_PAUSES;
        isRunning = false;
    }

    public String getFormattedTime() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    public int getPausesRemaining() {
        return pausesRemaining;
    }
}