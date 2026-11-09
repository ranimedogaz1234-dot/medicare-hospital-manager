package application.model;

import javafx.beans.property.*;

public class RendezVous {
    private final IntegerProperty id;
    private final IntegerProperty patientId;
    private final IntegerProperty medecinId;
    private final StringProperty dateRdv;
    private final StringProperty heure;
    private final StringProperty motif;
    private final StringProperty statut;
    private final StringProperty patientNom;
    private final StringProperty medecinNom;

    public RendezVous() {
        this.id = new SimpleIntegerProperty();
        this.patientId = new SimpleIntegerProperty();
        this.medecinId = new SimpleIntegerProperty();
        this.dateRdv = new SimpleStringProperty();
        this.heure = new SimpleStringProperty();
        this.motif = new SimpleStringProperty();
        this.statut = new SimpleStringProperty("Planifié");
        this.patientNom = new SimpleStringProperty();
        this.medecinNom = new SimpleStringProperty();
    }

    public RendezVous(int id, int patientId, int medecinId, String dateRdv, 
                      String heure, String motif, String statut, 
                      String patientNom, String medecinNom) {
        this.id = new SimpleIntegerProperty(id);
        this.patientId = new SimpleIntegerProperty(patientId);
        this.medecinId = new SimpleIntegerProperty(medecinId);
        this.dateRdv = new SimpleStringProperty(dateRdv);
        this.heure = new SimpleStringProperty(heure);
        this.motif = new SimpleStringProperty(motif);
        this.statut = new SimpleStringProperty(statut);
        this.patientNom = new SimpleStringProperty(patientNom);
        this.medecinNom = new SimpleStringProperty(medecinNom);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int patientId) { this.patientId.set(patientId); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public int getMedecinId() { return medecinId.get(); }
    public void setMedecinId(int medecinId) { this.medecinId.set(medecinId); }
    public IntegerProperty medecinIdProperty() { return medecinId; }

    public String getDateRdv() { return dateRdv.get(); }
    public void setDateRdv(String dateRdv) { this.dateRdv.set(dateRdv); }
    public StringProperty dateRdvProperty() { return dateRdv; }

    public String getHeure() { return heure.get(); }
    public void setHeure(String heure) { this.heure.set(heure); }
    public StringProperty heureProperty() { return heure; }

    public String getMotif() { return motif.get(); }
    public void setMotif(String motif) { this.motif.set(motif); }
    public StringProperty motifProperty() { return motif; }

    public String getStatut() { return statut.get(); }
    public void setStatut(String statut) { this.statut.set(statut); }
    public StringProperty statutProperty() { return statut; }

    public String getPatientNom() { return patientNom.get(); }
    public void setPatientNom(String patientNom) { this.patientNom.set(patientNom); }
    public StringProperty patientNomProperty() { return patientNom; }

    public String getMedecinNom() { return medecinNom.get(); }
    public void setMedecinNom(String medecinNom) { this.medecinNom.set(medecinNom); }
    public StringProperty medecinNomProperty() { return medecinNom; }
}
