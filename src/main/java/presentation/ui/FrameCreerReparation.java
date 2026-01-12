package presentation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Appareil;
import dao.Client;
import dao.LigneReparation;
import dao.Reparation;
import dao.User;
import dao.enums.EtatAppareil;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameCreerReparation extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();
    private final User reparateur;
    private final FrameReparateur parent;

    private JComboBox<Client> cbClients;
    private JComboBox<Appareil> cbAppareils;

    private JTextArea txtDescriptionPanne;

    private JTextField txtCoutAppareil;
    private JTextField txtCommentaireLigne;
    private JTextField txtCoutTotal;

    private JTable table;
    private DefaultTableModel model;

    private JButton btnAjouterLigne;
    private JButton btnSupprimerLigne;

    private JButton btnCreer;
    private JButton btnVider;

    public FrameCreerReparation(User reparateur, FrameReparateur parent) {
        this.reparateur = reparateur;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Créer réparation", SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        add(main, BorderLayout.CENTER);

        // ================= TOP (Client + Description) =================
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // Row client
        JPanel rowClient = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowClient.add(new JLabel("Client :"));

        cbClients = new JComboBox<>();
        cbClients.setPreferredSize(new Dimension(900, 26));
        rowClient.add(cbClients);

        JButton btnAjouterClient = new JButton("Ajouter un client");
        rowClient.add(btnAjouterClient);

        top.add(rowClient);

        // Description label + area
        JPanel rowDescLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowDescLabel.add(new JLabel("Description panne :"));
        top.add(rowDescLabel);

        txtDescriptionPanne = new JTextArea(5, 40);
        JScrollPane spDesc = new JScrollPane(txtDescriptionPanne);
        spDesc.setPreferredSize(new Dimension(1100, 120));
        top.add(spDesc);

        main.add(top, BorderLayout.NORTH);

        // ================= CENTER (Appareil + Ligne + Table) =================
        JPanel center = new JPanel(new BorderLayout(10, 10));

        // Row appareil
        JPanel rowApp = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowApp.add(new JLabel("Appareil :"));

        cbAppareils = new JComboBox<>();
        cbAppareils.setPreferredSize(new Dimension(650, 26));
        rowApp.add(cbAppareils);

        JButton btnAjouterAppareil = new JButton("Ajouter une appareil");
        rowApp.add(btnAjouterAppareil);

        center.add(rowApp, BorderLayout.NORTH);

        // Row coût + commentaire + boutons (TOUT SUR LA MEME LIGNE) ✅
        JPanel rowLine = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 10);
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Coût label
        c.gridx = 0;
        c.weightx = 0;
        rowLine.add(new JLabel("Coût appareil :"), c);

        // Coût field
        txtCoutAppareil = new JTextField();
        txtCoutAppareil.setPreferredSize(new Dimension(120, 26));
        c.gridx = 1;
        c.weightx = 0;
        rowLine.add(txtCoutAppareil, c);

        // Commentaire label
        c.gridx = 2;
        c.weightx = 0;
        rowLine.add(new JLabel("Commentaire (ligne) :"), c);

        // Commentaire field (IMPORTANT: ne pas le laisser énorme sinon les boutons sortent)
        txtCommentaireLigne = new JTextField();
        txtCommentaireLigne.setPreferredSize(new Dimension(420, 26)); // ajuste si besoin
        c.gridx = 3;
        c.weightx = 1.0; // prend l'espace restant
        rowLine.add(txtCommentaireLigne, c);

        // Bouton ajouter
        btnAjouterLigne = new JButton("Ajouter ligne");
        c.gridx = 4;
        c.weightx = 0;
        rowLine.add(btnAjouterLigne, c);

        // Bouton supprimer
        btnSupprimerLigne = new JButton("Supprimer ligne");
        c.gridx = 5;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        rowLine.add(btnSupprimerLigne, c);

        // On met cette ligne juste sous la ligne appareil
        JPanel northCenter = new JPanel();
        northCenter.setLayout(new BoxLayout(northCenter, BoxLayout.Y_AXIS));
        northCenter.add(rowApp);
        northCenter.add(Box.createVerticalStrut(6));
        northCenter.add(rowLine);

        center.add(northCenter, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "Appareil", "Coût", "État", "Commentaire", "idAppareil"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        // cacher la colonne id
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setWidth(0);

        center.add(new JScrollPane(table), BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);

        // ================= BOTTOM (Créer/Vider + Total) =================
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnCreer = new JButton("Créer réparation");
        btnVider = new JButton("Vider");
        left.add(btnCreer);
        left.add(btnVider);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.add(new JLabel("Coût total :"));
        txtCoutTotal = new JTextField("0.0", 10);
        txtCoutTotal.setEditable(false);
        right.add(txtCoutTotal);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ================= ACTIONS =================
        btnAjouterClient.addActionListener(e -> parent.showClients());
        btnAjouterAppareil.addActionListener(e -> parent.showAppareils());

        btnAjouterLigne.addActionListener(e -> ajouterLigneUI());
        btnSupprimerLigne.addActionListener(e -> supprimerLigneUI());

        btnCreer.addActionListener(e -> creerReparation());
        btnVider.addActionListener(e -> resetUI());

        refresh();
    }

    public void refresh() {
        refreshClients();
        refreshAppareils();
        resetUI();
    }

    private void refreshClients() {
        cbClients.removeAllItems();
        for (Client c : metier.listerClients()) cbClients.addItem(c);
        if (cbClients.getItemCount() > 0) cbClients.setSelectedIndex(0);
    }

    private void refreshAppareils() {
        cbAppareils.removeAllItems();
        for (Appareil a : metier.listerAppareils()) cbAppareils.addItem(a);
        if (cbAppareils.getItemCount() > 0) cbAppareils.setSelectedIndex(0);
    }

    private void ajouterLigneUI() {
        try {
            Appareil a = (Appareil) cbAppareils.getSelectedItem();
            if (a == null || a.getId() == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un appareil.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double cout = parseDouble(txtCoutAppareil.getText().trim(), "Coût appareil");
            if (cout < 0) {
                JOptionPane.showMessageDialog(this, "Le coût doit être >= 0", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String commentaire = txtCommentaireLigne.getText().trim();

            model.addRow(new Object[] {
                    a.toString(),
                    cout,
                    EtatAppareil.EN_COURS.name(),
                    commentaire,
                    a.getId()
            });

            txtCoutAppareil.setText("");
            txtCommentaireLigne.setText("");
            recalculerTotalUI();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerLigneUI() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        model.removeRow(row);
        recalculerTotalUI();
    }

    private void recalculerTotalUI() {
        double total = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 1);
            if (v != null) total += Double.parseDouble(String.valueOf(v));
        }
        txtCoutTotal.setText(String.valueOf(total));
    }

    private void creerReparation() {
        try {
            if (reparateur == null || reparateur.getId() == null) {
                JOptionPane.showMessageDialog(this, "Réparateur non valide (login).", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (reparateur.getBoutique() == null || reparateur.getBoutique().getId() == null) {
                JOptionPane.showMessageDialog(this, "Le réparateur n'est pas rattaché à une boutique.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Client client = (Client) cbClients.getSelectedItem();
            if (client == null || client.getId() == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un client.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String desc = txtDescriptionPanne.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description panne obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Ajoutez au moins une ligne (appareil).", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<LigneReparation> lignes = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Long idAppareil = (Long) model.getValueAt(i, 4);
                Double cout = Double.valueOf(String.valueOf(model.getValueAt(i, 1)));
                String etat = String.valueOf(model.getValueAt(i, 2));
                String com = String.valueOf(model.getValueAt(i, 3));

                Appareil appRef = Appareil.builder().id(idAppareil).build();

                LigneReparation lr = LigneReparation.builder()
                        .appareil(appRef)
                        .coutAppareil(cout)
                        .etatAppareil(EtatAppareil.valueOf(etat))
                        .commentaire(com != null && "null".equals(com) ? null : com)
                        .build();

                lignes.add(lr);
            }

            Reparation r = metier.creerReparation(
                    reparateur.getId(),
                    client.getId(),
                    reparateur.getBoutique().getId(),
                    desc,
                    lignes
            );

            JOptionPane.showMessageDialog(this,
                    "Réparation créée.\nCode suivi : " + r.getCodeSuivi() + "\nCoût total : " + r.getCoutTotal(),
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetUI();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetUI() {
        txtDescriptionPanne.setText("");
        txtCoutAppareil.setText("");
        txtCommentaireLigne.setText("");
        model.setRowCount(0);
        txtCoutTotal.setText("0.0");
        table.clearSelection();
    }

    private double parseDouble(String s, String fieldName) {
        if (s == null || s.isEmpty()) throw new MetierException(fieldName + " obligatoire");
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            throw new MetierException(fieldName + " invalide (nombre attendu)");
        }
    }
}
