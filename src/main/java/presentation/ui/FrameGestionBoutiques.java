package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.User;
import exception.MetierException;
import metier.impl.ProprietaireImpl;
import metier.interfaces.IProprietaire;

public class FrameGestionBoutiques extends JPanel {

    private static final long serialVersionUID = 1L;

    private final User proprietaire;
    private final IProprietaire metier = new ProprietaireImpl();

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtPatente;

    private JButton btnAjouter;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private JButton btnRafraichir;

    public FrameGestionBoutiques(User proprietaire) {
        this.proprietaire = proprietaire;
        setLayout(new BorderLayout());

        JLabel titre = new JLabel("Gestion des Boutiques");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[]{"ID", "Nom", "Adresse", "Patente"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM =====
        JPanel south = new JPanel(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l1.add(new JLabel("Nom :"));
        txtNom = new JTextField(20);
        l1.add(txtNom);

        JPanel l2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l2.add(new JLabel("Adresse :"));
        txtAdresse = new JTextField(30);
        l2.add(txtAdresse);

        JPanel l3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l3.add(new JLabel("Patente :"));
        txtPatente = new JTextField(20);
        l3.add(txtPatente);

        form.add(l1);
        form.add(l2);
        form.add(l3);

        south.add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnRafraichir = new JButton("Rafraîchir");

        buttons.add(btnAjouter);
        buttons.add(btnModifier);
        buttons.add(btnSupprimer);
        buttons.add(btnRafraichir);

        south.add(buttons, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // ===== Events =====
        btnRafraichir.addActionListener(e -> refresh());
        btnAjouter.addActionListener(e -> ajouterBoutique());
        btnModifier.addActionListener(e -> modifierBoutique());
        btnSupprimer.addActionListener(e -> supprimerBoutique());

        table.getSelectionModel().addListSelectionListener(e -> remplirFormDepuisTable());

        // Init
        chargerBoutiques();
    }

    /**
     * IMPORTANT: appelée par FrameProprietaire quand on ouvre cet écran.
     */
    public void refresh() {
        // on garde l'id sélectionné si possible
        Long selectedId = getSelectedBoutiqueId();
        chargerBoutiques();
        reselectionnerBoutique(selectedId);
    }

    private Long getSelectedBoutiqueId() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        Object v = model.getValueAt(row, 0);
        return (v instanceof Long) ? (Long) v : null;
    }

    private void reselectionnerBoutique(Long id) {
        if (id == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 0);
            if (v instanceof Long && id.equals(v)) {
                table.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void chargerBoutiques() {
        model.setRowCount(0);
        List<Boutique> list = metier.listerBoutiquesDuProprietaire(proprietaire.getId());
        for (Boutique b : list) {
            model.addRow(new Object[]{b.getId(), b.getNom(), b.getAdresse(), b.getNumeroPatente()});
        }
    }

    private void remplirFormDepuisTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        txtNom.setText(String.valueOf(model.getValueAt(row, 1)));
        txtAdresse.setText(String.valueOf(model.getValueAt(row, 2)));
        txtPatente.setText(String.valueOf(model.getValueAt(row, 3)));
    }

    private void ajouterBoutique() {
        try {
            String nom = txtNom.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String patente = txtPatente.getText().trim();

            Boutique b = Boutique.builder()
                    .nom(nom)
                    .adresse(adresse)
                    .numeroPatente(patente)
                    .proprietaire(proprietaire) // important
                    .build();

            metier.creerBoutique(b);

            JOptionPane.showMessageDialog(this, "Boutique ajoutée !");
            viderForm();
            refresh();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierBoutique() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne une boutique dans la table.");
            return;
        }

        try {
            Long id = (Long) model.getValueAt(row, 0);

            Boutique b = Boutique.builder()
                    .id(id)
                    .nom(txtNom.getText().trim())
                    .adresse(txtAdresse.getText().trim())
                    .numeroPatente(txtPatente.getText().trim())
                    .build();

            metier.modifierBoutique(b);

            JOptionPane.showMessageDialog(this, "Boutique modifiée !");
            viderForm();
            refresh();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerBoutique() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne une boutique dans la table.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Supprimer cette boutique ?", "Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            Long id = (Long) model.getValueAt(row, 0);
            metier.supprimerBoutique(id);

            JOptionPane.showMessageDialog(this, "Boutique supprimée !");
            viderForm();
            refresh();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viderForm() {
        txtNom.setText("");
        txtAdresse.setText("");
        txtPatente.setText("");
        table.clearSelection();
    }
}
