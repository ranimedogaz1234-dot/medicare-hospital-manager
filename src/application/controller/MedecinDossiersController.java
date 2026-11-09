package application.controller;
import application.App;import application.dao.*;import application.model.*;import application.util.*;
import javafx.fxml.*;import javafx.scene.control.*;import javafx.beans.property.ReadOnlyStringWrapper;
public class MedecinDossiersController {
 @FXML private TextField searchField;
 @FXML private TableView<Patient> patientTable;
 @FXML private TableColumn<Patient,String> patientNameColumn,patientPhoneColumn;
 @FXML private TableView<DossierMedical> tableDossiers;
 @FXML private TableColumn<DossierMedical,String> colDate,colDisease,colCategory,colDiagnostic;
 @FXML private Label patientInfo,lblTotal,messageLabel;
 @FXML private TextArea recordDetails;
 @FXML private Button newRecordButton,editRecordButton;
 @FXML private void initialize(){
  patientNameColumn.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getNomComplet()));patientPhoneColumn.setCellValueFactory(c->c.getValue().telephoneProperty());
  colDate.setCellValueFactory(c->c.getValue().dateConsultationProperty());colDisease.setCellValueFactory(c->new ReadOnlyStringWrapper(text(c.getValue().getMaladie())));
  colCategory.setCellValueFactory(c->new ReadOnlyStringWrapper(text(c.getValue().getCategorie())));colDiagnostic.setCellValueFactory(c->c.getValue().diagnosticProperty());
  patientTable.getSelectionModel().selectedItemProperty().addListener((o,a,b)->loadRecords());tableDossiers.getSelectionModel().selectedItemProperty().addListener((o,a,b)->showDetails());
  searchField.textProperty().addListener((o,a,b)->refresh());refresh();
 }
 @FXML public void refresh(){
  try{int selected=patientTable.getSelectionModel().getSelectedItem()==null?0:patientTable.getSelectionModel().getSelectedItem().getId();var dao=new PatientDAO();patientTable.setItems(searchField.getText().isBlank()?dao.getAllPatients():dao.searchPatients(searchField.getText().strip()));
   patientTable.getSelectionModel().select(patientTable.getItems().stream().filter(p->p.getId()==selected).findFirst().orElse(patientTable.getItems().isEmpty()?null:patientTable.getItems().getFirst()));loadRecords();
  }catch(java.sql.SQLException e){patientTable.getItems().clear();tableDossiers.getItems().clear();message("Chargement indisponible. Vérifiez la configuration locale.");}
 }
 private void loadRecords(){
  var p=patientTable.getSelectionModel().getSelectedItem();newRecordButton.setDisable(p==null);tableDossiers.getItems().clear();recordDetails.clear();editRecordButton.setDisable(true);
  if(p==null){patientInfo.setText("Sélectionnez un patient autorisé.");lblTotal.setText("Aucun dossier sélectionné.");return;}
  try{var actual=new PatientDAO().getPatientById(p.getId());if(actual==null)throw new java.sql.SQLException();patientInfo.setText(actual.getNomComplet()+" · Naissance : "+text(actual.getDateNaissance())+" · Téléphone : "+text(actual.getTelephone()));tableDossiers.setItems(new DossierMedicalDAO().getDossiersByPatient(p.getId()));lblTotal.setText(tableDossiers.getItems().size()+" dossier(s) pour ce patient");if(!tableDossiers.getItems().isEmpty())tableDossiers.getSelectionModel().selectFirst();}
  catch(java.sql.SQLException e){message("Dossiers indisponibles. Vérifiez vos droits ou la configuration locale.");}
 }
 private void showDetails(){var d=tableDossiers.getSelectionModel().getSelectedItem();editRecordButton.setDisable(d==null);recordDetails.setText(d==null?"Sélectionnez un dossier pour consulter son contenu.":"Maladie : "+text(d.getMaladie())+"\nCatégorie : "+text(d.getCategorie())+"\nDate du diagnostic : "+d.getDateConsultation()+"\n\nAntécédents médicaux :\n"+text(d.getAntecedents())+"\n\nDiagnostic :\n"+text(d.getDiagnostic())+"\n\nTraitement :\n"+text(d.getTraitement())+"\n\nNotes :\n"+text(d.getNotes()));}
 @FXML private void addRecord(){open(null);}
 @FXML private void editRecord(){var d=tableDossiers.getSelectionModel().getSelectedItem();if(d!=null)open(d.getId());}
 private void open(Integer id){try{var dialog=createRecordDialog(id);if(dialog!=null)dialog.showAndWait();}catch(Exception e){message("Impossible d'ouvrir ce dossier. Vérifiez vos droits.");}}
 private Dialog<ButtonType> createRecordDialog(Integer id)throws java.io.IOException,java.sql.SQLException{
  var p=patientTable.getSelectionModel().getSelectedItem();if(p==null)return null;
  var loader=new FXMLLoader(App.class.getResource("/view/medecin_dossier_dialog.fxml"));DialogPane pane=loader.load();MedicalRecordFormController controller=loader.getController();controller.configure(p.getId(),id);
  var dialog=new Dialog<ButtonType>();dialog.setTitle(id==null?"Nouveau dossier médical":"Modifier le dossier médical");dialog.setDialogPane(pane);pane.getStylesheets().add(App.class.getResource("/view/css/style.css").toExternalForm());
  var save=pane.getButtonTypes().stream().filter(b->b.getButtonData()==ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();pane.lookupButton(save).addEventFilter(javafx.event.ActionEvent.ACTION,e->{if(!controller.save())e.consume();else{loadRecords();message("Dossier médical enregistré.");}});return dialog;
 }
 private void message(String text){messageLabel.setText(text);}
 private static String text(String value){return value==null||value.isBlank()?"Non renseigné":value;}
}