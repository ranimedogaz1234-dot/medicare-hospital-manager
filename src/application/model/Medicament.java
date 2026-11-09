package application.model;

import javafx.beans.property.*;

public class Medicament {
    private final IntegerProperty id;
    private final StringProperty nom;
    private final StringProperty dosage;
    private final StringProperty indications;
    private final IntegerProperty stock;
    private final DoubleProperty prix;

    public Medicament() {
        this.id = new SimpleIntegerProperty();
        this.nom = new SimpleStringProperty();
        this.dosage = new SimpleStringProperty();
        this.indications = new SimpleStringProperty();
        this.stock = new SimpleIntegerProperty(0);
        this.prix = new SimpleDoubleProperty(0.0);
    }

    public Medicament(int id, String nom, String dosage, String indications, int stock, double prix) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.dosage = new SimpleStringProperty(dosage);
        this.indications = new SimpleStringProperty(indications);
        this.stock = new SimpleIntegerProperty(stock);
        this.prix = new SimpleDoubleProperty(prix);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getDosage() { return dosage.get(); }
    public void setDosage(String dosage) { this.dosage.set(dosage); }
    public StringProperty dosageProperty() { return dosage; }

    public String getIndications() { return indications.get(); }
    public void setIndications(String indications) { this.indications.set(indications); }
    public StringProperty indicationsProperty() { return indications; }

    public int getStock() { return stock.get(); }
    public void setStock(int stock) { this.stock.set(stock); }
    public IntegerProperty stockProperty() { return stock; }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }
    public DoubleProperty prixProperty() { return prix; }
}
