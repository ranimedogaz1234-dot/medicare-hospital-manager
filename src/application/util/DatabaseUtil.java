package application.util;

import java.sql.*;

public class DatabaseUtil {
    private static final String PATIENT_ASSIGNMENTS_SQL = "CREATE TABLE IF NOT EXISTS patient_medecins ("
        + "patient_id INT NOT NULL, medecin_id INT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
        + "PRIMARY KEY(patient_id,medecin_id), INDEX idx_patient_medecin(medecin_id),"
        + "FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE,"
        + "FOREIGN KEY(medecin_id) REFERENCES medecins(id) ON DELETE CASCADE) ENGINE=InnoDB";
    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }


    // Authentication/setup code uses short-lived connections, never the UI DAO session.
    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(requiredEnv("HOSPITAL_DB_URL"),
                requiredEnv("HOSPITAL_DB_USER"), requiredEnv("HOSPITAL_DB_PASSWORD"));
    }

    public static Connection getConnection() throws SQLException {
        verifyCurrentSession();
        // Every operation owns its connection; concurrent saves never share a transaction.
        return openConnection();
    }

    public static void initializeDatabase() {
        try (Connection conn = openConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS patients ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nom VARCHAR(255) NOT NULL,"
                + "prenom VARCHAR(255) NOT NULL,"
                + "date_naissance DATE NULL,"
                + "sexe VARCHAR(50) NULL,"
                + "telephone VARCHAR(100) NULL,"
                + "email VARCHAR(255) NULL,"
                + "adresse VARCHAR(255) NULL,"
                + "groupe_sanguin VARCHAR(50) NULL,"
                + "allergies TEXT NULL,"
                + "date_inscription DATE DEFAULT (CURRENT_DATE)"
                + ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS medecins ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nom VARCHAR(255) NOT NULL,"
                + "prenom VARCHAR(255) NOT NULL,"
                + "specialite VARCHAR(255) NOT NULL,"
                + "telephone VARCHAR(100) NULL,"
                + "email VARCHAR(255) NULL,"
                + "email_connexion VARCHAR(255) NULL UNIQUE,"
                + "mot_de_passe VARCHAR(255) NULL,"
                + "disponible INT DEFAULT 1"
                + ") ENGINE=InnoDB");

            if ("true".equalsIgnoreCase(System.getenv("HOSPITAL_MIGRATE_LEGACY_SCHEMA"))) migrateMedecinsTable(stmt);
            stmt.execute(PATIENT_ASSIGNMENTS_SQL);

            stmt.execute("CREATE TABLE IF NOT EXISTS rendez_vous ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "patient_id INT NOT NULL,"
                + "medecin_id INT NOT NULL,"
                + "date_rdv DATE NOT NULL,"
                + "heure VARCHAR(20) NOT NULL,"
                + "motif TEXT NULL,"
                + "statut VARCHAR(100) DEFAULT 'Planifié',"
                + "FOREIGN KEY (patient_id) REFERENCES patients(id),"
                + "FOREIGN KEY (medecin_id) REFERENCES medecins(id)"
                + ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS dossiers_medicaux ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "patient_id INT NOT NULL,"
                + "date_consultation DATE DEFAULT (CURRENT_DATE),"
                + "diagnostic TEXT NULL,"
                + "traitement TEXT NULL,"
                + "notes TEXT NULL,"
                + "maladie VARCHAR(255) NULL, antecedents TEXT NULL, categorie VARCHAR(80) NULL,"
                + "medecin_id INT NULL,"
                + "FOREIGN KEY (patient_id) REFERENCES patients(id),"
                + "FOREIGN KEY (medecin_id) REFERENCES medecins(id)"
                + ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS medicaments ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "nom VARCHAR(255) NOT NULL,"
                + "dosage VARCHAR(100) NULL,"
                + "indications TEXT NULL,"
                + "stock INT DEFAULT 0,"
                + "prix DECIMAL(10,2) DEFAULT 0.00"
                + ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS utilisateurs ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "email VARCHAR(255) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL"
                + ") ENGINE=InnoDB");

            if ("true".equalsIgnoreCase(System.getenv("HOSPITAL_MIGRATE_LEGACY_SCHEMA"))) migrateUtilisateursTable(stmt);


            System.out.println("Base de données initialisée avec succès (MySQL)!");
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failed. Check the local schema and configuration.", e);
        }
    }

    public static void initializePatientAssignments() throws SQLException {
        try (Connection conn=openConnection(); Statement stmt=conn.createStatement()) {
            stmt.execute(PATIENT_ASSIGNMENTS_SQL);
        }
    }

    public static void initializeClinicalWorkspace() throws SQLException {
        initializePatientAssignments();
        try(Connection c=openConnection();Statement s=c.createStatement()) {
            for(String[] field:new String[][]{{"maladie","VARCHAR(255) NULL"},{"antecedents","TEXT NULL"},{"categorie","VARCHAR(80) NULL"}}) {
                try(ResultSet r=c.getMetaData().getColumns(c.getCatalog(),null,"dossiers_medicaux",field[0])){if(r.next())continue;}
                s.execute("ALTER TABLE dossiers_medicaux ADD COLUMN "+field[0]+" "+field[1]);
            }
        }
    }

    public static void verifyAdministrator(int id) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement q = conn.prepareStatement("SELECT * FROM utilisateurs WHERE id=?")) {
            q.setInt(1, id);
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next()) throw new SQLException("Session non autorisée. Reconnectez-vous.");
                for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
                    if ("role".equalsIgnoreCase(rs.getMetaData().getColumnLabel(i))) {
                        String role=rs.getString(i);
                        if (!"admin".equalsIgnoreCase(role) && !"ROLE_ADMIN".equalsIgnoreCase(role))
                            throw new SQLException("Session non autorisée. Reconnectez-vous.");
                    }
                }
            }
        }
    }

    public static void verifyDoctor(int id) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement q = conn.prepareStatement("SELECT id FROM medecins WHERE id=?")) {
            q.setInt(1,id);
            try (ResultSet rs=q.executeQuery()) {
                if (!rs.next()) throw new SQLException("Compte indisponible. Reconnectez-vous.");
            }
        }
    }

    public static void verifyCurrentSession() throws SQLException {
        try {
            if (SessionManager.isAdminLoggedIn()) verifyAdministrator(SessionManager.getCurrentAdmin().getId());
            else if (SessionManager.isMedecinLoggedIn()) verifyDoctor(SessionManager.getMedecinId());
            else throw new SQLException("Connexion requise pour accéder aux données.");
        } catch (SQLException e) {
            SessionManager.clear();
            throw new SQLException("Accès indisponible. Reconnectez-vous ou contactez l'administrateur.");
        }
    }

    private static void migrateMedecinsTable(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE medecins ADD COLUMN email_connexion VARCHAR(255) NULL");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("ALTER TABLE medecins ADD COLUMN mot_de_passe VARCHAR(255) NULL");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("ALTER TABLE medecins ADD UNIQUE KEY uk_medecin_email_connexion (email_connexion)");
        } catch (SQLException ignored) { }
    }

    private static void migrateUtilisateursTable(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN email VARCHAR(255) NULL");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("UPDATE utilisateurs SET email = username WHERE email IS NULL AND username LIKE '%@%'");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("UPDATE utilisateurs SET email = CONCAT(username, '@example.invalid') "
                + "WHERE email IS NULL AND username IS NOT NULL AND username NOT LIKE '%@%'");
        } catch (SQLException ignored) { }
    }

    public static void closeConnection() {
        // Compatibility with existing callers; DAO try-with-resources closes each connection.
    }
}
