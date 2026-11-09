package application.dao;

import application.model.RendezVous;
import application.util.*;
import javafx.collections.*;
import java.sql.*;

public class RendezVousDAO {

    public void addRendezVous(RendezVous r)throws SQLException{save(r,true);}
    public void updateRendezVous(RendezVous r)throws SQLException{save(r,false);}
    private void save(RendezVous r,boolean create)throws SQLException{
        RecordAccess.requireDoctor(r.getMedecinId());
        int generated=r.getId();
        try(Connection c=DatabaseUtil.getConnection()){
            c.setAutoCommit(false);
            try {
                // Every application writer locks the doctor's row before checking slots.
                try(PreparedStatement q=c.prepareStatement("SELECT id FROM medecins WHERE id=? FOR UPDATE")){q.setInt(1,r.getMedecinId());try(ResultSet rs=q.executeQuery()){if(!rs.next())throw new SQLException("Médecin indisponible.");}}
                boolean rescheduled=false;
                if(!create)try(PreparedStatement q=c.prepareStatement("SELECT date_rdv,heure,medecin_id FROM rendez_vous WHERE id=? AND "+RecordAccess.ownerPredicate("rendez_vous")+" FOR UPDATE")){
                    q.setInt(1,r.getId());RecordAccess.bindOwner(q,2);
                    try(ResultSet rs=q.executeQuery()){
                        if(!rs.next())throw new SQLException("Rendez-vous introuvable ou accès non autorisé.");
                        rescheduled=!rs.getString(1).equals(r.getDateRdv())||!rs.getString(2).equals(r.getHeure())||rs.getInt(3)!=r.getMedecinId();
                        int previous=rs.getInt(3);if(previous!=r.getMedecinId())try(PreparedStatement lock=c.prepareStatement("SELECT id FROM medecins WHERE id=? FOR UPDATE")){lock.setInt(1,previous);lock.executeQuery().close();}
                    }
                }
                AppointmentRules.validate(r,create,rescheduled);
                RecordAccess.requirePatient(c,r.getPatientId());
                if(!r.getStatut().equals("Annulé"))checkConflict(c,r,create);
                String sql=create?"INSERT INTO rendez_vous(patient_id,medecin_id,date_rdv,heure,motif,statut) VALUES(?,?,?,?,?,?)":"UPDATE rendez_vous SET patient_id=?,medecin_id=?,date_rdv=?,heure=?,motif=?,statut=? WHERE id=? AND "+RecordAccess.ownerPredicate("rendez_vous");
                try(PreparedStatement q=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
                    bind(q,r);if(!create){q.setInt(7,r.getId());RecordAccess.bindOwner(q,8);}if(q.executeUpdate()!=1)throw new SQLException("Rendez-vous indisponible.");
                    if(create)try(ResultSet keys=q.getGeneratedKeys()){if(keys.next())generated=keys.getInt(1);}
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
        r.setId(generated);DataChanges.publish();
    }
    private void checkConflict(Connection c,RendezVous r,boolean create)throws SQLException{
        var begin=AppointmentRules.start(r);var end=begin.plusMinutes(AppointmentRules.SLOT_MINUTES);
        try(PreparedStatement q=c.prepareStatement("SELECT date_rdv,heure,statut FROM rendez_vous WHERE medecin_id=? AND date_rdv BETWEEN ? AND ? AND id<>?")){
            q.setInt(1,r.getMedecinId());q.setString(2,begin.toLocalDate().minusDays(1).toString());q.setString(3,begin.toLocalDate().plusDays(1).toString());q.setInt(4,create?-1:r.getId());
            try(ResultSet rs=q.executeQuery()){while(rs.next()){
                if(AppointmentRules.reportStatus(rs.getString(3)).equals("Annulé"))continue;
                java.time.LocalDateTime other;
                try{other=java.time.LocalDate.parse(rs.getString(1)).atTime(java.time.LocalTime.parse(rs.getString(2)));}catch(RuntimeException e){throw new SQLException("Un ancien créneau doit être corrigé avant cette réservation.");}
                if(begin.isBefore(other.plusMinutes(AppointmentRules.SLOT_MINUTES))&&other.isBefore(end))throw new IllegalArgumentException("Ce créneau de 30 minutes chevauche un autre rendez-vous. Choisissez une autre heure.");
            }}
        }
    }
    public RendezVous getRendezVousById(int id)throws SQLException{var rows=select("rv.id=?",id);return rows.isEmpty()?null:rows.getFirst();}
    public void cancelRendezVous(int id)throws SQLException{var r=getRendezVousById(id);if(r==null)throw new SQLException("Rendez-vous indisponible.");r.setStatut("Annulé");updateRendezVous(r);}
    public void deleteRendezVous(int id)throws SQLException{
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("DELETE FROM rendez_vous WHERE id=? AND "+RecordAccess.ownerPredicate("rendez_vous"))){
            q.setInt(1,id);RecordAccess.bindOwner(q,2);if(q.executeUpdate()!=1)throw new SQLException("Rendez-vous introuvable ou accès non autorisé.");
        }DataChanges.publish();
    }
    public ObservableList<RendezVous> getAllRendezVous()throws SQLException{return select("1=1");}
    public ObservableList<RendezVous> getRendezVousByDate(String date)throws SQLException{return select("rv.date_rdv=?",date);}
    public ObservableList<RendezVous> getRendezVousByMedecin(int id)throws SQLException{RecordAccess.requireDoctor(id);return select("rv.medecin_id=?",id);}
    public int getTotalRendezVous()throws SQLException{return count("1=1");}
    public int getRendezVousToday()throws SQLException{return count("rv.date_rdv=?",AppointmentRules.today().toString());}
    public int countRendezVousByMedecin(int id)throws SQLException{RecordAccess.requireDoctor(id);return count("rv.medecin_id=?",id);}
    public int countRendezVousTodayByMedecin(int id)throws SQLException{RecordAccess.requireDoctor(id);return count("rv.medecin_id=? AND rv.date_rdv=?",id,AppointmentRules.today().toString());}
    private int count(String where,Object...args)throws SQLException{
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM rendez_vous rv WHERE ("+where+") AND "+RecordAccess.ownerPredicate("rv"))){
            int i=1;for(Object value:args)q.setObject(i++,value);RecordAccess.bindOwner(q,i);try(ResultSet rs=q.executeQuery()){rs.next();return rs.getInt(1);}
        }
    }
    private ObservableList<RendezVous> select(String where,Object...args)throws SQLException{
        var rows=FXCollections.<RendezVous>observableArrayList();
        String sql="SELECT rv.*,CONCAT(p.nom,' ',p.prenom) patient_nom,CONCAT(m.nom,' ',m.prenom) medecin_nom FROM rendez_vous rv JOIN patients p ON rv.patient_id=p.id JOIN medecins m ON rv.medecin_id=m.id WHERE ("+where+") AND "+RecordAccess.ownerPredicate("rv")+" ORDER BY rv.date_rdv DESC,rv.heure";
        try(Connection c=DatabaseUtil.getConnection();PreparedStatement q=c.prepareStatement(sql)){
            int i=1;for(Object value:args)q.setObject(i++,value);RecordAccess.bindOwner(q,i);
            try(ResultSet rs=q.executeQuery()){while(rs.next())rows.add(new RendezVous(rs.getInt("id"),rs.getInt("patient_id"),rs.getInt("medecin_id"),rs.getString("date_rdv"),rs.getString("heure"),rs.getString("motif"),rs.getString("statut"),rs.getString("patient_nom"),rs.getString("medecin_nom")));}
        }return rows;
    }
    private void bind(PreparedStatement q,RendezVous r)throws SQLException{
        q.setInt(1,r.getPatientId());q.setInt(2,r.getMedecinId());q.setString(3,r.getDateRdv());q.setString(4,r.getHeure());q.setString(5,r.getMotif());q.setString(6,r.getStatut());
    }
}