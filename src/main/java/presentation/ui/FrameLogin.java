package presentation.ui;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.User;
import dao.enums.Role;
import exception.AuthException;
import metier.impl.AuthImpl;
import metier.interfaces.IAuth;

public class FrameLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextField txtEmail;
    private JPasswordField txtPwd;

    private final IAuth auth = new AuthImpl();

    public FrameLogin() {
        setTitle("Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 520, 320);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblTitre = new JLabel("Connexion");
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitre.setBounds(10, 10, 484, 25);
        contentPane.add(lblTitre);

        JLabel lblEmail = new JLabel("Email :");
        lblEmail.setBounds(30, 60, 80, 20);
        contentPane.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(120, 58, 330, 26);
        contentPane.add(txtEmail);

        JLabel lblPwd = new JLabel("Mot de passe :");
        lblPwd.setBounds(30, 105, 100, 20);
        contentPane.add(lblPwd);

        txtPwd = new JPasswordField();
        txtPwd.setBounds(120, 103, 330, 26);
        contentPane.add(txtPwd);

        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setBounds(120, 155, 160, 32);
        contentPane.add(btnLogin);

        JButton btnQuit = new JButton("Quitter");
        btnQuit.setBounds(290, 155, 160, 32);
        contentPane.add(btnQuit);

        // Accès client sans login (suivi par code)
        JButton btnSuiviClient = new JButton("Suivi client (code)");
        btnSuiviClient.setBounds(120, 205, 330, 32);
        contentPane.add(btnSuiviClient);

        btnQuit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> seConnecter());
        btnSuiviClient.addActionListener(e -> ouvrirSuiviClient());

        getRootPane().setDefaultButton(btnLogin);
    }

    private void ouvrirSuiviClient() {
        try {
            FrameSuiviClient f = new FrameSuiviClient();
            f.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur UI: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seConnecter() {
        try {
            String email = txtEmail.getText().trim();
            String pwd = new String(txtPwd.getPassword());

            if (email.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email et mot de passe obligatoires", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            User u = auth.seConnecter(email, pwd);

            // ROUTING SELON ROLE
            if (u.getRole() == null) {
                JOptionPane.showMessageDialog(this, "Rôle utilisateur non défini", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (u.getRole() == Role.REPARATEUR) {
                FrameReparateur fr = new FrameReparateur(u);
                fr.setVisible(true);
                dispose();
                return;
            }

            if (u.getRole() == Role.PROPRIETAIRE) {
                FrameProprietaire fp = new FrameProprietaire(u);
                fp.setVisible(true);
                dispose();
                return;
            }

            // Si tu ajoutes d'autres rôles plus tard
            JOptionPane.showMessageDialog(this,
                    "Rôle non pris en charge dans l'UI : " + u.getRole(),
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Optionnel : main de test
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            FrameLogin f = new FrameLogin();
            f.setVisible(true);
        });
    }
}
