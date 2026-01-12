package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Client;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameGestionClients extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtTel;

    public FrameGestionClients() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Gestion Clients (CRUD)");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Nom", "Prénom", "Téléphone" }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nom :"));
        txtNom = new JTextField(14);
        form.add(txtNom);

        form.add(new JLabel("Prénom :"));
        txtPrenom = new JTextField(14);
        form.add(txtPrenom);

        form.add(new JLabel("Tel :"));
        txtTel = new JTextField(12);
        form.add(txtTel);

        south.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Ajouter");
        JButton btnUpdate = new JButton("Modifier");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnVider = new JButton("Vider");

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnVider);

        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        btnVider.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        btnAdd.addActionListener(e -> ajouter());
        btnUpdate.addActionListener(e -> modifier());
        btnDelete.addActionListener(e -> supprimer());

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        List<Client> clients = metier.listerClients();
        for (Client c : clients) {
            model.addRow(new Object[] { c.getId(), c.getNom(), c.getPrenom(), c.getTelephone() });
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        txtNom.setText(String.valueOf(model.getValueAt(row, 1)));
        txtPrenom.setText(String.valueOf(model.getValueAt(row, 2)));
        txtTel.setText(String.valueOf(model.getValueAt(row, 3)));
    }

    private void ajouter() {
        try {
            Client c = Client.builder()
                    .nom(txtNom.getText().trim())
                    .prenom(txtPrenom.getText().trim())
                    .telephone(txtTel.getText().trim())
                    .build();

            metier.ajouterClient(c);
            refresh();
            clearForm();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifier() {
        try {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un client.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Long id = (Long) model.getValueAt(row, 0);

            Client c = metier.rechercherClient(id);
            c.setNom(txtNom.getText().trim());
            c.setPrenom(txtPrenom.getText().trim());
            c.setTelephone(txtTel.getText().trim());

            metier.modifierClient(c);
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer() {
        try {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un client.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Long id = (Long) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer ce client ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            metier.supprimerClient(id);
            refresh();
            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtNom.setText("");
        txtPrenom.setText("");
        txtTel.setText("");
        table.clearSelection();
    }
}
