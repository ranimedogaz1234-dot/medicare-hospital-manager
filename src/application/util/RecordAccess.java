package application.util;

import java.sql.*;

/** Application record scope. IDs always come from the validated staff session. */
public final class RecordAccess {
    private RecordAccess() {}
    public static String patientPredicate(String alias) {
        return SessionManager.isAdminLoggedIn() ? "1=1" :
            "("+alias+".id IN (SELECT patient_id FROM rendez_vous WHERE medecin_id=? UNION SELECT patient_id FROM dossiers_medicaux WHERE medecin_id=? UNION SELECT patient_id FROM patient_medecins WHERE medecin_id=?))";
    }
    public static int bindPatient(PreparedStatement q,int index) throws SQLException {
        if(!SessionManager.isAdminLoggedIn()){int id=SessionManager.getMedecinId();q.setInt(index++,id);q.setInt(index++,id);q.setInt(index++,id);}return index;
    }
    public static String ownerPredicate(String alias) {
        return SessionManager.isAdminLoggedIn()?"1=1":alias+".medecin_id=?";
    }
    public static int bindOwner(PreparedStatement q,int index) throws SQLException {
        if(!SessionManager.isAdminLoggedIn())q.setInt(index++,SessionManager.getMedecinId());return index;
    }
    public static void requireDoctor(int id) throws SQLException {
        DatabaseUtil.verifyCurrentSession();
        if(!SessionManager.isAdminLoggedIn()&&SessionManager.getMedecinId()!=id)throw new SQLException("Action non autorisée.");
    }
    public static void requirePatient(Connection c,int id) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT p.id FROM patients p WHERE p.id=? AND "+patientPredicate("p"))){
            q.setInt(1,id);bindPatient(q,2);try(ResultSet rs=q.executeQuery()){if(!rs.next())throw new SQLException("Patient introuvable ou accès non autorisé.");}
        }
    }
}
