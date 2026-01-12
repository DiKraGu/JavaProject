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
import dao.enums.TypeCaisse;
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
    private JComboBox<TypeCaisse> cbCaisse;
    private JComboBox<Reparation> cbReparation;

    private JTextField txtMontant;
    private JTextField txtDescription;

    private JTextField txtSoldeTR;
    private JTextField txtSoldeRep;

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

        JButton btnRefresh = new JButton("Rafraîchir");
        top.add(btnRefresh);

        add(top, BorderLayout.NORTH);

        // init dates
        LocalDateTime now = LocalDateTime.now();
        spDebut.setValue(toDate(now.withHour(0).withMinute(0)));
        spFin.setValue(toDate(now));

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[][] {}, new String[] {
                "ID", "Date", "Montant", "Opération", "Caisse", "Réparation", "Description"
        }) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== SOUTH (ajout + soldes) =====
        JPanel south = new JPanel(new BorderLayout(10, 10));

        JPanel addTx = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addTx.add(new JLabel("Montant :"));
        txtMontant = new JTextField(8);
        addTx.add(txtMontant);

        addTx.add(new JLabel("Opération :"));
        cbOp = new JComboBox<>(TypeOperation.values());
        addTx.add(cbOp);

        addTx.add(new JLabel("Caisse :"));
        cbCaisse = new JComboBox<>(TypeCaisse.values());
        addTx.add(cbCaisse);

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

        south.add(addTx, BorderLayout.NORTH);

        JPanel soldes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        soldes.add(new JLabel("Solde Temps Réel :"));
        txtSoldeTR = new JTextField(10);
        txtSoldeTR.setEditable(false);
        soldes.add(txtSoldeTR);

        soldes.add(new JLabel("   |   "));

        soldes.add(new JLabel("Solde Réparation :"));
        txtSoldeRep = new JTextField(10);
        txtSoldeRep.setEditable(false);
        soldes.add(txtSoldeRep);

        JButton btnRefreshReps = new JButton("Rafraîchir réparations");
        soldes.add(btnRefreshReps);

        south.add(soldes, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // events
        btnRefresh.addActionListener(e -> refreshAll());
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

        model.setRowCount(0);
        List<Transaction> txs = metier.listerTransactions(reparateur.getId(), debut, fin);
        for (Transaction t : txs) {
            model.addRow(new Object[] {
                    t.getId(),
                    t.getDate(),
                    t.getMontant(),
                    t.getTypeOperation() != null ? t.getTypeOperation().name() : "",
                    t.getTypeCaisse() != null ? t.getTypeCaisse().name() : "",
                    t.getReparation() != null ? t.getReparation().getCodeSuivi() : "",
                    t.getDescription()
            });
        }

        Double soldeTR = metier.soldeTempsReel(reparateur.getId(), debut, fin);
        Double soldeRep = metier.soldeReparation(reparateur.getId(), debut, fin);

        txtSoldeTR.setText(String.valueOf(soldeTR));
        txtSoldeRep.setText(String.valueOf(soldeRep));
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
            TypeCaisse caisse = (TypeCaisse) cbCaisse.getSelectedItem();
            if (op == null || caisse == null) throw new MetierException("Type opération / caisse invalide");

            Reparation rep = (Reparation) cbReparation.getSelectedItem();
            Long idReparation = rep != null ? rep.getId() : null;

            String desc = txtDescription.getText().trim();

            metier.ajouterTransaction(
                    reparateur.getId(),
                    idReparation,
                    LocalDateTime.now(),
                    montant,
                    op.name(),
                    caisse.name(),
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
