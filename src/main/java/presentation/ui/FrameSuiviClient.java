package presentation.ui;

import java.awt.EventQueue;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import dao.LigneReparation;
import dao.Reparation;
import dao.enums.StatutReparation;
import exception.NotFoundException;
import metier.impl.ClientImpl;
import metier.interfaces.IClient;

public class FrameSuiviClient extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTextField txtCode;
    private JTextArea txtResult;

    private IClient clientMetier = new ClientImpl();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FrameSuiviClient frame = new FrameSuiviClient();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public FrameSuiviClient() {
        setTitle("Suivi de réparation");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 700, 450);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lbl = new JLabel("Code de suivi :");
        lbl.setBounds(20, 20, 120, 25);
        contentPane.add(lbl);

        txtCode = new JTextField();
        txtCode.setBounds(140, 20, 300, 25);
        contentPane.add(txtCode);
        txtCode.setColumns(10);

        JButton btnRechercher = new JButton("Rechercher");
        btnRechercher.setBounds(450, 20, 120, 25);
        contentPane.add(btnRechercher);

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setBounds(580, 20, 90, 25);
        contentPane.add(btnFermer);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 70, 650, 320);
        contentPane.add(scrollPane);

        txtResult = new JTextArea();
        txtResult.setEditable(false);
        scrollPane.setViewportView(txtResult);

        btnRechercher.addActionListener(e -> rechercher());
        btnFermer.addActionListener(e -> dispose());
    }

    private void rechercher() {
        try {
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez saisir le code de suivi.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Reparation r = clientMetier.suivreReparationParCode(code);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            StringBuilder sb = new StringBuilder();
            sb.append("=== RÉPARATION TROUVÉE ===\n");
            sb.append("Code : ").append(r.getCodeSuivi()).append("\n");

            // Statut (version “client-friendly”)
            sb.append("Statut : ").append(libelleStatut(r.getStatut())).append("\n");

            sb.append("Coût total : ").append(r.getCoutTotal()).append("\n");
            sb.append("Date : ").append(r.getDateCreation().format(fmt)).append("\n");

            // Boutique (utile au client)
            if (r.getBoutique() != null) {
                sb.append("Boutique : ").append(r.getBoutique().getNom()).append("\n");
            }

            sb.append("\nDescription panne :\n");
            sb.append(r.getDescriptionPanne()).append("\n\n");

            // Liste des appareils + état par appareil
            if (r.getLignes() != null && !r.getLignes().isEmpty()) {
                sb.append("=== APPAREILS ===\n");
                for (LigneReparation lr : r.getLignes()) {
                    sb.append("- ")
                      .append(lr.getAppareil().getType()).append(" ")
                      .append(lr.getAppareil().getMarque()).append(" ")
                      .append(lr.getAppareil().getModele())
                      .append(" | État : ").append(libelleEtatAppareil(lr.getEtatAppareil().name()))
                      .append(" | Coût : ").append(lr.getCoutAppareil())
                      .append("\n");
                }
            } else {
                sb.append("Aucun appareil enregistré.\n");
            }

            txtResult.setText(sb.toString());

        } catch (NotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Introuvable", JOptionPane.ERROR_MESSAGE);
            txtResult.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Transforme le statut enum en libellé affichable.
     * Adapte les libellés si tu veux.
     */
    private String libelleStatut(StatutReparation statut) {
        if (statut == null) return "Inconnu";
        switch (statut) {
            case EN_COURS:
                return "En cours";
            case TERMINEE:
                return "Terminée";
            case ANNULEE:
                return "Annulée";
            case RENDUE:
                return "Rendue";
            default:
                return statut.name();
        }
    }

    /**
     * Libellé pour l'état d'un appareil (LigneReparation.etatAppareil).
     * Si ton enum s'appelle différemment, garde simplement la méthode et adapte les cases.
     */
    private String libelleEtatAppareil(String etat) {
        if (etat == null) return "Inconnu";
        switch (etat) {
            case "EN_COURS":
                return "En cours";
            case "TERMINEE":
                return "Terminé";
            case "ANNULEE":
                return "Annulé";
            case "RENDUE":
                return "Rendu";
            default:
                return etat;
        }
    }
}
