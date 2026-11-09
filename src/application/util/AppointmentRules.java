package application.util;
import application.model.RendezVous;
import java.time.*;
import java.text.Normalizer;
import java.util.Locale;
public final class AppointmentRules {
 public static final int SLOT_MINUTES=30;
 private AppointmentRules(){}
 public static ZoneId zone(){String value=System.getenv("HOSPITAL_TIME_ZONE");return ZoneId.of(value==null||value.isBlank()?"Africa/Tunis":value);}
 public static LocalDate today(){return LocalDate.now(zone());}
 public static LocalDateTime now(){return LocalDateTime.now(zone());}
 public static String canonicalStatus(String raw){
  String s=Normalizer.normalize(raw==null?"":raw,Normalizer.Form.NFD).replaceAll("\\p{M}","").strip().toLowerCase(Locale.ROOT);
  return switch(s){case "planifie"->"Planifié";case "confirme"->"Confirmé";case "en cours"->"En cours";case "termine","effectue"->"Terminé";case "annule"->"Annulé";default->"Autres";};
 }
 public static String reportStatus(String raw){String s=canonicalStatus(raw);return s.equals("Confirmé")||s.equals("En cours")?"Planifié":s;}
 public static LocalDateTime start(RendezVous r){
  try{if(r.getHeure()==null||!r.getHeure().matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))throw new IllegalArgumentException();return LocalDate.parse(r.getDateRdv()).atTime(LocalTime.parse(r.getHeure()));}
  catch(RuntimeException e){throw new IllegalArgumentException("Indiquez une date et une heure valides (HH:mm).");}
 }
 public static void validate(RendezVous r,boolean create,boolean rescheduled){
  if(r.getPatientId()<=0||r.getMedecinId()<=0)throw new IllegalArgumentException("Sélectionnez un patient autorisé.");
  LocalDateTime when=start(r);String status=canonicalStatus(r.getStatut());
  if(status.equals("Autres"))throw new IllegalArgumentException("Choisissez Planifié, Terminé ou Annulé.");
  if((create||rescheduled||(!status.equals("Terminé")&&!status.equals("Annulé")))&&!when.isAfter(now()))throw new IllegalArgumentException("Le rendez-vous doit être programmé dans le futur.");
  if(r.getMotif()!=null&&(r.getMotif().length()>2000||r.getMotif().indexOf('\0')>=0))throw new IllegalArgumentException("Le motif est trop long ou invalide.");
  r.setStatut(status);
 }
}
