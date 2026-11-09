package application.controller;
import application.App;import application.dao.RendezVousDAO;import application.model.RendezVous;import application.util.*;
import javafx.fxml.*;import javafx.scene.control.*;
public class MedecinRdvController {
 @FXML private TableView<RendezVous> tableRdv;
 @FXML private TableColumn<RendezVous,String> colDate,colHeure,colPatient,colMotif,colStatut;
 @FXML private Label lblTotal,messageLabel;
 @FXML private Button editButton,cancelButton;
 @FXML private void initialize(){
  colDate.setCellValueFactory(c->c.getValue().dateRdvProperty());colHeure.setCellValueFactory(c->c.getValue().heureProperty());colPatient.setCellValueFactory(c->c.getValue().patientNomProperty());colMotif.setCellValueFactory(c->c.getValue().motifProperty());colStatut.setCellValueFactory(c->c.getValue().statutProperty());
  tableRdv.getSelectionModel().selectedItemProperty().addListener((o,a,b)->{editButton.setDisable(b==null);cancelButton.setDisable(b==null||AppointmentRules.reportStatus(b.getStatut()).equals("Annulé"));});refresh();
 }
 @FXML public void refresh(){try{tableRdv.setItems(new RendezVousDAO().getRendezVousByMedecin(SessionManager.getMedecinId()));lblTotal.setText("Total : "+tableRdv.getItems().size()+" rendez-vous");editButton.setDisable(true);cancelButton.setDisable(true);}catch(java.sql.SQLException e){tableRdv.getItems().clear();messageLabel.setText("Chargement indisponible. Vérifiez la configuration locale.");}}
 @FXML private void addAppointment(){open(null);}
 @FXML private void editAppointment(){var r=tableRdv.getSelectionModel().getSelectedItem();if(r!=null)open(r.getId());}
 @FXML private void cancelAppointment(){var r=tableRdv.getSelectionModel().getSelectedItem();if(r==null)return;var confirm=new Alert(Alert.AlertType.CONFIRMATION,"Annuler ce rendez-vous ? Cette action conserve son historique.",ButtonType.YES,ButtonType.NO);confirm.setTitle("Annuler un rendez-vous");confirm.setHeaderText(null);confirm.getButtonTypes().setAll(new ButtonType("Oui, annuler",ButtonBar.ButtonData.YES),new ButtonType("Retour",ButtonBar.ButtonData.NO));confirm.showAndWait().filter(b->b.getButtonData()==ButtonBar.ButtonData.YES).ifPresent(b->performCancellation(r.getId()));}
 private void performCancellation(int id){try{new RendezVousDAO().cancelRendezVous(id);refresh();messageLabel.setText("Rendez-vous annulé.");}catch(java.sql.SQLException|IllegalArgumentException e){messageLabel.setText("Annulation impossible. Vérifiez vos droits ou réessayez.");}}
 private void open(Integer id){try{createAppointmentDialog(id).showAndWait();}catch(Exception e){messageLabel.setText("Formulaire indisponible. Vérifiez vos droits ou la configuration locale.");}}
 private Dialog<ButtonType> createAppointmentDialog(Integer id)throws java.io.IOException,java.sql.SQLException{
  var loader=new FXMLLoader(App.class.getResource("/view/medecin_rdv_dialog.fxml"));DialogPane pane=loader.load();AppointmentFormController controller=loader.getController();controller.configure(id);
  var dialog=new Dialog<ButtonType>();dialog.setTitle(id==null?"Nouveau rendez-vous":"Modifier / reprogrammer");dialog.setDialogPane(pane);pane.getStylesheets().add(App.class.getResource("/view/css/style.css").toExternalForm());
  var save=pane.getButtonTypes().stream().filter(b->b.getButtonData()==ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow();pane.lookupButton(save).addEventFilter(javafx.event.ActionEvent.ACTION,e->{if(!controller.save())e.consume();else{refresh();messageLabel.setText("Rendez-vous enregistré.");}});return dialog;
 }
}