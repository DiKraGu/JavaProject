package presentation.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import dao.User;

public class FrameReparateur extends JFrame {

    private static final long serialVersionUID = 1L;

    private final User reparateur;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private FrameCreerReparation panelCreer;
    private FrameGestionReparations panelReparations;
    private FrameGestionClients panelClients;
    private FrameGestionAppareils panelAppareils;
    private FrameCaisse panelCaisse;

    public FrameReparateur(User reparateur) {
        this.reparateur = reparateur;

        setTitle("Espace Réparateur - " + reparateur.getNom() + " " + reparateur.getPrenom());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== BARRE DE MENU (boutons, pas condensés) =====
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JButton bCreer = new JButton("Créer réparation");
        JButton bGerer = new JButton("Gérer réparations");
        JButton bClients = new JButton("Clients");
        JButton bAppareils = new JButton("Appareils");
        JButton bCaisse = new JButton("Caisse");
        JButton bLogout = new JButton("Déconnexion");

        menu.add(bCreer);
        menu.add(bGerer);
        menu.add(bClients);
        menu.add(bAppareils);
        menu.add(bCaisse);
        menu.add(Box.createHorizontalStrut(40));
        menu.add(bLogout);

        add(menu, BorderLayout.NORTH);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        panelCreer = new FrameCreerReparation(reparateur, this);
        panelReparations = new FrameGestionReparations(reparateur);
        panelClients = new FrameGestionClients();
        panelAppareils = new FrameGestionAppareils();
        panelCaisse = new FrameCaisse(reparateur);

        contentPanel.add(panelCreer, "CREER");
        contentPanel.add(panelReparations, "REPARATIONS");
        contentPanel.add(panelClients, "CLIENTS");
        contentPanel.add(panelAppareils, "APPAREILS");
        contentPanel.add(panelCaisse, "CAISSE");

        add(contentPanel, BorderLayout.CENTER);

        // ===== ACTIONS =====
        bCreer.addActionListener(e -> showCreer());
        bGerer.addActionListener(e -> showReparations());
        bClients.addActionListener(e -> showClients());
        bAppareils.addActionListener(e -> showAppareils());
        bCaisse.addActionListener(e -> showCaisse());

        bLogout.addActionListener(e -> {
            new FrameLogin().setVisible(true);
            dispose();
        });

        // affichage initial
        showCreer();
    }

    // ===== Méthodes de navigation simples =====
    public void showCreer() {
        panelCreer.refresh();
        cardLayout.show(contentPanel, "CREER");
    }

    public void showReparations() {
        panelReparations.refresh();
        cardLayout.show(contentPanel, "REPARATIONS");
    }

    public void showClients() {
        panelClients.refresh();
        cardLayout.show(contentPanel, "CLIENTS");
    }

    public void showAppareils() {
        panelAppareils.refresh();
        cardLayout.show(contentPanel, "APPAREILS");
    }

    public void showCaisse() {
        panelCaisse.refresh();
        cardLayout.show(contentPanel, "CAISSE");
    }
}
