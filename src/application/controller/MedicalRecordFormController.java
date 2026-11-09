package application.controller;
import application.dao.*;import application.model.*;import application.util.*;
import javafx.fxml.FXML;import javafx.scene.control.*;
public final class MedicalRecordFormController {
 @FXML private Label patientLabel,errorLabel;
 @FXML private TextField diseaseField;
 @FXML private ComboBox<String> categoryField;
 @FXML private DatePicker diagnosisDate;
 @FXML private TextArea historyField,diagnosisField,treatmentField,notesField;
 private int patientId,recordId;private boolean saved;
 @FXML private void initialize(){FrenchDateFields.configure(diagnosisDate);categoryField.getItems().setAll(DossierMedicalDAO.CATEGORIES);categoryField.setValue("Non renseignée");diagnosisDate.setValue(AppointmentRules.today());}
 public void configure(int id,Integer editId)throws java.sql.SQLException{
  Patient p=new PatientDAO().getPatientById(id);if(p==null)throw new java.sql.SQLException("Accès non autorisé.");
  patientId=id;patientLabel.setText(p.getNomComplet());
  if(editId!=null){DossierMedical d=new DossierMedicalDAO().getDossierById(editId);if(d==null||d.getPatientId()!=id)throw new java.sql.SQLException("Dossier indisponible.");
   recordId=d.getId();diseaseField.setText(d.getMaladie());categoryField.setValue(d.getCategorie()==null||d.getCategorie().isBlank()?"Non renseignée":d.getCategorie());
   diagnosisDate.setValue(java.time.LocalDate.parse(d.getDateConsultation()));historyField.setText(d.getAntecedents());diagnosisField.setText(d.getDiagnostic());treatmentField.setText(d.getTraitement());notesField.setText(d.getNotes());
  }
 }
 public boolean save(){
  if(saved)return true;
  try{
   try{diagnosisDate.commitValue();}catch(RuntimeException e){throw new IllegalArgumentException("La date du diagnostic est invalide.");}
   if(diagnosisDate.getValue()==null)throw new IllegalArgumentException("Indiquez la date du diagnostic.");
   var d=new DossierMedical();d.setId(recordId);d.setPatientId(patientId);d.setMedecinId(SessionManager.getMedecinId());d.setDateConsultation(diagnosisDate.getValue().toString());
   d.setMaladie(diseaseField.getText().strip());d.setCategorie(categoryField.getValue());d.setAntecedents(historyField.getText());d.setDiagnostic(diagnosisField.getText());d.setTraitement(treatmentField.getText());d.setNotes(notesField.getText());
   var dao=new DossierMedicalDAO();if(recordId==0)dao.addDossier(d);else dao.updateDossier(d);saved=true;return true;
  }catch(IllegalArgumentException e){error(e.getMessage());}catch(java.sql.SQLException|IllegalStateException e){error("Enregistrement indisponible. Vérifiez vos droits et la configuration locale, puis réessayez.");}
  return false;
 }
 private void error(String value){errorLabel.setText(value);errorLabel.setVisible(true);errorLabel.setManaged(true);}
}
