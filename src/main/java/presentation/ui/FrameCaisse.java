package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.*;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

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

    // création manuelle = SORTIE uniquement
    private JComboBox<TypeOperation> cbOp;

    private JTextField txtMontant;
    private JTextField txtDescription;

    private JTextField txtTotalEntrees;
    private JTextField txtTotalSorties;
    private JTextField txtSolde;

    private Timer autoRefreshTimer;

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
        spFin.setValue(toDate(now)); // sera ensuite MAJ automatiquement

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
        cbOp = new JComboBox<>(new TypeOperation[] { TypeOperation.SORTIE });
        cbOp.setEnabled(false);
        addTx.add(cbOp);

        addTx.add(new JLabel("Description :"));
        txtDescription = new JTextField(22);
        addTx.add(txtDescription);

        JButton btnAjouter = new JButton("Ajouter dépense (SORTIE)");
        addTx.add(btnAjouter);

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
        btnAjouter.addActionListener(e -> ajouterSortie());

        // init
        refreshAll();
        startAutoRefresh();
    }

    /**
     * Pour FrameReparateur.showCaisse()
     * On garde simple : un refresh immédiat + timer.
     */
    public void refresh() {
        refreshAll();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        if (autoRefreshTimer != null) autoRefreshTimer.stop();

        autoRefreshTimer = new Timer(2000, e -> refreshAll()); // 2 sec = plus réactif
        autoRefreshTimer.setRepeats(true);
        autoRefreshTimer.start();
    }

    private void refreshAll() {
        if (reparateur == null || reparateur.getId() == null) return;

        // FIX LAG : fin = maintenant (sinon les nouvelles transactions sont hors plage)
        Date nowDate = new Date();
        spFin.setValue(nowDate);

        LocalDateTime debut = toLocalDateTime((Date) spDebut.getValue());
        LocalDateTime fin = toLocalDateTime(nowDate);

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

        Double totalE = metier.totalEntrees(reparateur.getId(), debut, fin);
        Double totalS = metier.totalSorties(reparateur.getId(), debut, fin);
        Double solde = metier.solde(reparateur.getId(), debut, fin);

        txtTotalEntrees.setText(String.valueOf(totalE));
        txtTotalSorties.setText(String.valueOf(totalS));
        txtSolde.setText(String.valueOf(solde));
    }

    private void ajouterSortie() {
        try {
            if (reparateur == null || reparateur.getId() == null) {
                JOptionPane.showMessageDialog(this, "Réparateur invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double montant = parseDouble(txtMontant.getText().trim(), "Montant");
            if (montant <= 0) throw new MetierException("Montant doit être > 0");

            String desc = txtDescription.getText().trim();
            if (desc.isEmpty()) desc = "Dépense";

            metier.ajouterTransaction(
                    reparateur.getId(),
                    null,
                    LocalDateTime.now(),
                    montant,
                    TypeOperation.SORTIE.name(),
                    desc
            );

            txtMontant.setText("");
            txtDescription.setText("");

            refreshAll();
            JOptionPane.showMessageDialog(this, "Dépense ajoutée (SORTIE).", "Succès", JOptionPane.INFORMATION_MESSAGE);

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
