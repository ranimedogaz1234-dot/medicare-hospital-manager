package application.dao;

import application.model.Patient;
import application.util.*;
import javafx.collections.*;
import java.sql.*;

public class PatientDAO {
    // Creation links a doctor only to the newly generated patient ID, atomically.
    public void addPatient(Patient p) throws SQLException {
        DatabaseUtil.verifyCurrentSession();
        validatePatient(p);
        Integer doctor=SessionManager.isMedecinLoggedIn()?SessionManager.getMedecinId():null;
        int id;
        try(Connection c=DatabaseUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement q=c.prepareStatement("INSERT INTO patients(nom,prenom,date_naissance,sexe,telephone,email,adresse,groupe_sanguin,allergies) VALUES(?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    bind(q,p);q.executeUpdate();
                    try(ResultSet keys=q.getGeneratedKeys()){if(!keys.next())throw new SQLException("Patient creation failed.");id=keys.getInt(1);}
                }
                if(doctor!=null)try(PreparedStatement q=c.prepareStatement("INSERT INTO patient_medecins(patient_id,medecin_id) VALUES(?,?)")){q.setInt(1,id);q.setInt(2,doctor);q.executeUpdate();}
                c.commit();
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
        p.setId(id); DataChanges.publish();
    }
    public void updatePatient(Patient p) throws SQLException {
        SessionManager.requireAdmin();
        validatePatient(p);
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("UPDATE patients SET nom=?,prenom=?,date_naissance=?,sexe=?,telephone=?,email=?,adresse=?,groupe_sanguin=?,allergies=? WHERE id=?")){
            bind(q,p);q.setInt(10,p.getId());q.executeUpdate();DataChanges.publish();
        }
    }
    public void deletePatient(int id) throws SQLException {
        SessionManager.requireAdmin();
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("DELETE FROM patients WHERE id=?")){q.setInt(1,id);q.executeUpdate();DataChanges.publish();}
    }
    public ObservableList<Patient> getAllPatients() throws SQLException {return select("1=1");}
    public Patient getPatientById(int id) throws SQLException {var rows=select("p.id=?",id);return rows.isEmpty()?null:rows.getFirst();}
    public ObservableList<Patient> searchPatients(String keyword) throws SQLException {
        String term="%"+keyword+"%";return select("(p.nom LIKE ? OR p.prenom LIKE ? OR p.telephone LIKE ?)",term,term,term);
    }
    public int getTotalPatients() throws SQLException {
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM patients p WHERE "+RecordAccess.patientPredicate("p"))){
            RecordAccess.bindPatient(q,1);try(ResultSet rs=q.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }
    public ObservableList<Patient> getPatientsByMedecin(int id) throws SQLException {
        RecordAccess.requireDoctor(id);
        return select("p.id IN (SELECT patient_id FROM rendez_vous WHERE medecin_id=? UNION SELECT patient_id FROM dossiers_medicaux WHERE medecin_id=? UNION SELECT patient_id FROM patient_medecins WHERE medecin_id=?)",id,id,id);
    }
    public int countPatientsByMedecin(int id) throws SQLException {return getPatientsByMedecin(id).size();}
    private ObservableList<Patient> select(String where,Object...args) throws SQLException {
        var rows=FXCollections.<Patient>observableArrayList();
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("SELECT p.* FROM patients p WHERE ("+where+") AND "+RecordAccess.patientPredicate("p")+" ORDER BY p.nom,p.prenom")){
            int index=1;for(Object value:args)q.setObject(index++,value);RecordAccess.bindPatient(q,index);
            try(ResultSet rs=q.executeQuery()){while(rs.next())rows.add(map(rs));}
        }return rows;
    }
    private void bind(PreparedStatement q,Patient p)throws SQLException {
        q.setString(1,p.getNom().strip());q.setString(2,p.getPrenom().strip());
        if(p.getDateNaissance()==null||p.getDateNaissance().isBlank())q.setNull(3,Types.DATE);else q.setDate(3,Date.valueOf(p.getDateNaissance()));
        q.setString(4,p.getSexe());q.setString(5,p.getTelephone());q.setString(6,p.getEmail());q.setString(7,p.getAdresse());q.setString(8,p.getGroupeSanguin());q.setString(9,p.getAllergies());
    }
    public static void validatePatient(Patient p) {
        if(p==null||p.getNom()==null||p.getNom().isBlank()||p.getPrenom()==null||p.getPrenom().isBlank())throw new IllegalArgumentException("Le nom et le prénom sont obligatoires.");
        String[] values={p.getNom(),p.getPrenom(),p.getSexe(),p.getTelephone(),p.getEmail(),p.getAdresse(),p.getGroupeSanguin(),p.getAllergies()};
        int[] limits={255,255,50,100,255,255,50,10000};
        for(int i=0;i<values.length;i++)if(values[i]!=null&&(values[i].length()>limits[i]||values[i].indexOf('\0')>=0))throw new IllegalArgumentException("Un champ est trop long ou contient un caractère non valide.");
        if(p.getEmail()!=null&&!p.getEmail().isBlank()&&!p.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Indiquez une adresse email valide ou laissez ce champ vide.");
        if(p.getDateNaissance()!=null&&!p.getDateNaissance().isBlank())try{if(java.time.LocalDate.parse(p.getDateNaissance()).isAfter(java.time.LocalDate.now()))throw new IllegalArgumentException("La date de naissance ne peut pas être future.");}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("La date de naissance est invalide.");}
    }
    private Patient map(ResultSet r)throws SQLException {
        return new Patient(r.getInt("id"),r.getString("nom"),r.getString("prenom"),r.getString("date_naissance"),r.getString("sexe"),r.getString("telephone"),r.getString("email"),r.getString("adresse"),r.getString("groupe_sanguin"),r.getString("allergies"),r.getString("date_inscription"));
    }
}
