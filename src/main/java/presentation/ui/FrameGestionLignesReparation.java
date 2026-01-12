package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Appareil;
import dao.LigneReparation;
import dao.enums.EtatAppareil;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameGestionLignesReparation extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();
    private final Long idReparation;

    private JTable table;
    private DefaultTableModel model;

    private JComboBox<Appareil> cbAppareils;
    private JTextField txtCout;
    private JTextField txtCommentaire;
    private JComboBox<EtatAppareil> cbEtat;

    public FrameGestionLignesReparation(Long idReparation) {
        this.idReparation = idReparation;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Lignes réparation - Réparation ID : " + idReparation);
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "ID Ligne", "Appareil", "Coût", "État", "Commentaire"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== SOUTH =====
        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Appareil :"));
        cbAppareils = new JComboBox<>();
        cbAppareils.setPrototypeDisplayValue(new Appareil());
        cbAppareils.setPreferredSize(new java.awt.Dimension(260, 26));
        form.add(cbAppareils);

        form.add(new JLabel("Coût :"));
        txtCout = new JTextField(8);
        form.add(txtCout);

        form.add(new JLabel("Commentaire :"));
        txtCommentaire = new JTextField(18);
        form.add(txtCommentaire);

        form.add(new JLabel("État :"));
        cbEtat = new JComboBox<>(EtatAppareil.values());
        form.add(cbEtat);

        south.add(form, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier (coût/com)");
        JButton btnChangerEtat = new JButton("Changer état");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnRefresh = new JButton("Rafraîchir");
        JButton btnVider = new JButton("Vider");

        buttons.add(btnAjouter);
        buttons.add(btnModifier);
        buttons.add(btnChangerEtat);
        buttons.add(btnSupprimer);
        buttons.add(btnRefresh);
        buttons.add(btnVider);

        south.add(buttons, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // events
        btnRefresh.addActionListener(e -> refresh());
        btnVider.addActionListener(e -> clearForm());

        btnAjouter.addActionListener(e -> ajouter());
        btnModifier.addActionListener(e -> modifier());
        btnChangerEtat.addActionListener(e -> changerEtat());
        btnSupprimer.addActionListener(e -> supprimer());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        refreshAppareils();
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        List<LigneReparation> lignes = metier.listerLignesParReparation(idReparation);
        for (LigneReparation lr : lignes) {
            model.addRow(new Object[] {
                    lr.getId(),
                    lr.getAppareil() != null ? lr.getAppareil().toString() : "",
                    lr.getCoutAppareil(),
                    lr.getEtatAppareil() != null ? lr.getEtatAppareil().name() : "",
                    lr.getCommentaire()
            });
        }
        clearForm();
    }

    private void refreshAppareils() {
        cbAppareils.removeAllItems();
        List<Appareil> apps = metier.listerAppareils();
        for (Appareil a : apps) cbAppareils.addItem(a);
    }

    private Long getSelectedLigneId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Long) model.getValueAt(row, 0);
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        txtCout.setText(model.getValueAt(row, 2) != null ? String.valueOf(model.getValueAt(row, 2)) : "");
        txtCommentaire.setText(model.getValueAt(row, 4) != null ? String.valueOf(model.getValueAt(row, 4)) : "");

        String etat = model.getValueAt(row, 3) != null ? String.valueOf(model.getValueAt(row, 3)) : null;
        if (etat != null && !etat.isEmpty()) {
            try { cbEtat.setSelectedItem(EtatAppareil.valueOf(etat)); } catch (Exception ignored) {}
        }
    }

    private void clearForm() {
        txtCout.setText("");
        txtCommentaire.setText("");
        cbEtat.setSelectedItem(EtatAppareil.EN_COURS);
        table.clearSelection();
    }

    private void ajouter() {
        try {
            Appareil a = (Appareil) cbAppareils.getSelectedItem();
            if (a == null || a.getId() == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez un appareil.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double cout = parseDouble(txtCout.getText().trim(), "Coût");
            if (cout < 0) throw new MetierException("Coût invalide (>=0)");

            String commentaire = txtCommentaire.getText().trim();

            metier.ajouterLigne(idReparation, a.getId(), cout, commentaire);
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifier() {
        try {
            Long idLigne = getSelectedLigneId();
            if (idLigne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            double cout = parseDouble(txtCout.getText().trim(), "Coût");
            if (cout < 0) throw new MetierException("Coût invalide (>=0)");

            String commentaire = txtCommentaire.getText().trim();

            metier.modifierLigne(idLigne, cout, commentaire);
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changerEtat() {
        try {
            Long idLigne = getSelectedLigneId();
            if (idLigne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            EtatAppareil e = (EtatAppareil) cbEtat.getSelectedItem();
            if (e == null) throw new MetierException("État invalide");

            metier.changerEtatLigne(idLigne, e.name());
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer() {
        try {
            Long idLigne = getSelectedLigneId();
            if (idLigne == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer cette ligne ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            metier.supprimerLigne(idLigne);
            refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
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
