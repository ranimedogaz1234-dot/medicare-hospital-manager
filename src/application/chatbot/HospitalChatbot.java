package application.chatbot;

import application.dao.*;
import application.model.*;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HospitalChatbot {

    private PatientDAO patientDAO = new PatientDAO();
    private MedecinDAO medecinDAO = new MedecinDAO();
    private RendezVousDAO rendezVousDAO = new RendezVousDAO();
    private DossierMedicalDAO dossierDAO = new DossierMedicalDAO();
    private MedicamentDAO medicamentDAO = new MedicamentDAO();

    public String processQuery(String query) {
        String q = query.toLowerCase().trim();

        // Salutations
        if (q.contains("bonjour") || q.contains("salut") || q.contains("hello") || q.contains("hi")) {
            return "Bonjour ! Je suis MediBot, votre assistant hospitalier.\nComment puis-je vous aider aujourd'hui ?\n\nTapez 'aide' pour voir toutes les commandes disponibles.";
        }

        // RDV du jour
        if (q.contains("rdv du jour") || q.contains("rendez-vous du jour") || q.contains("rendez vous aujourd'hui") || q.contains("consultations aujourd'hui")) {
            try {
                String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                ObservableList<RendezVous> rdvs = rendezVousDAO.getRendezVousByDate(today);
                if (rdvs.isEmpty()) {
                    return "Aucun rendez-vous prevu pour aujourd'hui (" + today + ").";
                }
                StringBuilder sb = new StringBuilder("📅 Rendez-vous du jour (" + today + ") :\n");
                for (RendezVous rdv : rdvs) {
                    sb.append("\n  🕐 ").append(rdv.getHeure())
                      .append("  —  ").append(rdv.getPatientNom())
                      .append("  chez Dr. ").append(rdv.getMedecinNom())
                      .append("\n     Motif : ").append(rdv.getMotif()).append("\n");
                }
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors de la recuperation des rendez-vous.";
            }
        }

        // Recherche patient
        if (q.contains("chercher patient") || q.contains("rechercher patient") || q.contains("trouver patient") || q.contains("patient")) {
            String name = extractName(query);
            if (name.isEmpty()) {
                return "Veuillez preciser le nom du patient.\nExemple : 'chercher patient Sami'";
            }
            try {
                ObservableList<Patient> patients = patientDAO.searchPatients(name);
                if (patients.isEmpty()) {
                    return "Aucun patient trouve avec le nom '" + name + "'.";
                }
                StringBuilder sb = new StringBuilder("👥 Patients trouves :\n");
                for (Patient p : patients) {
                    sb.append("\n  • ").append(p.getNomComplet())
                      .append("  (ID: ").append(p.getId()).append(")\n")
                      .append("    Tel : ").append(p.getTelephone()).append("\n")
                      .append("    Groupe sanguin : ").append(p.getGroupeSanguin()).append("\n");
                }
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors de la recherche.";
            }
        }

        // Liste medecins
        if (q.contains("liste des medecins") || q.contains("medecins") || q.contains("docteurs") || q.contains("specialistes")) {
            try {
                ObservableList<Medecin> medecins = medecinDAO.getAllMedecins();
                StringBuilder sb = new StringBuilder("👨‍⚕️ Liste des medecins :\n");
                for (Medecin m : medecins) {
                    sb.append("\n  • Dr. ").append(m.getNomComplet())
                      .append("  —  ").append(m.getSpecialite())
                      .append("  ").append(m.isDisponible() ? "✅ Disponible" : "❌ Indisponible")
                      .append("\n");
                }
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors de la recuperation des medecins.";
            }
        }

        // Medicaments
        if (q.contains("medicament") || q.contains("medicaments") || q.contains("pharmacie") || q.contains("stock")) {
            try {
                ObservableList<Medicament> meds = medicamentDAO.getAllMedicaments();
                StringBuilder sb = new StringBuilder("💊 Medicaments en stock :\n");
                for (Medicament m : meds) {
                    sb.append("\n  • ").append(m.getNom()).append(" ").append(m.getDosage()).append("\n")
                      .append("    Stock : ").append(m.getStock())
                      .append("  —  Prix : ").append(m.getPrix()).append(" DT\n")
                      .append("    Indications : ").append(m.getIndications()).append("\n");
                }
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors de la recuperation des medicaments.";
            }
        }

        // Ajouter rendez-vous
        if (q.contains("ajouter rdv") || q.contains("nouveau rendez-vous") || q.contains("prendre rendez-vous") || q.contains("creer rendez-vous")) {
            return "Pour ajouter un rendez-vous :\n\n  1. Cliquez sur l'onglet 'Rendez-vous'\n  2. Cliquez sur le bouton 'Nouveau Rendez-vous'\n  3. Remplissez le formulaire et validez.";
        }

        // Resume dossier
        if (q.contains("resumer dossier") || q.contains("historique patient") || q.contains("dossier medical")) {
            String name = extractName(query);
            if (name.isEmpty()) {
                return "Veuillez preciser le nom du patient.\nExemple : 'resumer dossier Sami'";
            }
            try {
                ObservableList<Patient> patients = patientDAO.searchPatients(name);
                if (patients.isEmpty()) {
                    return "Patient non trouve.";
                }
                Patient p = patients.get(0);
                ObservableList<DossierMedical> dossiers = dossierDAO.getDossiersByPatient(p.getId());

                StringBuilder sb = new StringBuilder();
                sb.append("📋 Dossier medical de ").append(p.getNomComplet()).append("\n");
                sb.append("\nInformations personnelles :\n");
                sb.append("  • Groupe sanguin : ").append(p.getGroupeSanguin()).append("\n");
                sb.append("  • Allergies : ").append(p.getAllergies()).append("\n");
                sb.append("  • Telephone : ").append(p.getTelephone()).append("\n");

                if (dossiers.isEmpty()) {
                    sb.append("\nAucune consultation enregistree.");
                } else {
                    sb.append("\nHistorique des consultations (").append(dossiers.size()).append(") :\n");
                    for (DossierMedical d : dossiers) {
                        sb.append("\n  📅 ").append(d.getDateConsultation())
                          .append("  —  Dr. ").append(d.getMedecinNom()).append("\n");
                        sb.append("     Diagnostic : ").append(d.getDiagnostic()).append("\n");
                        sb.append("     Traitement : ").append(d.getTraitement()).append("\n");
                    }
                }
                return sb.toString();
            } catch (SQLException e) {
                return "Erreur lors de la recuperation du dossier.";
            }
        }

        // Statistiques
        if (q.contains("statistiques") || q.contains("stats") || q.contains("dashboard") || q.contains("chiffres")) {
            try {
                int patients = patientDAO.getTotalPatients();
                int medecins = medecinDAO.getTotalMedecins();
                int rdvs = rendezVousDAO.getTotalRendezVous();
                int dossiers = dossierDAO.getTotalDossiers();
                int meds = medicamentDAO.getTotalMedicaments();
                int rdvToday = rendezVousDAO.getRendezVousToday();

                return "📊 Statistiques MediCare HMS :\n\n" +
                       "  👥 Patients         : " + patients + "\n" +
                       "  👨‍⚕️ Medecins         : " + medecins + "\n" +
                       "  📅 RDV total        : " + rdvs + "\n" +
                       "  📅 RDV aujourd'hui  : " + rdvToday + "\n" +
                       "  📋 Dossiers         : " + dossiers + "\n" +
                       "  💊 Medicaments      : " + meds;
            } catch (SQLException e) {
                return "Erreur lors du calcul des statistiques.";
            }
        }

        // Aide
        if (q.contains("aide") || q.contains("help") || q.contains("commandes") || q.contains("que peux-tu faire")) {
            return "📖 Commandes disponibles :\n\n" +
                   "  • 'Bonjour'                  — Salutation\n" +
                   "  • 'RDV du jour'              — Voir les rendez-vous d'aujourd'hui\n" +
                   "  • 'Chercher patient [nom]'   — Rechercher un patient\n" +
                   "  • 'Liste des medecins'       — Voir tous les medecins\n" +
                   "  • 'Medicaments'              — Consulter le stock\n" +
                   "  • 'Resumer dossier [nom]'    — Voir l'historique d'un patient\n" +
                   "  • 'Statistiques'             — Chiffres cles du systeme\n" +
                   "  • 'Aide'                     — Afficher cette liste";
        }

        // Par defaut
        return "Je n'ai pas compris votre message.\nTapez 'aide' pour voir les commandes disponibles.";
    }

    private String extractName(String query) {
        String[] words = query.split("\\s+");
        for (int i = words.length - 1; i >= 0; i--) {
            if (!words[i].toLowerCase().matches("(chercher|patient|rechercher|trouver|resumer|dossier|historique|medical)")) {
                return words[i];
            }
        }
        return "";
    }
}
