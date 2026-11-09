package application.dao;
import application.model.RendezVous;import application.util.*;
import java.sql.*;import java.time.*;import java.util.*;
public final class DashboardDAO {
 public record Snapshot(int patients,int appointments,int today,int completed,int cancelled,int records,Map<String,Integer> statuses,Map<String,Integer> categories,Map<YearMonth,Integer> months,YearMonth firstMonth,List<RendezVous> upcoming){}
 public Snapshot load(int doctorId)throws SQLException{
  RecordAccess.requireDoctor(doctorId);LocalDate today=AppointmentRules.today();YearMonth first=YearMonth.from(today).minusMonths(5);
  Map<String,Integer> statuses=new LinkedHashMap<>();for(String name:List.of("Planifié","Terminé","Annulé","Autres"))statuses.put(name,0);
  Map<String,Integer> categories=new TreeMap<>();Map<YearMonth,Integer> months=new LinkedHashMap<>();for(int i=0;i<12;i++)months.put(first.plusMonths(i),0);
  List<RendezVous> upcoming=new ArrayList<>();int patients,appointments=0,todayCount=0,records=0;
  try(Connection c=DatabaseUtil.getConnection()){
   c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);c.setAutoCommit(false);
   try(PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM patients WHERE id IN (SELECT patient_id FROM patient_medecins WHERE medecin_id=? UNION SELECT patient_id FROM rendez_vous WHERE medecin_id=? UNION SELECT patient_id FROM dossiers_medicaux WHERE medecin_id=?)")){
    for(int i=1;i<=3;i++)q.setInt(i,doctorId);try(ResultSet r=q.executeQuery()){r.next();patients=r.getInt(1);}
   }
   try(PreparedStatement q=c.prepareStatement("SELECT rv.*,CONCAT(p.prenom,' ',p.nom) AS patient_nom FROM rendez_vous rv JOIN patients p ON p.id=rv.patient_id WHERE rv.medecin_id=? ORDER BY date_rdv,heure")){
    q.setInt(1,doctorId);try(ResultSet r=q.executeQuery()){while(r.next()){
     LocalDate date=r.getDate("date_rdv").toLocalDate();String status=AppointmentRules.reportStatus(r.getString("statut"));appointments++;statuses.merge(status,1,Integer::sum);
     if(date.equals(today)&&!status.equals("Annulé"))todayCount++;YearMonth month=YearMonth.from(date);if(months.containsKey(month))months.merge(month,1,Integer::sum);
     if(!status.equals("Annulé")&&!status.equals("Terminé")&&upcoming.size()<5){var item=new RendezVous(r.getInt("id"),r.getInt("patient_id"),doctorId,date.toString(),r.getString("heure"),r.getString("motif"),AppointmentRules.canonicalStatus(r.getString("statut")),r.getString("patient_nom"),"");try{if(!AppointmentRules.start(item).isBefore(AppointmentRules.now()))upcoming.add(item);}catch(IllegalArgumentException ignored){}}
    }}
   }
   try(PreparedStatement q=c.prepareStatement("SELECT COALESCE(NULLIF(TRIM(categorie),''),'Non renseignée') AS categorie,COUNT(*) AS nombre FROM dossiers_medicaux WHERE medecin_id=? GROUP BY COALESCE(NULLIF(TRIM(categorie),''),'Non renseignée')")){
    q.setInt(1,doctorId);try(ResultSet r=q.executeQuery()){while(r.next()){int count=r.getInt(2);records+=count;categories.put(r.getString(1),count);}}
   }
   c.commit();
  }
  return new Snapshot(patients,appointments,todayCount,statuses.get("Terminé"),statuses.get("Annulé"),records,Map.copyOf(statuses),Map.copyOf(categories),Map.copyOf(months),first,List.copyOf(upcoming));
 }
}