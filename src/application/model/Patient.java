package application.model;

import javafx.beans.property.*;

public class Patient {
    private final IntegerProperty id;
    private final StringProperty nom;
    private final StringProperty prenom;
    private final StringProperty dateNaissance;
    private final StringProperty sexe;
    private final StringProperty telephone;
    private final StringProperty email;
    private final StringProperty adresse;
    private final StringProperty groupeSanguin;
    private final StringProperty allergies;
    private final StringProperty dateInscription;

    public Patient() {
        this.id = new SimpleIntegerProperty();
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.dateNaissance = new SimpleStringProperty();
        this.sexe = new SimpleStringProperty();
        this.telephone = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.adresse = new SimpleStringProperty();
        this.groupeSanguin = new SimpleStringProperty();
        this.allergies = new SimpleStringProperty();
        this.dateInscription = new SimpleStringProperty();
    }

    public Patient(int id, String nom, String prenom, String dateNaissance, 
                   String sexe, String telephone, String email, String adresse,
                   String groupeSanguin, String allergies, String dateInscription) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.prenom = new SimpleStringProperty(prenom);
        this.dateNaissance = new SimpleStringProperty(dateNaissance);
        this.sexe = new SimpleStringProperty(sexe);
        this.telephone = new SimpleStringProperty(telephone);
        this.email = new SimpleStringProperty(email);
        this.adresse = new SimpleStringProperty(adresse);
        this.groupeSanguin = new SimpleStringProperty(groupeSanguin);
        this.allergies = new SimpleStringProperty(allergies);
        this.dateInscription = new SimpleStringProperty(dateInscription);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }

    public String getDateNaissance() { return dateNaissance.get(); }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance.set(dateNaissance); }
    public StringProperty dateNaissanceProperty() { return dateNaissance; }

    public String getSexe() { return sexe.get(); }
    public void setSexe(String sexe) { this.sexe.set(sexe); }
    public StringProperty sexeProperty() { return sexe; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getAdresse() { return adresse.get(); }
    public void setAdresse(String adresse) { this.adresse.set(adresse); }
    public StringProperty adresseProperty() { return adresse; }

    public String getGroupeSanguin() { return groupeSanguin.get(); }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin.set(groupeSanguin); }
    public StringProperty groupeSanguinProperty() { return groupeSanguin; }

    public String getAllergies() { return allergies.get(); }
    public void setAllergies(String allergies) { this.allergies.set(allergies); }
    public StringProperty allergiesProperty() { return allergies; }

    public String getDateInscription() { return dateInscription.get(); }
    public void setDateInscription(String dateInscription) { this.dateInscription.set(dateInscription); }
    public StringProperty dateInscriptionProperty() { return dateInscription; }

    public String getNomComplet() { return getPrenom() + " " + getNom(); }
}
