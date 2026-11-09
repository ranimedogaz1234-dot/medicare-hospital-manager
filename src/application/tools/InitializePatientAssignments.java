package application.tools;
import application.util.DatabaseUtil;
public final class InitializePatientAssignments {
    public static void main(String[] args) {
        if (!"true".equalsIgnoreCase(System.getenv("HOSPITAL_MIGRATE_LEGACY_SCHEMA")))
            throw new IllegalStateException("Explicitly select the local schema and enable HOSPITAL_MIGRATE_LEGACY_SCHEMA first.");
        try { DatabaseUtil.initializePatientAssignments(); }
        catch (java.sql.SQLException e) { throw new IllegalStateException("Patient assignment setup failed. Check the chosen local schema and setup privileges."); }
        System.out.println("Patient assignment table checked; existing records preserved.");
    }
}
