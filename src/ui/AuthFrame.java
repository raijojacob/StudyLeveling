package ui;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public AuthFrame() {
        setTitle("StudyLeveling - Login");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers window on screen
        setResizable(false);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Add the views to the CardLayout
        mainContainer.add(new LoginPanel(this), "LOGIN");
        mainContainer.add(new SignUpPanel(this), "SIGNUP");

        add(mainContainer);
    }

    // Methods called by the panels to switch views
    public void showSignUp() {
        setTitle("StudyLeveling - Register");
        cardLayout.show(mainContainer, "SIGNUP");
    }

    public void showLogin() {
        setTitle("StudyLeveling - Login");
        cardLayout.show(mainContainer, "LOGIN");
    }

    public void onLoginSuccess() {
        this.dispose(); // Close this authentication window
        System.out.println("Success! Handoff to MainFrame...");
        
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}