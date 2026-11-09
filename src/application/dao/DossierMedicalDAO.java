package application.dao;

import application.model.DossierMedical;
import application.util.*;
import javafx.collections.*;
import java.sql.*;

public class DossierMedicalDAO {
    public void addDossier(DossierMedical d)throws SQLException {
        RecordAccess.requireDoctor(d.getMedecinId()); validate(d);
        try(Connection c=DatabaseUtil.getConnection()){
            RecordAccess.requirePatient(c,d.getPatientId());
            try(PreparedStatement q=c.prepareStatement("INSERT INTO dossiers_medicaux(patient_id,date_consultation,diagnostic,traitement,notes,medecin_id,maladie,antecedents,categorie) VALUES(?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){bind(q,d);q.executeUpdate();try(ResultSet keys=q.getGeneratedKeys()){if(keys.next())d.setId(keys.getInt(1));}DataChanges.publish();}
        }
    }
    public void updateDossier(DossierMedical d)throws SQLException {
        RecordAccess.requireDoctor(d.getMedecinId()); validate(d);
        try(Connection c=DatabaseUtil.getConnection()){
            RecordAccess.requirePatient(c,d.getPatientId());
            try(PreparedStatement q=c.prepareStatement("UPDATE dossiers_medicaux SET patient_id=?,date_consultation=?,diagnostic=?,traitement=?,notes=?,medecin_id=?,maladie=COALESCE(?,maladie),antecedents=COALESCE(?,antecedents),categorie=COALESCE(?,categorie) WHERE id=? AND "+RecordAccess.ownerPredicate("dossiers_medicaux")+(SessionManager.isAdminLoggedIn()?"":" AND patient_id=?"))){
                bind(q,d);q.setInt(10,d.getId());int next=RecordAccess.bindOwner(q,11);if(!SessionManager.isAdminLoggedIn())q.setInt(next,d.getPatientId());if(q.executeUpdate()!=1)throw new SQLException("Dossier introuvable ou accès non autorisé."); DataChanges.publish();
            }
        }
    }
    public void deleteDossier(int id)throws SQLException {
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("DELETE FROM dossiers_medicaux WHERE id=? AND "+RecordAccess.ownerPredicate("dossiers_medicaux"))){
            q.setInt(1,id);RecordAccess.bindOwner(q,2);if(q.executeUpdate()!=1)throw new SQLException("Dossier introuvable ou accès non autorisé."); DataChanges.publish();
        }
    }
    public DossierMedical getDossierById(int id)throws SQLException{var rows=select("dm.id=?",id);return rows.isEmpty()?null:rows.getFirst();}
    public ObservableList<DossierMedical> getAllDossiers()throws SQLException{return select("1=1");}
    public ObservableList<DossierMedical> getDossiersByPatient(int id)throws SQLException{return select("dm.patient_id=?",id);}
    public ObservableList<DossierMedical> getDossiersByMedecin(int id)throws SQLException{RecordAccess.requireDoctor(id);return select("dm.medecin_id=?",id);}
    public int getTotalDossiers()throws SQLException{return count("1=1");}
    public int countDossiersByMedecin(int id)throws SQLException{RecordAccess.requireDoctor(id);return count("dm.medecin_id=?",id);}
    private int count(String where,Object...args)throws SQLException{
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM dossiers_medicaux dm WHERE ("+where+") AND "+RecordAccess.ownerPredicate("dm"))){
            int i=1;for(Object value:args)q.setObject(i++,value);RecordAccess.bindOwner(q,i);try(ResultSet rs=q.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }
    private ObservableList<DossierMedical> select(String where,Object...args)throws SQLException {
        var rows=FXCollections.<DossierMedical>observableArrayList();
        String sql="SELECT dm.*,CONCAT(p.nom,' ',p.prenom) patient_nom,CONCAT(m.nom,' ',m.prenom) medecin_nom FROM dossiers_medicaux dm JOIN patients p ON dm.patient_id=p.id LEFT JOIN medecins m ON dm.medecin_id=m.id WHERE ("+where+") AND "+RecordAccess.ownerPredicate("dm")+" ORDER BY dm.date_consultation DESC";
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement(sql)){
            int i=1;for(Object value:args)q.setObject(i++,value);RecordAccess.bindOwner(q,i);
            try(ResultSet rs=q.executeQuery()){while(rs.next()){var d=new DossierMedical(rs.getInt("id"),rs.getInt("patient_id"),rs.getString("date_consultation"),rs.getString("diagnostic"),rs.getString("traitement"),rs.getString("notes"),rs.getInt("medecin_id"),rs.getString("patient_nom"),rs.getString("medecin_nom"));d.setMaladie(rs.getString("maladie"));d.setAntecedents(rs.getString("antecedents"));d.setCategorie(rs.getString("categorie"));rows.add(d);};}
        }return rows;
    }
    private void bind(PreparedStatement q,DossierMedical d)throws SQLException {
        q.setInt(1,d.getPatientId());q.setString(2,d.getDateConsultation());q.setString(3,d.getDiagnostic());q.setString(4,d.getTraitement());q.setString(5,d.getNotes());q.setInt(6,d.getMedecinId());q.setString(7,d.getMaladie());q.setString(8,d.getAntecedents());q.setString(9,d.getCategorie());
    }
    public static final java.util.List<String> CATEGORIES=java.util.List.of("Non renseignée","Cardiovasculaire","Respiratoire","Métabolique / endocrinienne","Neurologique","Infectieuse","Autre");
    private void validate(DossierMedical d){
        try{if(java.time.LocalDate.parse(d.getDateConsultation()).isAfter(AppointmentRules.today()))throw new IllegalArgumentException();}catch(RuntimeException e){throw new IllegalArgumentException("La date du diagnostic est invalide ou future.");}
        if((d.getDiagnostic()==null||d.getDiagnostic().isBlank())&&(d.getMaladie()==null||d.getMaladie().isBlank()))throw new IllegalArgumentException("Indiquez une maladie ou un diagnostic.");
        for(String value:new String[]{d.getDiagnostic(),d.getTraitement(),d.getNotes(),d.getAntecedents()})if(value!=null&&(value.length()>10000||value.indexOf('\0')>=0))throw new IllegalArgumentException("Un texte médical est trop long ou invalide.");
        if(d.getMaladie()!=null&&(d.getMaladie().length()>255||d.getMaladie().indexOf('\0')>=0))throw new IllegalArgumentException("La maladie doit contenir au maximum 255 caractères.");
        if(d.getCategorie()!=null&&!d.getCategorie().isBlank()&&!CATEGORIES.contains(d.getCategorie()))throw new IllegalArgumentException("Choisissez une catégorie proposée.");
    }}
