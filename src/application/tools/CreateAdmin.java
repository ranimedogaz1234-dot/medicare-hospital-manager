package application.tools;
import application.util.DatabaseUtil;
import application.util.PasswordUtil;
import java.util.Arrays;
public final class CreateAdmin {
 public static void main(String[] args) throws Exception {
  var console=System.console();
  if(console==null) throw new IllegalStateException("Run this command in a terminal with an interactive console.");
  String email=console.readLine("Admin email: ").trim();
  char[] password=console.readPassword("New password (at least 12 characters): ");
  try {
   createAdmin(email, password);
   console.printf("Admin created.%n");
  } finally {Arrays.fill(password,'\0');DatabaseUtil.closeConnection();}
 }
 public static void createAdmin(String email, char[] password) throws Exception {
  if(email==null || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password==null || password.length<12 || password.length>1024) throw new IllegalArgumentException("Invalid email or password length");
  try(var db=DatabaseUtil.openConnection();var statement=db.prepareStatement("INSERT INTO utilisateurs(email,password) VALUES(?,?)")) {
   statement.setString(1,email);statement.setString(2,PasswordUtil.hash(new String(password)));statement.executeUpdate();
  }
 }
}
