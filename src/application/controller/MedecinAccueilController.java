package application.controller;
import application.dao.DashboardDAO;import application.dao.DashboardDAO.Snapshot;import application.model.RendezVous;import application.util.*;
import javafx.fxml.FXML;import javafx.scene.chart.*;import javafx.scene.control.*;import javafx.scene.layout.*;import javafx.scene.Scene;import javafx.stage.Window;import javafx.beans.value.ChangeListener;
import javafx.animation.*;import javafx.util.Duration;import javafx.concurrent.Task;import javafx.collections.FXCollections;
import java.time.format.DateTimeFormatter;import java.util.*;
public class MedecinAccueilController {
 @FXML private VBox dashboardRoot;
 @FXML private Label lblRdvTotal,lblRdvToday,lblPatients,lblDossiers,lblCompleted,lblCancelled,statusTotal,loadMessage,statusEmpty,conditionEmpty,monthEmpty,monthRange,statusLegend;
 @FXML private PieChart statusChart,conditionChart;
 @FXML private FlowPane conditionLegend;
 @FXML private TilePane statsGrid,chartsGrid;
 @FXML private BarChart<String,Number> monthChart;
 @FXML private TableView<RendezVous> tableRdvProchains;
 @FXML private TableColumn<RendezVous,String> colDate,colHeure,colPatient,colMotif,colStatut;
 private boolean loading,again;private AutoCloseable subscription;private Scene observedScene;private Window observedWindow;
 private final Timeline poll=new Timeline(new KeyFrame(Duration.seconds(30),e->refresh()));
 private final ChangeListener<Window> windowListener=(o,a,b)->bindWindow(b);
 private final ChangeListener<Boolean> visibleListener=(o,a,b)->visibility(b);
 @FXML private void initialize(){
  colDate.setCellValueFactory(c->c.getValue().dateRdvProperty());colHeure.setCellValueFactory(c->c.getValue().heureProperty());colPatient.setCellValueFactory(c->c.getValue().patientNomProperty());colMotif.setCellValueFactory(c->c.getValue().motifProperty());colStatut.setCellValueFactory(c->c.getValue().statutProperty());
  statusChart.setAnimated(false);conditionChart.setAnimated(false);monthChart.setAnimated(false);poll.setCycleCount(Timeline.INDEFINITE);
  dashboardRoot.widthProperty().addListener((o,a,b)->layoutCards(b.doubleValue()));
  dashboardRoot.sceneProperty().addListener((o,a,b)->bindScene(b));refresh();
 }
 private void layoutCards(double width){
  // Leave room for pixel rounding so TilePane does not wrap the last column.
  int stats=width>=780?3:width>=500?2:1;
  statsGrid.setPrefColumns(stats);statsGrid.setPrefTileWidth(Math.max(220,Math.floor((width-14*(stats-1)-2)/stats)));
  int charts=width>=720?2:1;
  chartsGrid.setPrefColumns(charts);chartsGrid.setPrefTileWidth(Math.max(260,Math.floor((width-14*(charts-1)-2)/charts)));
 }
 private void bindScene(Scene scene){if(observedScene!=null)observedScene.windowProperty().removeListener(windowListener);bindWindow(null);observedScene=scene;if(scene!=null){scene.windowProperty().addListener(windowListener);bindWindow(scene.getWindow());}}
 private void bindWindow(Window window){if(observedWindow!=null)observedWindow.showingProperty().removeListener(visibleListener);visibility(false);observedWindow=window;if(window!=null){window.showingProperty().addListener(visibleListener);visibility(window.isShowing());}}
 private void visibility(boolean visible){if(visible){if(subscription==null)subscription=DataChanges.subscribe(this::refresh);poll.play();refresh();}else{poll.stop();if(subscription!=null)try{subscription.close();}catch(Exception ignored){}subscription=null;}}
 @FXML public void refresh(){
  if(!SessionManager.isMedecinLoggedIn())return;if(loading){again=true;return;}loading=true;int doctor=SessionManager.getMedecinId();loadMessage.setText("Actualisation des données…");
  Task<Snapshot> task=new Task<>(){@Override protected Snapshot call()throws Exception{return new DashboardDAO().load(doctor);}};
  task.setOnSucceeded(e->{loading=false;if(SessionManager.isMedecinLoggedIn()&&SessionManager.getMedecinId()==doctor&&dashboardRoot.getScene()!=null)apply(task.getValue());if(again){again=false;refresh();}});
  task.setOnFailed(e->{loading=false;if(SessionManager.isMedecinLoggedIn()&&SessionManager.getMedecinId()==doctor){clear();loadMessage.setText("Données indisponibles. Vérifiez vos droits et la configuration locale.");}if(again){again=false;refresh();}});
  Thread thread=new Thread(task,"medical-dashboard");thread.setDaemon(true);thread.start();
 }
 private void apply(Snapshot s){
  lblPatients.setText(""+s.patients());lblRdvTotal.setText(""+s.appointments());lblRdvToday.setText(""+s.today());lblCompleted.setText(""+s.completed());lblCancelled.setText(""+s.cancelled());lblDossiers.setText(""+s.records());statusTotal.setText(""+s.appointments());
  var statuses=new LinkedHashMap<String,Integer>();for(String k:List.of("Planifié","Terminé","Annulé","Autres"))statuses.put(k,s.statuses().getOrDefault(k,0));pie(statusChart,statuses);pie(conditionChart,new TreeMap<>(s.categories()));
  statusLegend.setText("Actifs : "+statuses.get("Planifié")+"   ·   Terminés : "+s.completed()+"   ·   Annulés : "+s.cancelled()+(statuses.get("Autres")>0?"   ·   Autres : "+statuses.get("Autres"):""));
  statusEmpty.setVisible(s.appointments()==0);statusEmpty.setManaged(s.appointments()==0);conditionEmpty.setVisible(s.records()==0);conditionEmpty.setManaged(s.records()==0);
  var series=new XYChart.Series<String,Number>();series.setName("Rendez-vous");var format=DateTimeFormatter.ofPattern("MMM yy",Locale.FRENCH);int inRange=0;
  for(int i=0;i<12;i++){var month=s.firstMonth().plusMonths(i);int count=s.months().getOrDefault(month,0);inRange+=count;series.getData().add(new XYChart.Data<>(month.format(format),count));}
  monthChart.getData().setAll(series);int max=s.months().values().stream().mapToInt(Integer::intValue).max().orElse(0);var axis=(NumberAxis)monthChart.getYAxis();axis.setAutoRanging(false);axis.setLowerBound(0);axis.setUpperBound(Math.max(1,max));axis.setTickUnit(Math.max(1,Math.ceil(max/5.0)));monthRange.setText("De "+s.firstMonth().format(format)+" à "+s.firstMonth().plusMonths(11).format(format)+" · tous statuts");monthEmpty.setVisible(inRange==0);monthEmpty.setManaged(inRange==0);
  tableRdvProchains.setItems(FXCollections.observableArrayList(s.upcoming()));loadMessage.setText("Données de votre activité · actualisation automatique toutes les 30 secondes · "+AppointmentRules.zone());
 }
 private void pie(PieChart chart,Map<String,Integer> counts){
  if(chart==conditionChart)conditionLegend.getChildren().clear();
  var data=FXCollections.<PieChart.Data>observableArrayList();String[] colors={"#2980b9","#27ae60","#e67e22","#8e7cc3","#16a085","#64748b"};int i=0;
  for(var entry:counts.entrySet())if(entry.getValue()>0){var d=new PieChart.Data(entry.getKey(),entry.getValue());String color=chart==statusChart?switch(entry.getKey()){case "Terminé"->"#27ae60";case "Annulé"->"#e67e22";case "Planifié"->"#2980b9";default->"#8e7cc3";}:colors[i++%colors.length];d.nodeProperty().addListener((o,a,b)->{if(b!=null){b.setStyle("-fx-pie-color: "+color+";");Tooltip.install(b,new Tooltip(entry.getKey()+" : "+entry.getValue()));}});data.add(d);if(chart==conditionChart){var dot=new javafx.scene.shape.Circle(5,javafx.scene.paint.Color.web(color));var label=new Label(entry.getKey()+" : "+entry.getValue());label.getStyleClass().add("medical-muted");var row=new HBox(6,dot,label);row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);conditionLegend.getChildren().add(row);}}
  chart.setData(data);
 }
 private void clear(){for(Label label:List.of(lblRdvTotal,lblRdvToday,lblPatients,lblDossiers,lblCompleted,lblCancelled,statusTotal))label.setText("—");statusChart.getData().clear();conditionChart.getData().clear();conditionLegend.getChildren().clear();monthChart.getData().clear();tableRdvProchains.getItems().clear();statusLegend.setText("");for(Label label:List.of(statusEmpty,conditionEmpty,monthEmpty)){label.setVisible(false);label.setManaged(false);}}
}
