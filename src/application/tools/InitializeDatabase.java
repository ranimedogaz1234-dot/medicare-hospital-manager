package application.tools;
import application.util.DatabaseUtil;
public final class InitializeDatabase {
    public static void main(String[] args) {
        if (!"true".equalsIgnoreCase(System.getenv("HOSPITAL_DB_INITIALIZE"))) {
            throw new IllegalStateException("Set HOSPITAL_DB_INITIALIZE=true only for an explicitly chosen new local database.");
        }
        try { DatabaseUtil.initializeDatabase(); }
        finally { DatabaseUtil.closeConnection(); }
    }
}
