# MediCare Hospital Manager

**Academic demonstration using synthetic data. This is not a production-ready medical system.**

Projet académique utilisant des données synthétiques uniquement. Ce logiciel n'est pas adapté à une utilisation médicale réelle sans protections supplémentaires.

**Final PUBLIC source verdict (2026-11-09): READY FOR PUBLICATION as a trusted local synthetic-data demonstration only.** This is not approval or certification for real clinical deployment. See PUBLIC_RELEASE_SECURITY_REPORT.md for the exact scope and remaining security limitations.

A doctor-facing JavaFX desktop demonstration for patient information, appointments, medical records and medicines. Doctor Sign Up activates an account immediately. Patients have no accounts or login flow; registration never creates an administrator. The existing administrator workspace remains available for trusted administration and care assignments.

**Self-registration does not verify identity, email ownership, medical qualifications or authority to practice. This application is unsuitable for a real clinical deployment without additional safeguards.** Use synthetic data on a trusted local workstation. Anyone controlling this desktop or its shared database credentials can bypass client-side application checks; production use needs a server-side authorization boundary and a separate clinical-data security review.

## Requirements

Use a currently patched JDK 25, Maven and a maintained, patched MySQL server. pom.xml pins JavaFX 25.0.5 and Connector/J 26.10.0. Maven downloads dependencies separately; no local SDK paths, JARs, database records or credentials are required in Git.

## Fresh local setup

Create a NEW empty MySQL database and configure HOSPITAL_DB_URL, HOSPITAL_DB_USER and HOSPITAL_DB_PASSWORD in the process environment. .env.example is a placeholder reference, not an automatically loaded file. Use a setup account for initialization and a dedicated runtime account with schema-local CRUD privileges afterward.

PowerShell from the project directory, with JAVA_HOME pointing to your patched JDK:

```powershell
mvn compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
$cp = 'target/classes;' + (Get-Content target/classpath.txt -Raw).Trim()
$env:HOSPITAL_DB_INITIALIZE = 'true'
java -cp $cp application.tools.InitializeDatabase
$env:HOSPITAL_DB_INITIALIZE = 'false'
mvn javafx:run
```

Choose Créer un compte médecin / Sign Up, enter first and last name, email, specialty, password and confirmation, then return to Sign In. Email is the existing login identifier; a separate username is unnecessary. Passwords are 12–128 characters; choose a unique passphrase. Input bounds, email format, confirmation and duplicate addresses are validated. Salted PBKDF2-HMAC-SHA256 hashes use the existing 600,000-iteration implementation. No plaintext password is saved.

A successful registration creates a doctor in medecins (the MEDECIN login type represents DOCTOR), available for scheduling and able to sign in immediately. No role selector, patient identity, medical record or administrator is created. Registration never gives access to the administrator workspace.

## Patient and record access

A newly registered doctor starts with no assigned patients, appointments or private medical records. Doctors can now open Mes Patients → Ajouter un patient, enter the required last/first names and optional details, then select Enregistrer. The saved patient immediately appears in the creator's list and patient totals. A durable patient_medecins link is inserted with the new patient in the same transaction; no appointment or medical record is fabricated. Existing appointment/medical-record relationships also continue to grant the appropriate patient access. Trusted administrators retain patient editing, deletion and assignment features. Doctor creation cannot link an existing patient ID or grant another doctor access.

Doctor patient lists, searches, direct patient lookup and patient counts are scoped to those relationships. Appointments and medical records—including chatbot/date/statistics queries—are scoped to their owning doctor. An assigned shared patient's profile can be consulted, but another doctor's private consultation records remain hidden. Caller-supplied doctor or record IDs cannot expand access. Doctors cannot claim unassigned patients by creating an appointment/record, move another doctor's records, or change another doctor's account/password. They can maintain their own profile and their own authorized clinical records. Administrator data-management access is preserved.

## Add patient creation to an existing local database

Fresh initialization includes patient_medecins. Existing schemas need this additive, idempotent setup using the chosen local database and a setup account with CREATE/REFERENCES privileges:

```powershell
$cp = 'target/classes;' + (Get-Content target/classpath.txt -Raw).Trim()
$env:HOSPITAL_MIGRATE_LEGACY_SCHEMA = 'true'
java -cp $cp application.tools.InitializePatientAssignments
$env:HOSPITAL_MIGRATE_LEGACY_SCHEMA = 'false'
```

It creates only the patient/doctor association table and its foreign keys/index; it does not rewrite existing patients or clinical records. Switch back to the runtime account afterward; runtime saves need INSERT on patients and patient_medecins. Creation rolls back both rows if either insert fails. Names are required; an omitted birth date becomes SQL NULL, and invalid dates/email/oversized inputs are rejected without clearing the dialog. The dialog prevents repeated saves after success. These are clinical records, never patient login accounts.

## Dossiers médicaux, rendez-vous et tableau de bord

Dans **Dossiers médicaux**, recherchez un patient autorisé par son nom, sélectionnez-le, puis choisissez **Ajouter un diagnostic**. Le formulaire conserve la maladie, la catégorie, la date du diagnostic, les antécédents, le diagnostic détaillé, le traitement et les notes. **Modifier le dossier** met à jour la consultation sélectionnée. Une maladie ou un diagnostic est obligatoire ; la date ne peut pas être future. Chaque consultation reste liée à son patient et à son médecin. Un ancien formulaire administratif qui ne fournit pas les nouveaux champs conserve leur contenu.

Dans **Rendez-vous**, choisissez **Nouveau rendez-vous**, un patient de votre liste, une date, une heure HH:mm et un motif facultatif. Ajoutez d'abord un patient dans Mes patients si la liste est vide. **Modifier / Reprogrammer** permet de changer les détails et le statut ; **Annuler** conserve la ligne et son historique. Les nouveaux rendez-vous et les déplacements doivent être futurs. Une ancienne consultation peut être marquée Terminée ou Annulée sans déplacer sa date. Chaque créneau dure 30 minutes ; les chevauchements pour le même médecin, y compris après minuit, sont refusés. Une annulation libère le créneau. Les écritures de l'application verrouillent la ligne du médecin pendant la vérification et l'enregistrement.

Le **Tableau de bord** affiche six compteurs réels : patients autorisés, rendez-vous, rendez-vous du jour hors annulations, terminés, annulés et dossiers du médecin. Le graphique circulaire regroupe les statuts ; les anciens Confirmé / En cours sont comptés parmi les actifs et les valeurs inconnues dans Autres. Les catégories médicales proviennent du choix du médecin, sans déduction automatique : les parts comptent les dossiers, pas les patients uniques ; les anciennes valeurs vides restent Non renseignée. L'histogramme couvre les 12 mois explicitement affichés, du mois courant moins cinq au mois courant plus six, tous statuts compris. Les cinq prochains rendez-vous excluent les consultations terminées ou annulées.

Les graphiques utilisent JavaFX, sans nouvelle dépendance. Le tableau de bord s'actualise après une sauvegarde locale, à son ouverture et toutes les 30 secondes lorsqu'il est visible. Les données vides sont signalées ; une erreur de connexion efface les anciennes statistiques et affiche un message générique. Les dispositions s'adaptent à la largeur disponible. HOSPITAL_TIME_ZONE définit la date et l'heure métier ; la valeur par défaut est Africa/Tunis. Utilisez la même zone sur les postes qui partagent une base. Les DATE / HH:mm existants ne sont pas convertis automatiquement.

## Ajouter les champs cliniques à une base locale existante

Une initialisation neuve inclut tous les champs. Pour une base locale existante compatible, compilez cette version et utilisez explicitement un compte de préparation avec les droits CREATE / ALTER / REFERENCES sur cette seule base :

```powershell
$cp = 'target/classes;' + (Get-Content target/classpath.txt -Raw).Trim()
$env:HOSPITAL_MIGRATE_LEGACY_SCHEMA = 'true'
java -cp $cp application.tools.InitializeClinicalWorkspace
$env:HOSPITAL_MIGRATE_LEGACY_SCHEMA = 'false'
```

Cet outil idempotent crée patient_medecins si nécessaire et ajoute seulement les colonnes manquantes maladie VARCHAR(255), antecedents TEXT et categorie VARCHAR(80) à dossiers_medicaux. Il ne supprime ni ne réécrit les dossiers existants et ne fabrique aucune maladie. Les anciennes lignes conservent NULL dans les nouveaux champs. Sauvegardez votre base locale avant toute modification de schéma ; les DDL MySQL ne constituent pas une migration transactionnelle unique. Après la préparation, revenez au compte d'exécution avec les seuls droits nécessaires. L'application ne lance aucune migration automatiquement. Aucune migration sur une base de production n'est autorisée par ce projet.

## Existing databases and removed approval workflow

No approval migration is required. Fresh schemas omit account_status, reviewed_by and reviewed_at. Existing databases may retain those historical columns; the application does not drop or rewrite old review history. For compatibility, new inserts use the historical active value if that column exists, but login and protected operations do not use approval state.

Consequently, former pending/rejected metadata no longer blocks a valid doctor login. Existing appointment availability values are preserved and affect scheduling, not authentication. The approval screen/controller, review actions, navigation and migration utility are removed. No account is deleted as part of this change. Existing doctor login/email/password columns and a single-column unique email_connexion index remain required; registration fails safely if legacy uniqueness is missing. Resolve incompatible legacy schemas separately rather than deleting duplicates.

HOSPITAL_DB_INITIALIZE, HOSPITAL_MIGRATE_LEGACY_SCHEMA and HOSPITAL_ALLOW_LEGACY_PASSWORDS default false. Never point initialization at a real clinical database. The separate legacy schema/password controls remain for controlled existing migrations; legacy plaintext acceptance stays disabled by default and should be replaced by password resets before sensitive use.

## Existing administrator functions

If you need the retained administrative workspace, create an initial operator-controlled administrator through the existing interactive utility:

```powershell
java -cp $cp application.tools.CreateAdmin
```

It asks for email and a hidden unique password; no credentials are seeded. This utility is separate from doctor Sign Up. Administrator creation logic was tested; the actual interactive console prompt remains manually unverified. Doctor account creation through the existing trusted administrator dialog and blank-password edit behavior remain supported.

## Testing and publication

The current revision passed 223 automated checks: 38 clinical/database/security checks, 26 JavaFX clinical flow/layout/error-recovery checks, 27 patient-creation checks, 13 patient-dialog checks, 69 registration/authorization checks, 14 Sign Up flow checks and 36 earlier CRUD/authentication/security regressions. All 23 FXML views load. Clinical JavaFX tests use a visible synthetic scene and programmatic control events; registration and patient-dialog tests use an undisplayed context. Synthetic dashboard and form renders were inspected. This does not certify every manual interaction, screen size or clinical workflow. TESTING_REPORT.md records exact coverage, build/scan results and limitations.

The owner has authorized PUBLIC source publication under medicare-hospital-manager. COMMIT_FILES.txt is the complete reviewed publication allowlist. Generated classes, caches, databases, test credentials, diagnostic tools and runtime configuration stay ignored. The included review reports record the validation completed before the initial publication; they do not certify production deployment or real clinical use.
