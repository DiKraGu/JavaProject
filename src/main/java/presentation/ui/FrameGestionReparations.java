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

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FrameGestionReparations(User reparateur) {
        this.reparateur = reparateur;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Gestion Réparations (CRUD + Statut)");
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
        JButton btnRefresh = new JButton("Rafraîchir");
        JButton btnModifierDesc = new JButton("Modifier desc");
        JButton btnChangerStatut = new JButton("Changer statut");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnOuvrirLignes = new JButton("Ouvrir lignes");

        buttons.add(btnRefresh);
        buttons.add(btnModifierDesc);
        buttons.add(btnChangerStatut);
        buttons.add(btnSupprimer);
        buttons.add(btnOuvrirLignes);

        south.add(buttons, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // ===== Events =====
        btnRefresh.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        btnModifierDesc.addActionListener(e -> modifierDesc());
        btnChangerStatut.addActionListener(e -> changerStatut());
        btnSupprimer.addActionListener(e -> supprimer());
        btnOuvrirLignes.addActionListener(e -> ouvrirLignesDialog());

        refresh();
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
        txtDesc.setText("");
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

    private void modifierDesc() {
        try {
            Long id = getSelectedReparationId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une réparation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String desc = txtDesc.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            metier.modifierReparation(id, desc);
            refresh();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur métier", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changerStatut() {
        try {
            Long id = getSelectedReparationId();
            if (id == null) {
                JOptionPane.showMessageDialog(this, "Sélectionnez une réparation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StatutReparation s = (StatutReparation) cbStatut.getSelectedItem();
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Statut invalide.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            metier.changerStatutReparation(id, s.name());
            refresh();

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
            refresh();

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

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Lignes de réparation", Dialog.ModalityType.APPLICATION_MODAL);
        FrameGestionLignesReparation panel = new FrameGestionLignesReparation(id);
        dialog.setContentPane(panel);
        dialog.setSize(1100, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // après fermeture -> refresh réparations (coût total peut changer)
        refresh();
    }
}
