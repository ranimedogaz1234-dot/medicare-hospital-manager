# Dossiers médicaux, rendez-vous et tableau de bord — validation

Publication context: This document records the security checks performed before the first commit. Statements about absent Git history or publication refer to that review point. The owner subsequently authorized PUBLIC publication of only the reviewed academic demonstration source, with synthetic data and its documented security limitations.

Review date: 2026-11-09. Scope: the authorized Hopitale Fixed working copy only. No original archive, inspection copy, unrelated project, production service or GitHub repository was modified. This report supersedes earlier feature-test conclusions for the current source.

## Implemented behavior

**Dossiers médicaux:** authorized patient list and name search; patient selection; consultations with disease, declared category, diagnosis date, history, diagnosis, treatment and notes; add/edit dialogs with validation, save-before-close, retry and repeated-save protection. Patient and record IDs are authorized before populating forms. Doctor updates cannot move an existing record to another patient. Older administrative editors preserve extended fields they do not supply.

**Rendez-vous:** doctor's complete authorized list with patient/date/time/reason/French status, add, edit, reschedule, refresh and cancellation retaining history. Strict HH:mm and future scheduling checks; historical visits can be completed/cancelled without changing their date. Thirty-minute intervals reject same-doctor overlap, including across midnight. Transactional writes lock the doctor row before checking conflicts. Independent database connections replace a shared transaction connection; concurrent overlap testing confirms exactly one success. Cancelled slots are reusable. Existing administrator CRUD and unrelated features remain.

**Tableau de bord:** six scoped database counters; appointment-status donut, declared-condition pie, 12-month bar chart and next-five appointment list. No sample values are shown as real statistics. Confirmed/in-progress legacy statuses count as active; unknown statuses remain Autres. Condition categories count consultation records, not unique patients; old empty categories are Non renseignée. Month range is current month minus five through plus six, with all statuses included. Today excludes cancellations. Completed/cancelled visits do not appear in upcoming visits. Consistent snapshot reads, save notifications, visible-only 30-second refresh, actor checks, responsive layout, empty states and safe connection-error recovery are included.

## Database changes

Fresh initialization includes nullable maladie VARCHAR(255), antecedents TEXT and categorie VARCHAR(80) in dossiers_medicaux. The explicitly enabled InitializeClinicalWorkspace utility adds only missing columns and the existing patient_medecins association table. It does not delete/rewrite clinical rows, backfill fabricated diagnoses or change authentication. Idempotency and old-record preservation were tested on a disposable synthetic schema. The old-editor COALESCE behavior preserves extended fields omitted by administrative forms. MySQL DDL can commit individually; review/backup a selected local database before manually running the additive utility. No production database was migrated.

## Tests and results

| Suite | Result |
|---|---|
| Clinical DAO, scheduling, migration, dashboard and privacy | 38 checks PASS |
| JavaFX clinical workflows, values, layout and recovery | 26 checks PASS |
| Immediate doctor registration, hashing, compatibility and authorization | 69 checks PASS |
| Sign Up / immediate Sign In JavaFX flow | 14 checks PASS |
| Doctor Add Patient database/security | 27 checks PASS |
| Doctor Add Patient JavaFX dialog | 13 checks PASS |
| Existing patient/doctor/password CRUD | 13 checks PASS |
| Existing appointment/record/medicine CRUD | 9 checks PASS |
| Legacy-password disabled/enabled security | 7 + 7 checks PASS |
| FXML construction with appropriate sessions | All 23 views PASS |

Total: **223 automated checks plus 23 FXML constructions**. Isolated synthetic databases were created separately for clinical, registration, patient-creation and earlier regression suites on a project-local loopback MySQL instance. Generated credentials remain ignored and were never printed. No real patient information, payment API or external medical service was used.

The 38 clinical checks exercise: empty dashboard, creator patient counts, extended-field persistence/generated IDs, patient separation, updates and denied record moves, invalid/future dates, additive migration preservation/idempotency, uncategorized legacy records, old-editor compatibility, private cross-doctor record/dashboard access, appointments and IDs, exact/partial/adjacent conflicts, rescheduling and rollback, cancellation/history/slot reuse, midnight overlap, independent doctor schedules, unauthorized cancellation/direct lookup, concurrent overlapping writes, actual status/today/month/category totals, changes after diagnosis creation and anonymous denial. Synthetic historical appointments are inserted by the test fixture to exercise old statuses and today's totals; normal application scheduling still rejects past starts.

The 26 JavaFX checks load the real FXML/controllers, populate forms and fire application control events against synthetic records. They cover empty states, automatic patient/record/appointment count changes, patient search/selection, invalid diagnosis, save/edit/history persistence, category pie values, appointment save/conflict/retry/reschedule/completion/cancellation, donut and monthly values, upcoming filtering, French dates/patient labels, wide/narrow layouts, safe clearing on database failure, recovery and unsubscribe on hide. The cancellation handler is invoked programmatically; the actual confirmation dialog's pointer interaction is not certified. Synthetic dashboard and both clinical-form renders were visually inspected.

Registration and earlier suites were rerun successfully against this revision. Their scheduling fixtures were updated to valid future, non-overlapping times and diagnosis fixtures to non-future dates. Ownership-negative fixtures contain valid payloads so they still test authorization. Coverage includes salted PBKDF2, duplicate/concurrent registration, immediate login, no patient/admin creation through Sign Up, old review metadata compatibility, creator links/rollback, own/shared/unassigned scopes, chatbot/date/count queries, profile/password restrictions, administrator CRUD and unchanged rows after denied writes.

## Build and publication review

The finalization process compiles current source and an allowlist-only fresh source snapshot using the documented Maven dependencies. COMMIT_FILES.txt enumerates the exact proposed publication set. The candidate receives redacted Gitleaks scanning, supplementary credential/private-path checks, .gitignore assertions and SHA256 source/snapshot comparisons. Generated tests, fixture databases, credentials, caches, class files, scans, screenshots and runtime configuration under .test-tools are excluded. See FINAL_REVIEW.md for the completed review status.

## Remaining limitations

Self-registration does not verify medical identity or email ownership. This is a trusted-workstation demonstration, unsuitable for real clinical deployment without additional safeguards. Anyone controlling the workstation/shared database credentials can bypass client-side checks; production needs server-side authorization, appropriate identity provisioning, audit and independent clinical-data security review.

Shared patient profiles remain available to assigned doctors; private consultation records remain scoped to their owning doctor. Slots currently use a documented fixed 30-minute duration. Direct SQL/other applications do not participate in application row-lock scheduling. Exhaustive concurrent reassignment/revocation, multi-process/session lifecycle, legacy corrupt dates/times, full manual dialog coverage, every screen size, load and penetration testing remain unverified. Runtime patch status, asset rights and the administrator CLI's actual hidden-password console interaction are not certified. No new clinical decision logic, credential verification or production readiness is claimed.

README documents fresh setup, explicit additive local schema preparation, time-zone configuration, immediate doctor registration and record privacy. No Git initialization, commit, repository creation, push or publication was performed.
## Exact changed publication files

These repository-relative paths changed from the previous reviewed candidate:

- .env.example (modified)
- COMMIT_FILES.txt (modified)
- FINAL_REVIEW.md (modified)
- GITHUB_PREPARATION.md (modified)
- PUBLIC_RELEASE_SECURITY_REPORT.md (modified)
- README.md (modified)
- TESTING_REPORT.md (modified)
- src/application/App.java (modified)
- src/application/controller/AppointmentFormController.java (added)
- src/application/controller/DashboardController.java (modified)
- src/application/controller/MedecinAccueilController.java (modified)
- src/application/controller/MedecinDashboardController.java (modified)
- src/application/controller/MedecinDossiersController.java (modified)
- src/application/controller/MedecinRdvController.java (modified)
- src/application/controller/MedicalRecordFormController.java (added)
- src/application/dao/DashboardDAO.java (added)
- src/application/dao/DossierMedicalDAO.java (modified)
- src/application/dao/PatientDAO.java (modified)
- src/application/dao/RendezVousDAO.java (modified)
- src/application/model/DossierMedical.java (modified)
- src/application/tools/InitializeClinicalWorkspace.java (added)
- src/application/util/AppointmentRules.java (added)
- src/application/util/DataChanges.java (added)
- src/application/util/DatabaseUtil.java (modified)
- src/application/util/FrenchDateFields.java (added)
- src/application/util/SessionManager.java (modified)
- src/view/css/style.css (modified)
- src/view/medecin_accueil.fxml (modified)
- src/view/medecin_dashboard.fxml (modified)
- src/view/medecin_dossier_dialog.fxml (added)
- src/view/medecin_dossiers.fxml (modified)
- src/view/medecin_rdv_dialog.fxml (added)
- src/view/medecin_rendezvous.fxml (modified)

Ignored additions/updates: ClinicalWorkflowTests.java, ClinicalUiTests.java, clinical-regressions.ps1, clinical-review.cjs, updated synthetic scheduling fixtures in SelfSignupTests.java, DoctorPatientCreationTests.java and Workflow.java and generated runtime configuration under .test-tools, plus local dependency/scanner installs, generated results, test credentials/databases and snapshots. None are publication files.

## Final independent publication review — 2026-11-09

All 223 checks and 23 FXML constructions were rerun successfully on newly generated disposable schemas. Doctor registration, immediate login without approval, duplicate prevention, salted hashing, no patient/admin registration, own-record boundaries, patient creation, clinical records, appointments and dashboard values passed again. A fresh copy of the exact proposed source compiled successfully. No application code was changed during this review.

The exact 85-file proposal matches COMMIT_FILES.txt and nonignored files. No project or inherited Git repository exists, hence there is no commit history to include or scan. Redacted Gitleaks plus supplementary key/token/credential/hash/private-path/personal-email checks found no exposed credentials or personal medical/account records. Test credentials, runtime settings, populated databases, test snapshots/tools and class output are excluded. Current OSV queries returned no known advisories for the six resolved runtime package/version pairs and two declared Maven plugins. This does not certify all native runtime, transitive build/test or operating-system components.

Verdict: READY FOR PUBLICATION as a documented local synthetic-data demonstration. Remaining security limitations include unverified professional/email identity, a shared direct database credential/client-side trust boundary, no login-attempt throttling, and raw diagnostic stack traces in some older controllers. Generic/redacted operational logging, appropriate authentication/abuse controls, server-side authorization, secure database transport/storage and independent review are required before real clinical deployment. Complete manual/load/penetration coverage and historical credential activity remain unverified. Nothing was committed or uploaded.
