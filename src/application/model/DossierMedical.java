package application.model;

import javafx.beans.property.*;

public class DossierMedical {
    private final StringProperty maladie = new SimpleStringProperty();
    private final StringProperty antecedents = new SimpleStringProperty();
    private final StringProperty categorie = new SimpleStringProperty();
    public String getMaladie(){return maladie.get();}
    public void setMaladie(String value){maladie.set(value);}
    public StringProperty maladieProperty(){return maladie;}
    public String getAntecedents(){return antecedents.get();}
    public void setAntecedents(String value){antecedents.set(value);}
    public String getCategorie(){return categorie.get();}
    public void setCategorie(String value){categorie.set(value);}
    private final IntegerProperty id;
    private final IntegerProperty patientId;
    private final StringProperty dateConsultation;
    private final StringProperty diagnostic;
    private final StringProperty traitement;
    private final StringProperty notes;
    private final IntegerProperty medecinId;
    private final StringProperty patientNom;
    private final StringProperty medecinNom;

    public DossierMedical() {
        this.id = new SimpleIntegerProperty();
        this.patientId = new SimpleIntegerProperty();
        this.dateConsultation = new SimpleStringProperty();
        this.diagnostic = new SimpleStringProperty();
        this.traitement = new SimpleStringProperty();
        this.notes = new SimpleStringProperty();
        this.medecinId = new SimpleIntegerProperty();
        this.patientNom = new SimpleStringProperty();
        this.medecinNom = new SimpleStringProperty();
    }

    public DossierMedical(int id, int patientId, String dateConsultation, 
                          String diagnostic, String traitement, String notes, 
                          int medecinId, String patientNom, String medecinNom) {
        this.id = new SimpleIntegerProperty(id);
        this.patientId = new SimpleIntegerProperty(patientId);
        this.dateConsultation = new SimpleStringProperty(dateConsultation);
        this.diagnostic = new SimpleStringProperty(diagnostic);
        this.traitement = new SimpleStringProperty(traitement);
        this.notes = new SimpleStringProperty(notes);
        this.medecinId = new SimpleIntegerProperty(medecinId);
        this.patientNom = new SimpleStringProperty(patientNom);
        this.medecinNom = new SimpleStringProperty(medecinNom);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int patientId) { this.patientId.set(patientId); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public String getDateConsultation() { return dateConsultation.get(); }
    public void setDateConsultation(String dateConsultation) { this.dateConsultation.set(dateConsultation); }
    public StringProperty dateConsultationProperty() { return dateConsultation; }

    public String getDiagnostic() { return diagnostic.get(); }
    public void setDiagnostic(String diagnostic) { this.diagnostic.set(diagnostic); }
    public StringProperty diagnosticProperty() { return diagnostic; }

    public String getTraitement() { return traitement.get(); }
    public void setTraitement(String traitement) { this.traitement.set(traitement); }
    public StringProperty traitementProperty() { return traitement; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }

    public int getMedecinId() { return medecinId.get(); }
    public void setMedecinId(int medecinId) { this.medecinId.set(medecinId); }
    public IntegerProperty medecinIdProperty() { return medecinId; }

    public String getPatientNom() { return patientNom.get(); }
    public void setPatientNom(String patientNom) { this.patientNom.set(patientNom); }
    public StringProperty patientNomProperty() { return patientNom; }

    public String getMedecinNom() { return medecinNom.get(); }
    public void setMedecinNom(String medecinNom) { this.medecinNom.set(medecinNom); }
}
