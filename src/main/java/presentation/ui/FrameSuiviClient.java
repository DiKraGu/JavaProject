package presentation.ui;

import java.awt.EventQueue;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.LigneReparation;
import dao.Reparation;
import exception.NotFoundException;
import metier.impl.ClientImpl;
import metier.interfaces.IClient;

public class FrameSuiviClient extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextField txtCode;

    private JLabel lblCode;
    private JLabel lblClient;
    private JLabel lblDate;
    private JLabel lblStatut;
    private JLabel lblCout;
    private JTextArea txtPanne;

    private JTable table;
    private DefaultTableModel model;

    private final IClient clientMetier = new ClientImpl();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            FrameSuiviClient f = new FrameSuiviClient();
            f.setVisible(true);
        });
    }

    public FrameSuiviClient() {
        setTitle("Suivi réparation (Client)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 900, 520);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel title = new JLabel("Suivi de réparation");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(10, 10, 860, 25);
        contentPane.add(title);

        JLabel lbl = new JLabel("Code suivi :");
        lbl.setBounds(30, 55, 90, 25);
        contentPane.add(lbl);

        txtCode = new JTextField();
        txtCode.setBounds(120, 55, 220, 25);
        contentPane.add(txtCode);

        JButton btnRechercher = new JButton("Rechercher");
        btnRechercher.setBounds(350, 55, 120, 25);
        contentPane.add(btnRechercher);

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBounds(770, 55, 100, 25);
        contentPane.add(btnFermer);

        // Infos réparation
        lblCode = new JLabel("Code : -");
        lblCode.setBounds(30, 95, 400, 20);
        contentPane.add(lblCode);

        lblClient = new JLabel("Client : -");
        lblClient.setBounds(30, 120, 400, 20);
        contentPane.add(lblClient);

        lblDate = new JLabel("Date : -");
        lblDate.setBounds(30, 145, 400, 20);
        contentPane.add(lblDate);

        lblStatut = new JLabel("Statut : -");
        lblStatut.setBounds(470, 95, 250, 20);
        contentPane.add(lblStatut);

        lblCout = new JLabel("Coût total : -");
        lblCout.setBounds(470, 120, 250, 20);
        contentPane.add(lblCout);

        JLabel lblPanne = new JLabel("Description panne :");
        lblPanne.setBounds(30, 175, 150, 20);
        contentPane.add(lblPanne);

        txtPanne = new JTextArea();
        txtPanne.setLineWrap(true);
        txtPanne.setWrapStyleWord(true);
        txtPanne.setEditable(false);
        JScrollPane spPanne = new JScrollPane(txtPanne);
        spPanne.setBounds(30, 200, 840, 70);
        contentPane.add(spPanne);

        // Table lignes
        model = new DefaultTableModel(
                new Object[]{"Appareil", "Etat appareil", "Coût appareil", "Commentaire"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(30, 290, 840, 170);
        contentPane.add(sp);

        btnRechercher.addActionListener(e -> rechercher());
        btnFermer.addActionListener(e -> dispose());
    }

    private void rechercher() {
        try {
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir le code de suivi.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Reparation r = clientMetier.suivreReparationParCode(code);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            lblCode.setText("Code : " + r.getCodeSuivi());
            lblClient.setText("Client : " + (r.getClient() != null ? (r.getClient().getNom() + " " + r.getClient().getPrenom()) : "-"));
            lblDate.setText("Date : " + (r.getDateCreation() != null ? r.getDateCreation().format(fmt) : "-"));
            lblStatut.setText("Statut : " + (r.getStatut() != null ? r.getStatut().name() : "-"));
            lblCout.setText("Coût total : " + (r.getCoutTotal() != null ? r.getCoutTotal() : 0.0));

            txtPanne.setText(r.getDescriptionPanne() != null ? r.getDescriptionPanne() : "");

            // Remplir lignes
            model.setRowCount(0);
            List<LigneReparation> lignes = r.getLignes();
            if (lignes != null) {
                for (LigneReparation lr : lignes) {
                    String app = (lr.getAppareil() != null) ? lr.getAppareil().toString() : "-";
                    String etat = (lr.getEtatAppareil() != null) ? lr.getEtatAppareil().name() : "-";
                    Double cout = lr.getCoutAppareil() != null ? lr.getCoutAppareil() : 0.0;
                    String com = lr.getCommentaire() != null ? lr.getCommentaire() : "";
                    model.addRow(new Object[]{app, etat, cout, com});
                }
            }

        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Introuvable", JOptionPane.WARNING_MESSAGE);
            clearAffichage();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearAffichage() {
        lblCode.setText("Code : -");
        lblClient.setText("Client : -");
        lblDate.setText("Date : -");
        lblStatut.setText("Statut : -");
        lblCout.setText("Coût total : -");
        txtPanne.setText("");
        model.setRowCount(0);
    }
}
