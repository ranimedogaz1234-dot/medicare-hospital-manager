package application.dao;

import application.util.DatabaseUtil;
import application.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;


public final class DoctorRegistrationDAO {
    public boolean register(String name,String email,String specialty,String password) throws SQLException {
        try (Connection conn=DatabaseUtil.openConnection()) {
            // Fail closed on a legacy schema that lacks uniqueness; never auto-migrate at signup.
            java.util.Map<String,java.util.List<String>> keys=new java.util.HashMap<>();
            try(ResultSet indexes=conn.getMetaData().getIndexInfo(conn.getCatalog(),null,"medecins",true,false)) {
                while(indexes.next()) {
                    String key=indexes.getString("INDEX_NAME"),column=indexes.getString("COLUMN_NAME");
                    if(key!=null && column!=null)keys.computeIfAbsent(key,k->new ArrayList<>()).add(column);
                }
            }
            boolean unique=keys.values().stream().anyMatch(columns->columns.size()==1&&"email_connexion".equalsIgnoreCase(columns.getFirst()));
            if(!unique) throw new SQLException("Doctor login uniqueness must be configured.");
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement q=conn.prepareStatement("SELECT 1 FROM medecins WHERE LOWER(TRIM(email_connexion))=? OR LOWER(TRIM(email))=? UNION ALL SELECT 1 FROM utilisateurs WHERE LOWER(TRIM(email))=? LIMIT 1")) {
                    q.setString(1,email);q.setString(2,email);q.setString(3,email);
                    try(ResultSet rs=q.executeQuery()){if(rs.next()){conn.rollback();return false;}}
                }
                int split=name.lastIndexOf(' ');
                // Historical approval columns may remain; keep their metadata compatible
                // without making authentication depend on them. Fresh schemas omit them.
                boolean historicalStatus;
                try(ResultSet columns=conn.getMetaData().getColumns(conn.getCatalog(),null,"medecins","account_status")){historicalStatus=columns.next();}
                String sql=historicalStatus
                    ? "INSERT INTO medecins(nom,prenom,specialite,email,email_connexion,mot_de_passe,disponible,account_status) VALUES(?,?,?,?,?,?,1,'APPROVED')"
                    : "INSERT INTO medecins(nom,prenom,specialite,email,email_connexion,mot_de_passe,disponible) VALUES(?,?,?,?,?,?,1)";
                try(PreparedStatement insert=conn.prepareStatement(sql)) {
                    insert.setString(1,name.substring(split+1));insert.setString(2,name.substring(0,split));insert.setString(3,specialty);
                    insert.setString(4,email);insert.setString(5,email);insert.setString(6,PasswordUtil.hash(password));insert.executeUpdate();
                }
                conn.commit();return true;
            } catch(SQLException e) {
                conn.rollback();if(e.getErrorCode()==1062)return false;throw e;
            }
        }
    }

}
