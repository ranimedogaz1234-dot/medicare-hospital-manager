package application.dao;

import application.model.Medecin;
import application.util.DatabaseUtil;
import application.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class MedecinDAO {

    public void addMedecin(Medecin medecin) throws SQLException { application.util.SessionManager.requireAdmin();
        String sql = "INSERT INTO medecins (nom, prenom, specialite, telephone, email, email_connexion, mot_de_passe, disponible) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bindMedecinFields(pstmt, medecin);
            pstmt.executeUpdate();
        }
    }

    public void updateMedecin(Medecin medecin) throws SQLException { application.util.RecordAccess.requireDoctor(medecin.getId());
        String sql = "UPDATE medecins SET nom=?, prenom=?, specialite=?, telephone=?, email=?, "
                + "email_connexion=?, disponible=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medecin.getNom());
            pstmt.setString(2, medecin.getPrenom());
            pstmt.setString(3, medecin.getSpecialite());
            pstmt.setString(4, medecin.getTelephone());
            pstmt.setString(5, medecin.getEmail());
            pstmt.setString(6, medecin.getEmailConnexion());
            pstmt.setInt(7, medecin.isDisponible() ? 1 : 0);
            pstmt.setInt(8, medecin.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateMedecinWithPassword(Medecin medecin) throws SQLException { application.util.RecordAccess.requireDoctor(medecin.getId());
        String sql = "UPDATE medecins SET nom=?, prenom=?, specialite=?, telephone=?, email=?, "
                + "email_connexion=?, mot_de_passe=?, disponible=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medecin.getNom());
            pstmt.setString(2, medecin.getPrenom());
            pstmt.setString(3, medecin.getSpecialite());
            pstmt.setString(4, medecin.getTelephone());
            pstmt.setString(5, medecin.getEmail());
            pstmt.setString(6, medecin.getEmailConnexion());
            pstmt.setString(7, PasswordUtil.hash(medecin.getMotDePasse()));
            pstmt.setInt(8, medecin.isDisponible() ? 1 : 0);
            pstmt.setInt(9, medecin.getId());
            pstmt.executeUpdate();
        }
    }

    public void updatePassword(int medecinId, String newPassword) throws SQLException { application.util.RecordAccess.requireDoctor(medecinId);
        String sql = "UPDATE medecins SET mot_de_passe=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hash(newPassword));
            pstmt.setInt(2, medecinId);
            pstmt.executeUpdate();
        }
    }

    public Medecin authenticate(String emailConnexion, String password) throws SQLException {
        String sql = "SELECT * FROM medecins WHERE LOWER(email_connexion) = LOWER(?)";
        try (Connection conn = DatabaseUtil.openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emailConnexion.trim());
            Medecin medecin = null;
            String storedPassword = null;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    storedPassword = rs.getString("mot_de_passe");
                    if (!PasswordUtil.verify(password, storedPassword)) {
                        return null;
                    }
                    medecin = mapMedecin(rs);
                }
            }
            if (medecin != null && PasswordUtil.needsRehash(storedPassword)) {
                try (PreparedStatement update=conn.prepareStatement("UPDATE medecins SET mot_de_passe=? WHERE id=?")) {
                    update.setString(1,PasswordUtil.hash(password)); update.setInt(2,medecin.getId()); update.executeUpdate();
                }
            }
            return medecin;
        }
    }
    public void deleteMedecin(int id) throws SQLException { application.util.RecordAccess.requireDoctor(id);
        String sql = "DELETE FROM medecins WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public ObservableList<Medecin> getAllMedecins() throws SQLException {
        ObservableList<Medecin> list = FXCollections.observableArrayList();
        String sql = application.util.SessionManager.isAdminLoggedIn()
                ? "SELECT * FROM medecins ORDER BY nom, prenom"
                : "SELECT id,nom,prenom,specialite,NULL AS telephone,NULL AS email,NULL AS email_connexion,disponible FROM medecins ORDER BY nom,prenom";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapMedecin(rs));
            }
        }
        return list;
    }

    public Medecin getMedecinById(int id) throws SQLException { application.util.RecordAccess.requireDoctor(id);
        String sql = "SELECT * FROM medecins WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapMedecin(rs);
            }
        }
        return null;
    }

    public int getTotalMedecins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM medecins";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private void bindMedecinFields(PreparedStatement pstmt, Medecin medecin) throws SQLException {
        pstmt.setString(1, medecin.getNom());
        pstmt.setString(2, medecin.getPrenom());
        pstmt.setString(3, medecin.getSpecialite());
        pstmt.setString(4, medecin.getTelephone());
        pstmt.setString(5, medecin.getEmail());
        pstmt.setString(6, medecin.getEmailConnexion());
        pstmt.setString(7, PasswordUtil.hash(medecin.getMotDePasse()));
        pstmt.setInt(8, medecin.isDisponible() ? 1 : 0);
    }

    private Medecin mapMedecin(ResultSet rs) throws SQLException {
        return new Medecin(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("specialite"),
            rs.getString("telephone"),
            rs.getString("email"),
            rs.getString("email_connexion"),
            null,
            rs.getInt("disponible") == 1
        );
    }
}
