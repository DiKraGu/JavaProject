package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.User;
import exception.MetierException;
import metier.impl.ProprietaireImpl;
import metier.interfaces.IProprietaire;

public class FrameProfit extends JPanel {

    private static final long serialVersionUID = 1L;

    private final User proprietaire;
    private final IProprietaire metier = new ProprietaireImpl();

    private JComboBox<Boutique> cbBoutiques;
    private JTextField txtDebut; // yyyy-MM-dd
    private JTextField txtFin;   // yyyy-MM-dd
    private JButton btnCalculer;

    private JLabel lblProfitBoutique;

    private JTable table;
    private DefaultTableModel model;

    public FrameProfit(User proprietaire) {
        this.proprietaire = proprietaire;
        setLayout(new BorderLayout());

        // ===== TOP =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Boutique :"));
        cbBoutiques = new JComboBox<>();
        top.add(cbBoutiques);

        top.add(new JLabel("Début (yyyy-MM-dd) :"));
        txtDebut = new JTextField(10);
        top.add(txtDebut);

        top.add(new JLabel("Fin (yyyy-MM-dd) :"));
        txtFin = new JTextField(10);
        top.add(txtFin);

        btnCalculer = new JButton("Calculer");
        top.add(btnCalculer);

        JPanel north = new JPanel(new BorderLayout());
       // north.add(titre, BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel center = new JPanel(new BorderLayout());

        lblProfitBoutique = new JLabel("Profit boutique : -");
        lblProfitBoutique.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(lblProfitBoutique, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "IdRep", "Nom", "Prénom", "%", "CA", "Profit propriétaire"
        }, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // events
        btnCalculer.addActionListener(e -> calculer());

        // init
        chargerBoutiques();
        setDatesParDefaut();
    }

    public void refresh() {
        Boutique selected = (Boutique) cbBoutiques.getSelectedItem();
        Long id = (selected != null) ? selected.getId() : null;

        chargerBoutiques();
        reselectionnerBoutique(id);
        // optionnel: ne recalcule pas automatiquement, laisse l’utilisateur cliquer "Calculer"
        // model.setRowCount(0);
        // lblProfitBoutique.setText("Profit boutique : -");
    }

    private void reselectionnerBoutique(Long id) {
        if (id == null) return;
        for (int i = 0; i < cbBoutiques.getItemCount(); i++) {
            Boutique b = cbBoutiques.getItemAt(i);
            if (b != null && id.equals(b.getId())) {
                cbBoutiques.setSelectedIndex(i);
                return;
            }
        }
    }

    private void chargerBoutiques() {
        cbBoutiques.removeAllItems();
        List<Boutique> bs = metier.listerBoutiquesDuProprietaire(proprietaire.getId());
        for (Boutique b : bs) cbBoutiques.addItem(b);
        if (cbBoutiques.getItemCount() > 0) cbBoutiques.setSelectedIndex(0);
    }

    private void setDatesParDefaut() {
        LocalDate now = LocalDate.now();
        txtFin.setText(now.toString());
        txtDebut.setText(now.withDayOfMonth(1).toString());
    }

    private void calculer() {
        Boutique b = (Boutique) cbBoutiques.getSelectedItem();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Aucune boutique. Crée une boutique d'abord.");
            return;
        }

        try {
            LocalDate d1 = LocalDate.parse(txtDebut.getText().trim());
            LocalDate d2 = LocalDate.parse(txtFin.getText().trim());

            LocalDateTime debut = d1.atStartOfDay();
            LocalDateTime fin = d2.atTime(23, 59, 59);

            Double profitBoutique = metier.profitProprietaireParBoutique(b.getId(), debut, fin);
            lblProfitBoutique.setText("Profit boutique (propriétaire) : " + profitBoutique);

            model.setRowCount(0);
            List<Object[]> rows = metier.profitProprietaireParReparateur(b.getId(), debut, fin);
            for (Object[] r : rows) {
                model.addRow(new Object[]{ r[0], r[1], r[2], r[3], r[4], r[5] });
            }

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date invalide. Format attendu: yyyy-MM-dd",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
