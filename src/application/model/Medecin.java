package application.model;

import javafx.beans.property.*;

public class Medecin {
    private final IntegerProperty id;
    private final StringProperty nom;
    private final StringProperty prenom;
    private final StringProperty specialite;
    private final StringProperty telephone;
    private final StringProperty email;
    private final StringProperty emailConnexion;
    private final StringProperty motDePasse;
    private final BooleanProperty disponible;

    public Medecin() {
        this.id = new SimpleIntegerProperty();
        this.nom = new SimpleStringProperty();
        this.prenom = new SimpleStringProperty();
        this.specialite = new SimpleStringProperty();
        this.telephone = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.emailConnexion = new SimpleStringProperty();
        this.motDePasse = new SimpleStringProperty();
        this.disponible = new SimpleBooleanProperty(true);
    }

    public Medecin(int id, String nom, String prenom, String specialite,
                   String telephone, String email, String emailConnexion,
                   String motDePasse, boolean disponible) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.prenom = new SimpleStringProperty(prenom);
        this.specialite = new SimpleStringProperty(specialite);
        this.telephone = new SimpleStringProperty(telephone);
        this.email = new SimpleStringProperty(email);
        this.emailConnexion = new SimpleStringProperty(emailConnexion);
        this.motDePasse = new SimpleStringProperty(motDePasse);
        this.disponible = new SimpleBooleanProperty(disponible);
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

    public String getSpecialite() { return specialite.get(); }
    public void setSpecialite(String specialite) { this.specialite.set(specialite); }
    public StringProperty specialiteProperty() { return specialite; }

    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    public String getEmailConnexion() { return emailConnexion.get(); }
    public void setEmailConnexion(String emailConnexion) { this.emailConnexion.set(emailConnexion); }
    public StringProperty emailConnexionProperty() { return emailConnexion; }

    public String getMotDePasse() { return motDePasse.get(); }
    public void setMotDePasse(String motDePasse) { this.motDePasse.set(motDePasse); }
    public StringProperty motDePasseProperty() { return motDePasse; }

    public boolean isDisponible() { return disponible.get(); }
    public void setDisponible(boolean disponible) { this.disponible.set(disponible); }
    public BooleanProperty disponibleProperty() { return disponible; }

    public String getNomComplet() { return getPrenom() + " " + getNom(); }
}
