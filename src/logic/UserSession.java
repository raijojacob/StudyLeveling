package logic;

public class UserSession {
    private static UserSession instance;
    private int loggedInUserId;
    private String loggedInUsername;

    // Private constructor prevents anyone else from creating a new instance
    private UserSession() {
        this.loggedInUserId = -1; // -1 means no one is logged in
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(int userId, String username) {
        this.loggedInUserId = userId;
        this.loggedInUsername = username;
    }

    public void logout() {
        this.loggedInUserId = -1;
        this.loggedInUsername = null;
    }

    public boolean isLoggedIn() {
        return loggedInUserId != -1;
    }

    public int getUserId() {
        return loggedInUserId;
    }

    public String getUsername() {
        return loggedInUsername;
    }
}