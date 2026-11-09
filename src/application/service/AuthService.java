package application.service;

import application.dao.MedecinDAO;
import application.dao.UtilisateurDAO;
import application.model.Medecin;
import application.model.Utilisateur;

import java.sql.SQLException;

/**
 * Authentification à deux voies : administrateurs (utilisateurs) puis médecins.
 */
public class AuthService {

    public enum LoginType { ADMIN, MEDECIN, FAILED }

    public static final class LoginResult {
        private final LoginType type;
        private final Utilisateur admin;
        private final Medecin medecin;

        private LoginResult(LoginType type, Utilisateur admin, Medecin medecin) {
            this.type = type;
            this.admin = admin;
            this.medecin = medecin;
        }

        public static LoginResult admin(Utilisateur admin) {
            return new LoginResult(LoginType.ADMIN, admin, null);
        }

        public static LoginResult medecin(Medecin medecin) {
            return new LoginResult(LoginType.MEDECIN, null, medecin);
        }

        public static LoginResult failed() {
            return new LoginResult(LoginType.FAILED, null, null);
        }

        public LoginType getType() { return type; }
        public Utilisateur getAdmin() { return admin; }
        public Medecin getMedecin() { return medecin; }
    }

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();

    public LoginResult login(String email, String password) throws SQLException {
        application.util.SessionManager.clear();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return LoginResult.failed();
        }

        String identifiant = email.trim();

        Utilisateur admin = utilisateurDAO.authenticate(identifiant, password);
        if (admin != null) {
            return LoginResult.admin(admin);
        }

        Medecin medecin = medecinDAO.authenticate(identifiant, password);
        if (medecin != null) {
            return LoginResult.medecin(medecin);
        }

        return LoginResult.failed();
    }
}
