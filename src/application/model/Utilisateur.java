package application.model;

/**
 * Compte administrateur uniquement (connexion séparée des médecins).
 */
public class Utilisateur {
    private int id;
    private String email;
    private String password;

    public Utilisateur() {}

    public Utilisateur(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
