package presentation.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dao.Boutique;
import dao.User;
import dao.enums.Role;
import exception.MetierException;
import metier.impl.ProprietaireImpl;
import metier.interfaces.IProprietaire;

public class FrameGestionReparateurs extends JPanel {

    private static final long serialVersionUID = 1L;

    private final User proprietaire;
    private final IProprietaire metier = new ProprietaireImpl();

    private JComboBox<Boutique> cbBoutiques;

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtEmail;
    private JPasswordField txtPwd;
    private JTextField txtNom;
    private JTextField txtPrenom;
    private JTextField txtTel;
    private JTextField txtPourcentage;

    private JButton btnRefreshBoutiques;
    private JButton btnCharger;

    private JButton btnAjouter;
    private JButton btnModifier;
    private JButton btnSupprimer;
    private JButton btnVider;

    // Pour éviter double reload quand on refresh la combo (actionListener déclenché)
    private boolean isRefreshingCombo = false;

    public FrameGestionReparateurs(User proprietaire) {
        this.proprietaire = proprietaire;
        setLayout(new BorderLayout());

        JLabel titre = new JLabel("Gestion des Réparateurs (par Boutique)");
        titre.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== TOP =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Boutique :"));

        cbBoutiques = new JComboBox<>();
        cbBoutiques.setPrototypeDisplayValue(Boutique.builder().nom("XXXXXXXXXXXXXXXXXXXXXXXX").build());
        top.add(cbBoutiques);

        btnRefreshBoutiques = new JButton("Rafraîchir boutiques");
        top.add(btnRefreshBoutiques);

        btnCharger = new JButton("Charger réparateurs");
        top.add(btnCharger);

        JPanel north = new JPanel(new BorderLayout());
        north.add(titre, BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new Object[]{"ID", "Nom", "Prénom", "Email", "Téléphone", "%"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM =====
        JPanel south = new JPanel(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l1.add(new JLabel("Email :"));
        txtEmail = new JTextField(18);
        l1.add(txtEmail);

        l1.add(new JLabel("Mot de passe :"));
        txtPwd = new JPasswordField(12);
        l1.add(txtPwd);

        JPanel l2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l2.add(new JLabel("Nom :"));
        txtNom = new JTextField(12);
        l2.add(txtNom);

        l2.add(new JLabel("Prénom :"));
        txtPrenom = new JTextField(12);
        l2.add(txtPrenom);

        JPanel l3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l3.add(new JLabel("Téléphone :"));
        txtTel = new JTextField(12);
        l3.add(txtTel);

        l3.add(new JLabel("% :"));
        txtPourcentage = new JTextField(6);
        l3.add(txtPourcentage);

        JLabel infoPwd = new JLabel("Info : laisser mot de passe vide = ne pas changer.");
        l3.add(infoPwd);

        form.add(l1);
        form.add(l2);
        form.add(l3);

        south.add(form, BorderLayout.CENTER);

        // ===== BUTTONS =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAjouter = new JButton("Ajouter");
        btnModifier = new JButton("Modifier (tout)");
        btnSupprimer = new JButton("Supprimer");
        btnVider = new JButton("Vider");

        buttons.add(btnAjouter);
        buttons.add(btnModifier);
        buttons.add(btnSupprimer);
        buttons.add(btnVider);

        south.add(buttons, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        // ===== events =====
        btnRefreshBoutiques.addActionListener(e -> refreshBoutiquesKeepSelection());
        btnCharger.addActionListener(e -> chargerReparateurs());

        btnAjouter.addActionListener(e -> ajouterReparateur());
        btnModifier.addActionListener(e -> modifierReparateur());
        btnSupprimer.addActionListener(e -> supprimerReparateur());
        btnVider.addActionListener(e -> viderForm());

        table.getSelectionModel().addListSelectionListener(e -> remplirFormDepuisTable());

        // Changement boutique => recharge auto (mais pas pendant refresh combo)
        cbBoutiques.addActionListener(e -> {
            if (!isRefreshingCombo) {
                viderForm();          // optionnel: tu peux enlever si tu veux
                chargerReparateurs();
            }
        });

        // init
        chargerBoutiques();
    }

    /**
     * À appeler depuis FrameProprietaire quand tu affiches ce panel,
     * comme ça la combo boutiques est toujours à jour.
     */
    public void refresh() {
        refreshBoutiquesKeepSelection();
    }

    private void chargerBoutiques() {
        isRefreshingCombo = true;
        try {
            cbBoutiques.removeAllItems();
            List<Boutique> bs = metier.listerBoutiquesDuProprietaire(proprietaire.getId());
            for (Boutique b : bs) cbBoutiques.addItem(b);
            if (cbBoutiques.getItemCount() > 0) cbBoutiques.setSelectedIndex(0);
        } finally {
            isRefreshingCombo = false;
        }
        chargerReparateurs();
    }

    private void refreshBoutiquesKeepSelection() {
        Boutique selected = (Boutique) cbBoutiques.getSelectedItem();
        Long selectedId = (selected != null ? selected.getId() : null);

        isRefreshingCombo = true;
        try {
            cbBoutiques.removeAllItems();
            List<Boutique> bs = metier.listerBoutiquesDuProprietaire(proprietaire.getId());

            Boutique toSelect = null;
            for (Boutique b : bs) {
                cbBoutiques.addItem(b);
                if (selectedId != null && selectedId.equals(b.getId())) {
                    toSelect = b;
                }
            }

            if (toSelect != null) {
                cbBoutiques.setSelectedItem(toSelect);
            } else if (cbBoutiques.getItemCount() > 0) {
                cbBoutiques.setSelectedIndex(0);
            }
        } finally {
            isRefreshingCombo = false;
        }

        viderForm();       // optionnel: tu peux enlever
        chargerReparateurs();
    }

    private Boutique boutiqueSelectionnee() {
        return (Boutique) cbBoutiques.getSelectedItem();
    }

    private void chargerReparateurs() {
        model.setRowCount(0);
        Boutique b = boutiqueSelectionnee();
        if (b == null) return;

        List<User> reps = metier.listerReparateursParBoutique(b.getId());
        for (User u : reps) {
            model.addRow(new Object[]{
                    u.getId(),
                    u.getNom(),
                    u.getPrenom(),
                    u.getEmail(),
                    u.getTelephone(),
                    u.getPourcentage()
            });
        }
    }

    private void ajouterReparateur() {
        Boutique b = boutiqueSelectionnee();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Crée d'abord une boutique.");
            return;
        }

        try {
            Double pct = Double.valueOf(txtPourcentage.getText().trim());

            String email = txtEmail.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String tel = txtTel.getText().trim();

            User rep = User.builder()
                    .email(email)
                    .motDePasse(pwd)
                    .nom(nom)
                    .prenom(prenom)
                    .telephone(tel)
                    .role(Role.REPARATEUR)
                    .pourcentage(pct)
                    .build();

            metier.creerReparateur(rep, b.getId());

            JOptionPane.showMessageDialog(this, "Réparateur ajouté !");
            viderForm();
            chargerReparateurs();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Pourcentage invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierReparateur() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne un réparateur dans la table.");
            return;
        }

        Boutique b = boutiqueSelectionnee();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Aucune boutique sélectionnée.");
            return;
        }

        try {
            Long idRep = (Long) model.getValueAt(row, 0);
            Double pct = Double.valueOf(txtPourcentage.getText().trim());

            String email = txtEmail.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim(); // vide => ne change pas
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String tel = txtTel.getText().trim();

            User rep = User.builder()
                    .id(idRep)
                    .email(email)
                    .motDePasse(pwd)
                    .nom(nom)
                    .prenom(prenom)
                    .telephone(tel)
                    .role(Role.REPARATEUR)
                    .pourcentage(pct)
                    .build();

            metier.modifierReparateur(rep, b.getId());

            JOptionPane.showMessageDialog(this, "Réparateur modifié !");
            viderForm();
            chargerReparateurs();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Pourcentage invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerReparateur() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Sélectionne un réparateur dans la table.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Supprimer ce réparateur ?", "Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            Long idRep = (Long) model.getValueAt(row, 0);
            metier.supprimerReparateur(idRep);

            JOptionPane.showMessageDialog(this, "Réparateur supprimé !");
            viderForm();
            chargerReparateurs();

        } catch (MetierException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur système : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirFormDepuisTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        txtNom.setText(String.valueOf(model.getValueAt(row, 1)));
        txtPrenom.setText(String.valueOf(model.getValueAt(row, 2)));
        txtEmail.setText(String.valueOf(model.getValueAt(row, 3)));

        Object tel = model.getValueAt(row, 4);
        txtTel.setText(tel != null ? tel.toString() : "");

        Object pct = model.getValueAt(row, 5);
        txtPourcentage.setText(pct != null ? pct.toString() : "");

        // mot de passe : on ne le charge pas
        txtPwd.setText("");
    }

    private void viderForm() {
        txtEmail.setText("");
        txtPwd.setText("");
        txtNom.setText("");
        txtPrenom.setText("");
        txtTel.setText("");
        txtPourcentage.setText("");
        table.clearSelection();
    }
}
