package application.service;

import application.dao.DoctorRegistrationDAO;
import java.util.Locale;

public final class DoctorRegistrationService {
    public record Result(boolean success, String message) {}

    public Result register(String fullName, String email, String specialty, String password, String confirm) {
        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()
                || specialty == null || specialty.isBlank() || password == null || password.isEmpty() || confirm == null || confirm.isEmpty())
            return new Result(false,"Veuillez remplir tous les champs.");
        String name=fullName.strip().replaceAll("\\s+"," ");
        if (name.length()>150 || name.indexOf(' ')<1 || fullName.chars().anyMatch(Character::isISOControl))
            return new Result(false,"Indiquez votre prénom et votre nom (150 caractères maximum).");
        String login=email.strip().toLowerCase(Locale.ROOT);
        if (login.length()>254 || !login.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+")
                || login.startsWith(".") || login.contains("..") || login.contains(".@"))
            return new Result(false,"Indiquez une adresse email valide.");
        if (specialty.strip().length()>100 || specialty.chars().anyMatch(Character::isISOControl))
            return new Result(false,"La spécialité doit contenir 1 à 100 caractères.");
        if (password.length()<12 || password.length()>128 || password.isBlank() || password.indexOf('\0')>=0)
            return new Result(false,"Utilisez un mot de passe unique de 12 à 128 caractères.");
        if (!password.equals(confirm)) return new Result(false,"Les mots de passe ne correspondent pas.");
        try {
            boolean created=new DoctorRegistrationDAO().register(name,login,specialty.strip(),password);
            return created ? new Result(true,"Compte médecin créé. Vous pouvez maintenant vous connecter avec votre email et votre mot de passe.")
                    : new Result(false,"Cette adresse n'est pas disponible. Connectez-vous ou contactez l'administrateur.");
        } catch (java.sql.SQLException e) {
            return new Result(false,"Inscription indisponible. Contactez l'administrateur pour vérifier la configuration locale.");
        }
    }
}
