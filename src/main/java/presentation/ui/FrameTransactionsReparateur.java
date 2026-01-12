package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.Transaction;
import dao.User;
import dao.enums.Role;
import exception.MetierException;
import metier.impl.ProprietaireImpl;
import metier.interfaces.IProprietaire;

public class FrameTransactionsReparateur extends JPanel {

    private static final long serialVersionUID = 1L;

    private final User proprietaire;
    private final IProprietaire metier = new ProprietaireImpl();

    private JComboBox<Boutique> cbBoutiques;
    private JButton btnChargerReps;

    private JComboBox<User> cbReparateurs;

    private JTextField txtDebut;
    private JTextField txtFin;
    private JButton btnAfficher;

    private JLabel lblSoldeTempsReel;
    private JLabel lblSoldeReparation;

    private JTable table;
    private DefaultTableModel model;

    public FrameTransactionsReparateur(User proprietaire) {
        this.proprietaire = proprietaire;

        setLayout(new BorderLayout());

        JLabel titre = new JLabel("Transactions d'un réparateur");
        titre.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== TOP =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Boutique :"));
        cbBoutiques = new JComboBox<>();
        top.add(cbBoutiques);

        btnChargerReps = new JButton("Charger réparateurs");
        top.add(btnChargerReps);

        top.add(new JLabel("Réparateur :"));
        cbReparateurs = new JComboBox<>();
        cbReparateurs.setPrototypeDisplayValue(User.builder().nom("XXXXXXXX").prenom("XXXXXXXX").build());
        top.add(cbReparateurs);

        top.add(new JLabel("Début (yyyy-MM-dd) :"));
        txtDebut = new JTextField(10);
        top.add(txtDebut);

        top.add(new JLabel("Fin (yyyy-MM-dd) :"));
        txtFin = new JTextField(10);
        top.add(txtFin);

        btnAfficher = new JButton("Afficher");
        top.add(btnAfficher);

        JPanel north = new JPanel(new BorderLayout());
        north.add(titre, BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // ===== CENTER =====
        model = new DefaultTableModel(new Object[]{
                "Date", "Montant", "Type Op", "Type Caisse", "Description", "Réparation"
        }, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblSoldeTempsReel = new JLabel("Solde Temps Réel : -");
        lblSoldeReparation = new JLabel("Solde Réparation : -");
        bottom.add(lblSoldeTempsReel);
        bottom.add(new JLabel("   |   "));
        bottom.add(lblSoldeReparation);

        add(bottom, BorderLayout.SOUTH);

        // events
        btnChargerReps.addActionListener(e -> chargerReparateurs());
        btnAfficher.addActionListener(e -> afficherTransactions());

        cbBoutiques.addActionListener(e -> chargerReparateurs()); // pratique : change boutique => recharge réparateurs

        // init
        chargerBoutiques();
        setDatesParDefaut();
        chargerReparateurs();
    }

    public void refresh() {
        Boutique selectedB = (Boutique) cbBoutiques.getSelectedItem();
        Long idB = (selectedB != null) ? selectedB.getId() : null;

        chargerBoutiques();
        reselectionnerBoutique(idB);
        chargerReparateurs();

        // reset affichage
        model.setRowCount(0);
        lblSoldeTempsReel.setText("Solde Temps Réel : -");
        lblSoldeReparation.setText("Solde Réparation : -");
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

    private void chargerReparateurs() {
        cbReparateurs.removeAllItems();
        Boutique b = (Boutique) cbBoutiques.getSelectedItem();
        if (b == null) return;

        List<User> reps = metier.listerReparateursParBoutique(b.getId());
        for (User u : reps) {
            if (u.getRole() == Role.REPARATEUR) cbReparateurs.addItem(u);
        }
        if (cbReparateurs.getItemCount() > 0) cbReparateurs.setSelectedIndex(0);
    }

    private void setDatesParDefaut() {
        LocalDate now = LocalDate.now();
        txtFin.setText(now.toString());
        txtDebut.setText(now.withDayOfMonth(1).toString());
    }

    private void afficherTransactions() {
        Boutique b = (Boutique) cbBoutiques.getSelectedItem();
        User rep = (User) cbReparateurs.getSelectedItem();

        if (b == null) {
            JOptionPane.showMessageDialog(this, "Aucune boutique.");
            return;
        }
        if (rep == null) {
            JOptionPane.showMessageDialog(this, "Aucun réparateur pour cette boutique.");
            return;
        }

        try {
            LocalDate d1 = LocalDate.parse(txtDebut.getText().trim());
            LocalDate d2 = LocalDate.parse(txtFin.getText().trim());

            LocalDateTime debut = d1.atStartOfDay();
            LocalDateTime fin = d2.atTime(23, 59, 59);

            // table
            model.setRowCount(0);
            List<Transaction> list = metier.listerTransactionsReparateur(rep.getId(), debut, fin);
            for (Transaction t : list) {
                String repInfo = (t.getReparation() != null) ? t.getReparation().getCodeSuivi() : "-";
                model.addRow(new Object[]{
                        t.getDate(),
                        t.getMontant(),
                        t.getTypeOperation(),
                        t.getTypeCaisse(),
                        t.getDescription(),
                        repInfo
                });
            }

            // soldes
            Double sTR = metier.soldeTempsReel(rep.getId(), debut, fin);
            Double sRep = metier.soldeReparation(rep.getId(), debut, fin);

            lblSoldeTempsReel.setText("Solde Temps Réel : " + sTR);
            lblSoldeReparation.setText("Solde Réparation : " + sRep);

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
