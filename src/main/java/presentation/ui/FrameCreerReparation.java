package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

    public FrameCreerReparation(User reparateur, FrameReparateur parent) {
        this.reparateur = reparateur;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Créer réparation", SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout(10, 10));

        JPanel rowClient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowClient.add(new JLabel("Client :"));
        cbClients = new JComboBox<>();
        cbClients.setPreferredSize(new java.awt.Dimension(700, 26));
        rowClient.add(cbClients);

        JButton btnClients = new JButton("Gérer clients");
        rowClient.add(btnClients);

        top.add(rowClient, BorderLayout.NORTH);

        txtDescriptionPanne = new JTextArea(4, 20);
        top.add(new JScrollPane(txtDescriptionPanne), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel rowApp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowApp.add(new JLabel("Appareil :"));
        cbAppareils = new JComboBox<>();
        cbAppareils.setPreferredSize(new java.awt.Dimension(520, 26));
        rowApp.add(cbAppareils);

        JButton btnApps = new JButton("Gérer appareils");
        rowApp.add(btnApps);

        rowApp.add(new JLabel("Coût :"));
        txtCoutAppareil = new JTextField(8);
        rowApp.add(txtCoutAppareil);

        rowApp.add(new JLabel("Commentaire :"));
        txtCommentaireLigne = new JTextField(20);
        rowApp.add(txtCommentaireLigne);

        JButton btnAdd = new JButton("Ajouter ligne");
        JButton btnDel = new JButton("Supprimer ligne");
        rowApp.add(btnAdd);
        rowApp.add(btnDel);

        center.add(rowApp, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Appareil", "Coût", "État", "Commentaire", "id"},
                0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);

        center.add(new JScrollPane(table), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCreer = new JButton("Créer réparation");
        JButton btnReset = new JButton("Vider");
        left.add(btnCreer);
        left.add(btnReset);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(new JLabel("Coût total :"));
        txtCoutTotal = new JTextField("0.0", 10);
        txtCoutTotal.setEditable(false);
        right.add(txtCoutTotal);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ===== ACTIONS =====
        btnClients.addActionListener(e -> parent.showClients());
        btnApps.addActionListener(e -> parent.showAppareils());

        btnAdd.addActionListener(e -> ajouterLigne());
        btnDel.addActionListener(e -> supprimerLigne());

        btnCreer.addActionListener(e -> creerReparation());
        btnReset.addActionListener(e -> resetUI());

        refresh();
    }

    public void refresh() {
        cbClients.removeAllItems();
        for (Client c : metier.listerClients()) cbClients.addItem(c);

        cbAppareils.removeAllItems();
        for (Appareil a : metier.listerAppareils()) cbAppareils.addItem(a);

        resetUI();
    }

    private void ajouterLigne() {
        Appareil a = (Appareil) cbAppareils.getSelectedItem();
        if (a == null) return;

        double cout = Double.parseDouble(txtCoutAppareil.getText());
        model.addRow(new Object[]{a, cout, EtatAppareil.EN_COURS, txtCommentaireLigne.getText(), a.getId()});
        recalculerTotal();
        txtCoutAppareil.setText("");
        txtCommentaireLigne.setText("");
    }

    private void supprimerLigne() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            model.removeRow(r);
            recalculerTotal();
        }
    }

    private void recalculerTotal() {
        double t = 0;
        for (int i = 0; i < model.getRowCount(); i++)
            t += Double.parseDouble(model.getValueAt(i, 1).toString());
        txtCoutTotal.setText(String.valueOf(t));
    }

    private void creerReparation() {
        JOptionPane.showMessageDialog(this, "Réparation créée (OK)");
        resetUI();
    }

    private void resetUI() {
        txtDescriptionPanne.setText("");
        txtCoutTotal.setText("0.0");
        model.setRowCount(0);
    }
}
