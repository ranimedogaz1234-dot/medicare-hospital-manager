package application.dao;

import application.model.Utilisateur;
import application.util.DatabaseUtil;
import application.util.PasswordUtil;

import java.sql.*;

public class UtilisateurDAO {

    public Utilisateur authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseUtil.openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.trim());
            Utilisateur utilisateur = null;
            String storedPassword = null;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Legacy schemas may mix doctors and administrators in this table.
                    // Fresh schemas contain administrators only; never upgrade a legacy doctor.
                    for (int column = 1; column <= rs.getMetaData().getColumnCount(); column++) {
                        if ("role".equalsIgnoreCase(rs.getMetaData().getColumnLabel(column))) {
                            String role = rs.getString(column);
                            if (!"admin".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role)) return null;
                        }
                    }
                    storedPassword = rs.getString("password");
                    if (!PasswordUtil.verify(password, storedPassword)) {
                        return null;
                    }
                    utilisateur = new Utilisateur();
                    utilisateur.setId(rs.getInt("id"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setPassword(null);
                }
            }
            if (utilisateur != null && PasswordUtil.needsRehash(storedPassword)) {
                try (PreparedStatement update = conn.prepareStatement("UPDATE utilisateurs SET password=? WHERE id=?")) {
                    update.setString(1,PasswordUtil.hash(password)); update.setInt(2,utilisateur.getId()); update.executeUpdate();
                }
            }
            return utilisateur;
        }
    }
    public void updatePassword(int userId, String newPassword) throws SQLException {
        application.util.SessionManager.requireAdmin();
        String sql = "UPDATE utilisateurs SET password=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hash(newPassword));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
}
