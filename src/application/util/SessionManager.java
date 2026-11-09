package application.util;

import application.model.Medecin;
import application.model.Utilisateur;

public final class SessionManager {
    private static volatile Utilisateur currentAdmin;
    private static volatile Medecin currentMedecin;

    private SessionManager() {}

    public static void setAdminSession(Utilisateur admin) throws java.sql.SQLException {
        DatabaseUtil.verifyAdministrator(admin.getId());
        currentAdmin = admin;
        currentMedecin = null;
    }

    public static void setMedecinSession(Medecin medecin) throws java.sql.SQLException {
        DatabaseUtil.verifyDoctor(medecin.getId());
        currentMedecin = medecin;
        currentAdmin = null;
    }

    public static void clear() {
        currentAdmin = null;
        currentMedecin = null;
    }

    public static Utilisateur getCurrentAdmin() {
        return currentAdmin;
    }

    /** @deprecated Préférer {@link #getCurrentAdmin()} */
    public static Utilisateur getCurrentUser() {
        return currentAdmin;
    }

    public static Medecin getCurrentMedecin() {
        return currentMedecin;
    }

    public static int getMedecinId() {
        if (currentMedecin == null) {
            throw new IllegalStateException("Aucun medecin connecte");
        }
        return currentMedecin.getId();
    }

    public static boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }

    public static boolean isMedecinLoggedIn() {
        return currentMedecin != null;
    }

    public static void requireAdmin() throws java.sql.SQLException {
        DatabaseUtil.verifyCurrentSession();
        if (!isAdminLoggedIn()) throw new java.sql.SQLException("Action réservée à l'administrateur.");
    }

    public static boolean canOpenView(String view) {
        if ("login.fxml".equals(view) || "register_doctor.fxml".equals(view)) return true;
        try { DatabaseUtil.verifyCurrentSession(); } catch (java.sql.SQLException e) { return false; }
        if (isAdminLoggedIn()) return view.matches("[a-z_]+\\.fxml") && !view.startsWith("medecin_")
                && application.App.class.getResource("/view/"+view)!=null;
        return java.util.Set.of("medecin_dashboard.fxml","medecin_accueil.fxml","medecin_rendezvous.fxml",
                "medecin_patients.fxml","medecin_dossiers.fxml","medecin_profil.fxml","chatbot.fxml").contains(view);
    }
}
