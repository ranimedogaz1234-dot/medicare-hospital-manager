package application.dao;

import application.model.Medicament;
import application.util.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class MedicamentDAO {

    public void addMedicament(Medicament medicament) throws SQLException {
        String sql = "INSERT INTO medicaments (nom, dosage, indications, stock, prix) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicament.getNom());
            pstmt.setString(2, medicament.getDosage());
            pstmt.setString(3, medicament.getIndications());
            pstmt.setInt(4, medicament.getStock());
            pstmt.setDouble(5, medicament.getPrix());
            pstmt.executeUpdate();
        }
    }

    public void updateMedicament(Medicament medicament) throws SQLException {
        String sql = "UPDATE medicaments SET nom=?, dosage=?, indications=?, stock=?, prix=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicament.getNom());
            pstmt.setString(2, medicament.getDosage());
            pstmt.setString(3, medicament.getIndications());
            pstmt.setInt(4, medicament.getStock());
            pstmt.setDouble(5, medicament.getPrix());
            pstmt.setInt(6, medicament.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteMedicament(int id) throws SQLException {
        String sql = "DELETE FROM medicaments WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public ObservableList<Medicament> getAllMedicaments() throws SQLException {
        ObservableList<Medicament> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM medicaments ORDER BY nom";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Medicament(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("dosage"),
                    rs.getString("indications"),
                    rs.getInt("stock"),
                    rs.getDouble("prix")
                ));
            }
        }
        return list;
    }

    public Medicament getMedicamentById(int id) throws SQLException {
        String sql = "SELECT * FROM medicaments WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Medicament(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("dosage"),
                    rs.getString("indications"),
                    rs.getInt("stock"),
                    rs.getDouble("prix")
                );
            }
        }
        return null;
    }

    public int getTotalMedicaments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicaments";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
