package presentation.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Reparation;
import dao.User;
import dao.enums.StatutReparation;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameGestionReparations extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();
    private final User reparateur;

    private JTable table;
    private DefaultTableModel model;

    private JTextArea txtDesc;
    private JComboBox<StatutReparation> cbStatut;

    private JButton btnEnregistrer;   // 1 seul bouton pour desc + statut
    private JButton btnSupprimer;
    private JButton btnOuvrirLignes;
    private JButton btnVider;         // remplace "Rafraîchir"

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FrameGestionReparations(User reparateur) {
        this.reparateur = reparateur;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Gestion Réparations");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "ID", "Code", "Client", "Statut", "Date", "Coût total"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM + BUTTONS =====
        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.add(new JLabel("Description panne :"), BorderLayout.NORTH);
        txtDesc = new JTextArea(4, 20);
        left.add(new JScrollPane(txtDesc), BorderLayout.CENTER);
        form.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT));
        right.add(new JLabel("Statut :"));
        cbStatut = new JComboBox<>(StatutReparation.values());
        right.add(cbStatut);
        form.add(right, BorderLayout.EAST);

        south.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnEnregistrer = new JButton("Modifier");
        btnSupprimer = new JButton("Supprimer");
        btnOuvrirLignes = new JButton("Voir details");
        btnVider = new JButton("Vider");

        buttons.add(btnEnregistrer);
        buttons.add(btnSupprimer);
        buttons.add(btnOuvrirLignes);
        buttons.add(btnVider);

        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // ===== Events =====
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        btnEnregistrer.addActionListener(e -> enregistrerDescEtStatut());
        btnSupprimer.addActionListener(e -> supprimer());
        btnOuvrirLignes.addActionListener(e -> ouvrirLignesDialog());
        btnVider.addActionListener(e -> viderForm());

        // init
        refresh();
        viderForm();
    }

    public void refresh() {
        if (reparateur == null || reparateur.getId() == null) return;

        model.setRowCount(0);
        List<Reparation> reps = metier.listerReparationsParReparateur(reparateur.getId());
        for (Reparation r : reps) {
            model.addRow(new Object[] {
                    r.getId(),
                    r.getCodeSuivi(),
                    (r.getClient() != null ? (r.getClient().getNom() + " " + r.getClient().getPrenom()) : ""),
                    r.getStatut() != null ? r.getStatut().name() : "",
                    r.getDateCreation() != null ? r.getDateCreation().format(fmt) : "",
                    r.getCoutTotal()
            });
        }
    }

    private Long getSelectedReparationId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Long) model.getValueAt(row, 0);
    }

    private void fillForm() {
        try {
            Long id = getSelectedReparationId();
            if (id == null) return;

            Reparation r = metier.rechercherReparation(id);

            txtDesc.setText(r.getDescriptionPanne() != null ? r.getDescriptionPanne() : "");
            if (r.getStatut() != null) cbStatut.setSelectedItem(r.getStatut());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 1 seul bouton: met à jour description + statut
     * MAIS: n'appelle pas le métier si rien n'a changé.
     */
    private void enregistrerDescEtStatut() {
        try {
            Long id = getSelectedReparationId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une réparation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String newDesc = txtDesc.getText().trim();
            StatutReparation newStatut = (StatutReparation) cbStatut.getSelectedItem();

            if (newDesc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newStatut == null) {
                JOptionPane.showMessageDialog(this, "Statut invalide.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Charger l'état actuel depuis la BD
            Reparation r = metier.rechercherReparation(id);

            String oldDesc = r.getDescriptionPanne() != null ? r.getDescriptionPanne() : "";
            StatutReparation oldStatut = r.getStatut();

            boolean descChanged = !oldDesc.equals(newDesc);
            boolean statutChanged = oldStatut != newStatut;

            if (!descChanged && !statutChanged) {
                JOptionPane.showMessageDialog(this, "Aucune modification détectée.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Appliquer uniquement ce qui a changé
            if (descChanged) {
                metier.modifierReparation(id, newDesc);
            }
            if (statutChanged) {
                metier.changerStatutReparation(id, newStatut.name());
            }

            JOptionPane.showMessageDialog(this, "Modification enregistrée.", "Succès", JOptionPane.INFORMATION_MESSAGE);

            // refresh table + garder sélection si possible
            refresh();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer() {
        try {
            Long id = getSelectedReparationId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une réparation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer cette réparation ? (les lignes seront supprimées aussi)",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            metier.supprimerReparation(id);

            JOptionPane.showMessageDialog(this, "Réparation supprimée.", "Succès", JOptionPane.INFORMATION_MESSAGE);

            refresh();
            viderForm();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ouvrirLignesDialog() {
        Long id = getSelectedReparationId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez une réparation.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Lignes de réparation",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        FrameGestionLignesReparation panel = new FrameGestionLignesReparation(id);
        dialog.setContentPane(panel);
        dialog.setSize(1100, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Après fermeture -> refresh réparations (coût total peut changer)
        refresh();
    }

    private void viderForm() {
        txtDesc.setText("");
        cbStatut.setSelectedIndex(0);
        table.clearSelection();
    }
}
