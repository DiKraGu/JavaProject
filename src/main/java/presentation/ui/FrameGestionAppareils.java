package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Appareil;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameGestionAppareils extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtType;
    private JTextField txtMarque;
    private JTextField txtModele;
    private JTextField txtRam;
    private JTextField txtStockage;

    public FrameGestionAppareils() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Gestion Appareils (CRUD)");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "ID", "Type", "Marque", "Modèle", "RAM", "Stockage"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Type :"));
        txtType = new JTextField(10);
        form.add(txtType);

        form.add(new JLabel("Marque :"));
        txtMarque = new JTextField(10);
        form.add(txtMarque);

        form.add(new JLabel("Modèle :"));
        txtModele = new JTextField(10);
        form.add(txtModele);

        form.add(new JLabel("RAM :"));
        txtRam = new JTextField(8);
        form.add(txtRam);

        form.add(new JLabel("Stockage :"));
        txtStockage = new JTextField(8);
        form.add(txtStockage);

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
        List<Appareil> apps = metier.listerAppareils();
        for (Appareil a : apps) {
            model.addRow(new Object[] {
                    a.getId(),
                    a.getType(),
                    a.getMarque(),
                    a.getModele(),
                    a.getRam(),
                    a.getStockage()
            });
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        txtType.setText(String.valueOf(model.getValueAt(row, 1)));
        txtMarque.setText(String.valueOf(model.getValueAt(row, 2)));
        txtModele.setText(String.valueOf(model.getValueAt(row, 3)));
        txtRam.setText(model.getValueAt(row, 4) != null ? String.valueOf(model.getValueAt(row, 4)) : "");
        txtStockage.setText(model.getValueAt(row, 5) != null ? String.valueOf(model.getValueAt(row, 5)) : "");
    }

    private void ajouter() {
        try {
            Appareil a = Appareil.builder()
                    .type(txtType.getText().trim())
                    .marque(txtMarque.getText().trim())
                    .modele(txtModele.getText().trim())
                    .ram(txtRam.getText().trim())
                    .stockage(txtStockage.getText().trim())
                    .build();

            metier.ajouterAppareil(a);
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
                JOptionPane.showMessageDialog(this, "Sélectionnez un appareil.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Long id = (Long) model.getValueAt(row, 0);

            Appareil a = metier.rechercherAppareil(id);
            a.setType(txtType.getText().trim());
            a.setMarque(txtMarque.getText().trim());
            a.setModele(txtModele.getText().trim());
            a.setRam(txtRam.getText().trim());
            a.setStockage(txtStockage.getText().trim());

            metier.modifierAppareil(a);
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer() {
        try {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un appareil.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Long id = (Long) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Supprimer cet appareil ?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            metier.supprimerAppareil(id);
            refresh();
            clearForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtType.setText("");
        txtMarque.setText("");
        txtModele.setText("");
        txtRam.setText("");
        txtStockage.setText("");
        table.clearSelection();
    }
}
