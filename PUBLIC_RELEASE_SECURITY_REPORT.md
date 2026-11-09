# Final PUBLIC publication security review — MediCare Hospital Manager

Publication context: This document records the security checks performed before the first commit. Statements about absent Git history or publication refer to that review point. The owner subsequently authorized PUBLIC publication of only the reviewed academic demonstration source, with synthetic data and its documented security limitations.

Review date: 2026-11-09. Verdict: **READY FOR PUBLICATION**, specifically as a trusted local synthetic-data demonstration. This is not certification or authorization for real clinical deployment. No Git/GitHub operation occurred.

## Exact publication boundary and Git history

COMMIT_FILES.txt enumerates 85 proposed files, matching the nonignored project set exactly. Source, FXML, CSS, placeholder environment settings, Maven configuration and preparation documents are included. A fresh candidate was copied and SHA256-hashed. Application source matched the previous reviewed candidate; this audit updates reports only.

There is no project .git file/directory or inherited Git repository. No commits, branches, tags or historical blobs exist here to scan or publish. No history was initialized. A future first commit must use only the reviewed project allowlist, without old/unrelated history or forced ignored files.

Actual credentials, local runtime configuration, populated test databases, screenshots, test classes/scripts, scanners, dependencies/cache and target output remain excluded under .gitignore. No database dumps, real patients, staff-account records or generated test secrets are part of this source set. .env.example contains explicit placeholders.

## Secrets, personal data and dependency findings

Redacted Gitleaks 8.30.1 and supplementary private-key, provider-token, credential-URL, credential-literal, password-hash, private-home-path and non-placeholder-email checks found no exposed credentials or private records in the exact proposal. No publication-blocking privacy/source secret finding was identified. This does not establish the validity or safety of credentials outside the reviewed set.

Current OSV queries returned no known advisory for the six resolved runtime package/version pairs: JavaFX base/controls/FXML/graphics 25.0.5, MySQL Connector/J 26.10.0 and protobuf-java 4.36.1. Queries for the declared maven-compiler-plugin 3.14.1 and javafx-maven-plugin 0.0.8 also returned none. Official JavaFX and Connector/J release documentation was checked. Unknown vulnerabilities, all transitive build/test components, native JDK/MySQL binaries and the operating system are not certified by these checks.

No downloader, automatic shell execution, committed application binary or GitHub execution workflow was found in the proposed set. Source publication does not itself start the application, expose a local port or grant database/account access.

## Verified registration and existing doctor workflows

All **223 automated checks and all 23 FXML constructions** passed again, using newly generated disposable schemas with synthetic data:

| Suite | Passed |
|---|---:|
| Clinical DAO/scheduling/migration/dashboard/privacy | 38 |
| JavaFX clinical forms/values/layout/error recovery | 26 |
| Registration/hash/duplicate/compatibility/authorization | 69 |
| JavaFX Sign Up and immediate Sign In | 14 |
| Doctor patient-creation database/security | 27 |
| JavaFX patient creation | 13 |
| Earlier patient/doctor/password CRUD | 13 |
| Earlier appointment/record/medicine CRUD | 9 |
| Legacy-password disabled/enabled security | 7 + 7 |

A fresh copy of the exact proposed source compiled successfully. Immediate doctor registration and login work without administrator approval; historical review metadata does not gate login. Patients have no registration/login role, and doctor Sign Up offers no administrator privilege. Passwords use independent salts and PBKDF2-HMAC-SHA256 at 600,000 iterations; duplicate and concurrent duplicate registration were tested. SQL queries use parameters for submitted values.

New doctors start without access to another doctor's private patients/consultations. Patient creation stores a generated patient ID and session doctor link transactionally. Patient lists/searches/direct IDs/counts, clinical ownership, chatbot/date queries and dashboard aggregates enforce the tested scope. Shared patient profiles remain shared with assigned clinicians; another doctor's consultation records remain private. Retained administrator CRUD, own profile/password changes, clinical updates, rescheduling/cancellation, scheduling conflicts and real chart totals passed.

These are integration and programmatic JavaFX control tests, not exhaustive real-pointer interaction or complete medical-system validation. No production database or medical credential-verification service was contacted.

## Remaining security limitations

- **Identity:** self-registration verifies neither qualifications, identity nor email ownership. Immediate login is intentional but unsuitable as clinical identity assurance.
- **Architecture:** the desktop shares direct database credentials. Anyone controlling the workstation/code or using those credentials can bypass client-side guards. A real multi-user clinical system needs a server-side authorization boundary, auditing and independent medical-data review.
- **Authentication abuse:** there is no login-attempt throttle. Protect real authentication with appropriate rate/attempt controls; changing a password does not require re-entering the old password in the existing desktop form.
- **Diagnostics/privacy:** some older controllers still print raw exception stack traces. Database/configuration or submitted values can reach local diagnostics. Keep logs private and use controlled/redacted logging before processing sensitive information.
- **Operations:** remote database TLS verification, storage encryption, backups/retention, runtime patching and clinical access provisioning require deployment-specific configuration and review; this source does not certify them.
- **Coverage:** fixed 30-minute slots, direct SQL writers outside application locks, exhaustive concurrent assignment/revocation, all multi-process/session sequences, corrupt legacy dates/times, every manual dialog/resolution, load and penetration testing remain unverified. The administrator CLI's actual hidden-password console prompt and asset-rights certification were not completed.

## Before publication versus before deployment

No blocking source/privacy fix was identified for the documented synthetic-data publication scope. Publish only the current reviewed allowlist after separate explicit owner approval, retaining these limitations and placeholder-only configuration. Recheck the actual staged initial commit if any files change.

Before real clinical deployment, resolve the identity, shared-credential architecture, authentication abuse, diagnostic privacy and operational protections above. Rotate any historical exposed/reused credentials if they were ever real; this audit cannot establish their historical activity. No real credential was accessed, displayed, rotated or revoked.

Original archives, untouched inspection copies and unrelated projects were not modified. No repository, commit, push or upload occurred.

Sources: [official JavaFX 25 release notes](https://docs.oracle.com/en/java/java-components/javafx/25/release-notes/), [official Connector/J 26.10 information](https://dev.mysql.com/doc/connector-j/en/connector-j-whats-new.html), [OSV public vulnerability database](https://osv.dev/).