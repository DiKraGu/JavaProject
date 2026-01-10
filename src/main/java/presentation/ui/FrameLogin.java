package presentation.ui;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import dao.User;
import exception.AuthException;
import metier.impl.AuthImpl;
import metier.interfaces.IAuth;

public class FrameLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextField txtEmail;
    private JPasswordField txtPassword;

    private IAuth authMetier = new AuthImpl();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FrameLogin frame = new FrameLogin();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public FrameLogin() {
        setTitle("Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Label Email
        JLabel lblEmail = new JLabel("Email :");
        lblEmail.setBounds(50, 60, 80, 25);
        contentPane.add(lblEmail);

        // Champ Email
        txtEmail = new JTextField();
        txtEmail.setBounds(150, 60, 220, 25);
        contentPane.add(txtEmail);
        txtEmail.setColumns(10);

        // Label Mot de passe
        JLabel lblMdp = new JLabel("Mot de passe :");
        lblMdp.setBounds(50, 110, 100, 25);
        contentPane.add(lblMdp);

        // Champ Mot de passe
        txtPassword = new JPasswordField();
        txtPassword.setBounds(150, 110, 220, 25);
        contentPane.add(txtPassword);

        // Bouton Login
        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setBounds(150, 170, 130, 30);
        contentPane.add(btnLogin);

        // Bouton Suivi client
        JButton btnSuivi = new JButton("Suivi client");
        btnSuivi.setBounds(290, 170, 120, 30);
        contentPane.add(btnSuivi);

        // Actions
        btnLogin.addActionListener(e -> seConnecter());

        btnSuivi.addActionListener(e -> {
            FrameSuiviClient frame = new FrameSuiviClient();
            frame.setVisible(true);
        });
    }

    /**
     * Méthode de connexion
     */
    private void seConnecter() {
        try {
            String email = txtEmail.getText().trim();
            String mdp = new String(txtPassword.getPassword());

            User user = authMetier.seConnecter(email, mdp);

            JOptionPane.showMessageDialog(this,
                    "Bienvenue " + user.getPrenom(),
                    "Connexion réussie",
                    JOptionPane.INFORMATION_MESSAGE);

            // Plus tard :
            // if (user.getRole() == Role.PROPRIETAIRE) { ... }
            // else { ... }

        } catch (AuthException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erreur d'authentification",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur système : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
