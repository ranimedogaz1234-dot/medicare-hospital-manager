# MediCare Hospital Manager

A JavaFX desktop application that demonstrates how doctors can manage patients, appointments, and medical records in a MySQL-backed workspace.

**Academic demonstration using synthetic data only. This is not a production-ready medical system.** The interface is in French.

## Features

- Doctor self-registration and immediate sign-in. Patients have no login accounts.
- Patient creation and access scoped to the doctor’s assignments.
- Medical records with diagnoses, history, treatments, and notes.
- Appointment scheduling, rescheduling, cancellation history, and conflict checks.
- Dashboard with appointment and clinical record counts and charts.
- Local, rule-based assistant and a separate administrator workspace.

## Technology

Java, JavaFX and FXML, Maven, MySQL, and JDBC. Passwords use salted PBKDF2-HMAC-SHA256. No external AI service or API key is required.

## Run locally

1. Install a patched JDK 25, Maven, and MySQL. Set `JAVA_HOME` to the JDK.
2. Create a new, empty local MySQL database. Set `HOSPITAL_DB_URL`, `HOSPITAL_DB_USER`, and `HOSPITAL_DB_PASSWORD` in your terminal. See [the configuration example](.env.example) for the expected names and URL format. The file is a reference; the app does not load it automatically.
3. From the repository directory, initialize the empty schema using a setup account. In PowerShell:

```powershell
mvn compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
$cp = 'target/classes;' + (Get-Content target/classpath.txt -Raw).Trim()
$env:HOSPITAL_DB_INITIALIZE = 'true'
java -cp $cp application.tools.InitializeDatabase
$env:HOSPITAL_DB_INITIALIZE = 'false'
```

4. Switch to a dedicated runtime database account with CRUD privileges limited to this schema, then run `mvn javafx:run`.
5. Choose **Créer un compte médecin**, enter synthetic details and a unique password of at least 12 characters, then sign in. Use **Mes Patients → Ajouter un patient** to create your first patient.

For the optional administrator workspace, run `java -cp $cp application.tools.CreateAdmin` in an interactive terminal. There are no default credentials. For an existing **local demo** database, back it up and review the schema first. The `application.tools.InitializeClinicalWorkspace` utility adds missing patient assignment and clinical fields when `HOSPITAL_MIGRATE_LEGACY_SCHEMA=true`; return the setting to false after use. Old approval columns may remain in place and do not gate login. `HOSPITAL_TIME_ZONE` defaults to `Africa/Tunis`.

## Verification and limitations

The local regression suite covered registration, login, patient creation, record ownership, appointments, dashboards, and administrator workflows. All 23 FXML views loaded. See the [testing report](TESTING_REPORT.md) and [security review](PUBLIC_RELEASE_SECURITY_REPORT.md) for scope and limitations.

Self-registration does not verify email ownership or medical credentials. The desktop application connects directly to MySQL; anyone who controls the workstation or its shared database credentials can bypass client-side access checks. Real clinical use requires a server-side authorization boundary, verified identities, and a separate security review. Keep credentials and database records outside Git.

