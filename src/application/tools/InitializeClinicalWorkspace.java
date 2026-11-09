package application.tools;
import application.util.DatabaseUtil;
public final class InitializeClinicalWorkspace {
 public static void main(String[] args){
  if(!"true".equalsIgnoreCase(System.getenv("HOSPITAL_MIGRATE_LEGACY_SCHEMA")))throw new IllegalStateException("Select the local schema and explicitly enable HOSPITAL_MIGRATE_LEGACY_SCHEMA first.");
  try{DatabaseUtil.initializeClinicalWorkspace();}catch(java.sql.SQLException e){throw new IllegalStateException("Clinical schema setup failed. Check the selected local schema and setup permissions.");}
  System.out.println("Clinical fields and assignments checked; existing records preserved.");
 }
}
