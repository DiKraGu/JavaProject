package presentation.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dao.User;

public class FrameProprietaire extends JFrame {

    private static final long serialVersionUID = 1L;

    private final User proprietaire;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private FrameGestionBoutiques panelBoutiques;
    private FrameGestionReparateurs panelReparateurs;
    private FrameProfit panelProfit;
    private FrameTransactionsReparateur panelTransactions;

    public FrameProprietaire(User proprietaire) {
        this.proprietaire = proprietaire;

        setTitle("Espace Propriétaire - " + proprietaire.getNom() + " " + proprietaire.getPrenom());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // ===== NAVBAR (boutons, pas menu condense) =====
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnBoutiques = new JButton("Boutiques");
        JButton btnReparateurs = new JButton("Réparateurs");
        JButton btnProfit = new JButton("Profit");
        JButton btnTransactions = new JButton("Transactions réparateur");
        JButton btnLogout = new JButton("Déconnexion");

        nav.add(btnBoutiques);
        nav.add(btnReparateurs);
        nav.add(btnProfit);
        nav.add(btnTransactions);

        // espace (push logout à droite)
        nav.add(Box.createHorizontalStrut(30));
        nav.add(btnLogout);

        add(nav, BorderLayout.NORTH);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        panelBoutiques = new FrameGestionBoutiques(proprietaire);
        panelReparateurs = new FrameGestionReparateurs(proprietaire);
        panelProfit = new FrameProfit(proprietaire);
        panelTransactions = new FrameTransactionsReparateur(proprietaire);

        contentPanel.add(panelBoutiques, "BOUTIQUES");
        contentPanel.add(panelReparateurs, "REPARATEURS");
        contentPanel.add(panelProfit, "PROFIT");
        contentPanel.add(panelTransactions, "TRANSACTIONS");

        add(contentPanel, BorderLayout.CENTER);

        // ===== ACTIONS NAVBAR =====
        btnBoutiques.addActionListener(e -> {
            // IMPORTANT : refresh pour voir les changements
            panelBoutiques.refresh();
            cardLayout.show(contentPanel, "BOUTIQUES");
        });

        btnReparateurs.addActionListener(e -> {
            // IMPORTANT : refresh pour voir les boutiques à jour dans la combo
            panelReparateurs.refresh();
            cardLayout.show(contentPanel, "REPARATEURS");
        });

        btnProfit.addActionListener(e -> {
            panelProfit.refresh();
            cardLayout.show(contentPanel, "PROFIT");
        });

        btnTransactions.addActionListener(e -> {
            panelTransactions.refresh();
            cardLayout.show(contentPanel, "TRANSACTIONS");
        });

        btnLogout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Se déconnecter ?", "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                FrameLogin login = new FrameLogin();
                login.setVisible(true);
                dispose();
            }
        });

        // écran par défaut
        panelBoutiques.refresh();
        cardLayout.show(contentPanel, "BOUTIQUES");
    }
}
