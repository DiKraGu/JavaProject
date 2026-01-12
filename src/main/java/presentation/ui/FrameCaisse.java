package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.*;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.Reparation;
import dao.Transaction;
import dao.User;
import dao.enums.TypeOperation;
import exception.MetierException;
import metier.impl.ReparateurImpl;
import metier.interfaces.IReparateur;

public class FrameCaisse extends JPanel {

    private static final long serialVersionUID = 1L;

    private final IReparateur metier = new ReparateurImpl();
    private final User reparateur;

    private JTable table;
    private DefaultTableModel model;

    private JSpinner spDebut;
    private JSpinner spFin;

    private JComboBox<TypeOperation> cbOp;
    private JComboBox<Reparation> cbReparation;

    private JTextField txtMontant;
    private JTextField txtDescription;

    private JTextField txtTotalEntrees;
    private JTextField txtTotalSorties;
    private JTextField txtSolde;

    public FrameCaisse(User reparateur) {
        this.reparateur = reparateur;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Caisse / Transactions");
        titre.setHorizontalAlignment(SwingConstants.CENTER);
        add(titre, BorderLayout.NORTH);

        // ===== TOP filtres =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        top.add(new JLabel("Début :"));
        spDebut = new JSpinner(new SpinnerDateModel());
        spDebut.setEditor(new JSpinner.DateEditor(spDebut, "yyyy-MM-dd HH:mm"));
        top.add(spDebut);

        top.add(new JLabel("Fin :"));
        spFin = new JSpinner(new SpinnerDateModel());
        spFin.setEditor(new JSpinner.DateEditor(spFin, "yyyy-MM-dd HH:mm"));
        top.add(spFin);

        JButton btnActualiser = new JButton("Actualiser");
        top.add(btnActualiser);

        add(top, BorderLayout.NORTH);

        // init dates
        LocalDateTime now = LocalDateTime.now();
        spDebut.setValue(toDate(now.withHour(0).withMinute(0)));
        spFin.setValue(toDate(now));

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "ID", "Date", "Montant", "Opération", "Réparation", "Description"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== SOUTH (ajout + totaux) =====
        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel addTx = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addTx.add(new JLabel("Montant :"));
        txtMontant = new JTextField(8);
        addTx.add(txtMontant);

        addTx.add(new JLabel("Opération :"));
        cbOp = new JComboBox<>(TypeOperation.values());
        addTx.add(cbOp);

        addTx.add(new JLabel("Réparation :"));
        cbReparation = new JComboBox<>();
        cbReparation.setPrototypeDisplayValue(new Reparation());
        cbReparation.setPreferredSize(new java.awt.Dimension(220, 26));
        addTx.add(cbReparation);

        addTx.add(new JLabel("Description :"));
        txtDescription = new JTextField(18);
        addTx.add(txtDescription);

        JButton btnAjouter = new JButton("Ajouter transaction");
        addTx.add(btnAjouter);

        JButton btnRefreshReps = new JButton("Actualiser réparations");
        addTx.add(btnRefreshReps);

        south.add(addTx, BorderLayout.NORTH);

        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT));

        totals.add(new JLabel("Total entrées :"));
        txtTotalEntrees = new JTextField(10);
        txtTotalEntrees.setEditable(false);
        totals.add(txtTotalEntrees);

        totals.add(new JLabel("   |   "));

        totals.add(new JLabel("Total sorties :"));
        txtTotalSorties = new JTextField(10);
        txtTotalSorties.setEditable(false);
        totals.add(txtTotalSorties);

        totals.add(new JLabel("   |   "));

        totals.add(new JLabel("Solde :"));
        txtSolde = new JTextField(10);
        txtSolde.setEditable(false);
        totals.add(txtSolde);

        south.add(totals, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // events
        btnActualiser.addActionListener(e -> refreshAll());
        btnAjouter.addActionListener(e -> ajouterTransaction());
        btnRefreshReps.addActionListener(e -> refreshReparations());

        // init
        refresh();
    }

    public void refresh() {
        refreshReparations();
        refreshAll();
    }

    private void refreshReparations() {
        cbReparation.removeAllItems();
        if (reparateur == null || reparateur.getId() == null) return;

        cbReparation.addItem(null); // "Aucune"

        List<Reparation> reps = metier.listerReparationsParReparateur(reparateur.getId());
        for (Reparation r : reps) cbReparation.addItem(r);
    }

    private void refreshAll() {
        if (reparateur == null || reparateur.getId() == null) return;

        LocalDateTime debut = toLocalDateTime((Date) spDebut.getValue());
        LocalDateTime fin = toLocalDateTime((Date) spFin.getValue());

        // table
        model.setRowCount(0);
        List<Transaction> txs = metier.listerTransactions(reparateur.getId(), debut, fin);
        for (Transaction t : txs) {
            model.addRow(new Object[] {
                    t.getId(),
                    t.getDate(),
                    t.getMontant(),
                    t.getTypeOperation() != null ? t.getTypeOperation().name() : "",
                    t.getReparation() != null ? t.getReparation().getCodeSuivi() : "",
                    t.getDescription()
            });
        }

        // totals
        Double totalE = metier.totalEntrees(reparateur.getId(), debut, fin);
        Double totalS = metier.totalSorties(reparateur.getId(), debut, fin);
        Double solde = metier.solde(reparateur.getId(), debut, fin);

        txtTotalEntrees.setText(String.valueOf(totalE));
        txtTotalSorties.setText(String.valueOf(totalS));
        txtSolde.setText(String.valueOf(solde));
    }

    private void ajouterTransaction() {
        try {
            if (reparateur == null || reparateur.getId() == null) {
                JOptionPane.showMessageDialog(this, "Réparateur invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double montant = parseDouble(txtMontant.getText().trim(), "Montant");
            if (montant <= 0) throw new MetierException("Montant doit être > 0");

            TypeOperation op = (TypeOperation) cbOp.getSelectedItem();
            if (op == null) throw new MetierException("Type opération invalide");

            Reparation rep = (Reparation) cbReparation.getSelectedItem();
            Long idReparation = (rep != null ? rep.getId() : null);

            String desc = txtDescription.getText().trim();

            metier.ajouterTransaction(
                    reparateur.getId(),
                    idReparation,
                    LocalDateTime.now(),
                    montant,
                    op.name(),
                    desc
            );

            txtMontant.setText("");
            txtDescription.setText("");

            refreshAll();
            JOptionPane.showMessageDialog(this, "Transaction ajoutée.", "Succès", JOptionPane.INFORMATION_MESSAGE);

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

    private static LocalDateTime toLocalDateTime(Date d) {
        return Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static Date toDate(LocalDateTime dt) {
        return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
