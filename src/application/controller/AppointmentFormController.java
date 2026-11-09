package application.controller;
import application.dao.*;import application.model.*;import application.util.*;
import javafx.fxml.FXML;import javafx.scene.control.*;
public final class AppointmentFormController {
 @FXML private ComboBox<Patient> patientField;
 @FXML private DatePicker dateField;
 @FXML private ComboBox<String> timeField,statusField;
 @FXML private TextArea reasonField;
 @FXML private Label errorLabel;
 private int recordId;private boolean saved;
 @FXML private void initialize()throws java.sql.SQLException{
  FrenchDateFields.configure(dateField);
  patientField.setConverter(new javafx.util.StringConverter<Patient>(){
   @Override public String toString(Patient patient){return patient==null?"":patient.getNomComplet();}
   @Override public Patient fromString(String value){throw new UnsupportedOperationException("Sélectionnez un patient dans la liste.");}
  });
  patientField.setItems(new PatientDAO().getPatientsByMedecin(SessionManager.getMedecinId()));
  patientField.setCellFactory(v->new ListCell<>(){@Override protected void updateItem(Patient p,boolean empty){super.updateItem(p,empty);setText(empty||p==null?null:p.getNomComplet());}});
  patientField.setButtonCell(new ListCell<>(){@Override protected void updateItem(Patient p,boolean empty){super.updateItem(p,empty);setText(empty||p==null?null:p.getNomComplet());}});
  for(int h=8;h<=18;h++){timeField.getItems().add(String.format("%02d:00",h));if(h<18)timeField.getItems().add(String.format("%02d:30",h));}
  timeField.setValue("09:00");dateField.setValue(AppointmentRules.today().plusDays(1));statusField.getItems().setAll("Planifié","Terminé","Annulé");statusField.setValue("Planifié");statusField.setDisable(true);
 }
 public void configure(Integer id)throws java.sql.SQLException{
  if(id==null)return;var r=new RendezVousDAO().getRendezVousById(id);if(r==null)throw new java.sql.SQLException("Rendez-vous indisponible.");
  recordId=id;patientField.setValue(patientField.getItems().stream().filter(p->p.getId()==r.getPatientId()).findFirst().orElseThrow(()->new java.sql.SQLException("Patient indisponible.")));
  dateField.setValue(java.time.LocalDate.parse(r.getDateRdv()));timeField.setValue(r.getHeure());reasonField.setText(r.getMotif());statusField.setDisable(false);statusField.setValue(AppointmentRules.canonicalStatus(r.getStatut()));
 }
 public boolean save(){
  if(saved)return true;
  try{
   try{dateField.commitValue();}catch(RuntimeException e){throw new IllegalArgumentException("La date est invalide.");}
   if(patientField.getValue()==null||dateField.getValue()==null)throw new IllegalArgumentException("Sélectionnez un patient et une date.");
   var r=new RendezVous();r.setId(recordId);r.setPatientId(patientField.getValue().getId());r.setMedecinId(SessionManager.getMedecinId());r.setDateRdv(dateField.getValue().toString());
   r.setHeure(timeField.getEditor().getText().strip());r.setMotif(reasonField.getText());r.setStatut(recordId==0?"Planifié":statusField.getValue());
   var dao=new RendezVousDAO();if(recordId==0)dao.addRendezVous(r);else dao.updateRendezVous(r);saved=true;return true;
  }catch(IllegalArgumentException e){error(e.getMessage());}catch(java.sql.SQLException|IllegalStateException e){error("Enregistrement indisponible. Vérifiez vos droits et la configuration locale, puis réessayez.");}
  return false;
 }
 private void error(String text){errorLabel.setText(text);errorLabel.setManaged(true);errorLabel.setVisible(true);}
}
