package presentation.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout; // Import nécessaire pour centrer

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dao.User;
import dao.enums.Role;
import exception.AuthException;
import metier.impl.AuthImpl;
import metier.interfaces.IAuth;

public class FrameLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;      // Le fond (plein écran)
    private JPanel panelForm;        // Le petit formulaire (centré)
    private JTextField txtEmail;
    private JPasswordField txtPwd;

    private final IAuth auth = new AuthImpl();

    public FrameLogin() {
        setTitle("Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. COMMANDE POUR LE PLEIN ÉCRAN
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        
        // Optionnel : Enlever la barre de titre (croix, réduire) pour un effet total
        // setUndecorated(true); 

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        
        // 2. ASTUCE POUR CENTRER : On utilise GridBagLayout sur le fond
        contentPane.setLayout(new GridBagLayout()); 
        contentPane.setBackground(new Color(240, 240, 240)); // Gris clair pour le fond

        // 3. CRÉATION DU PANNEAU FORMULAIRE (C'est lui qui contient vos champs)
        panelForm = new JPanel();
        panelForm.setLayout(null); // On garde votre logique de positionnement absolu
        panelForm.setPreferredSize(new Dimension(520, 320)); // Taille fixe du formulaire
        panelForm.setBackground(Color.WHITE); // Fond blanc pour le formulaire
        panelForm.setBorder(new LineBorder(new Color(200, 200, 200), 1, true)); // Petite bordure esthétique
        
        // On ajoute le panneau formulaire au fond (il sera centré automatiquement grâce au GridBagLayout)
        contentPane.add(panelForm);

        // --- À partir d'ici, on ajoute les composants à 'panelForm' au lieu de 'contentPane' ---

        JLabel lblTitre = new JLabel("Connexion");
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitre.setBounds(10, 10, 484, 25);
        panelForm.add(lblTitre); // Changé contentPane -> panelForm

        JLabel lblEmail = new JLabel("Email :");
        lblEmail.setBounds(30, 60, 80, 20);
        panelForm.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(120, 58, 330, 26);
        panelForm.add(txtEmail);

        JLabel lblPwd = new JLabel("Mot de passe :");
        lblPwd.setBounds(30, 105, 100, 20);
        panelForm.add(lblPwd);

        txtPwd = new JPasswordField();
        txtPwd.setBounds(120, 103, 330, 26);
        panelForm.add(txtPwd);

        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setBounds(120, 155, 160, 32);
        panelForm.add(btnLogin);

        JButton btnQuit = new JButton("Quitter");
        btnQuit.setBounds(290, 155, 160, 32);
        panelForm.add(btnQuit);

        // Accès client sans login (suivi par code)
        JButton btnSuiviClient = new JButton("Suivi client (code)");
        btnSuiviClient.setBounds(120, 205, 330, 32);
        panelForm.add(btnSuiviClient);

        btnQuit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> seConnecter());
        btnSuiviClient.addActionListener(e -> ouvrirSuiviClient());

        getRootPane().setDefaultButton(btnLogin);
    }

    // ... Le reste de vos méthodes (ouvrirSuiviClient, seConnecter, main) reste identique ...
    
    private void ouvrirSuiviClient() {
        try {
            // Remarque : FrameSuiviClient devra peut-être aussi être mis en plein écran si vous voulez
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

            if (u.getRole() == null) {
                JOptionPane.showMessageDialog(this, "Rôle utilisateur non défini", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (u.getRole() == Role.REPARATEUR) {
                FrameReparateur fr = new FrameReparateur(u);
                fr.setExtendedState(JFrame.MAXIMIZED_BOTH); // <-- Astuce : ouvrir la suivante en plein écran aussi
                fr.setVisible(true);
                dispose();
                return;
            }

            if (u.getRole() == Role.PROPRIETAIRE) {
                FrameProprietaire fp = new FrameProprietaire(u);
                fp.setExtendedState(JFrame.MAXIMIZED_BOTH); // <-- Pareil ici
                fp.setVisible(true);
                dispose();
                return;
            }

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

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            FrameLogin f = new FrameLogin();
            f.setVisible(true);
        });
    }
}