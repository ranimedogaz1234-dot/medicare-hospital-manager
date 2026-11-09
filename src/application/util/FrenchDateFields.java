package application.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/** French display/input only; database dates remain ISO-8601. */
public final class FrenchDateFields {
    private FrenchDateFields() {}

    public static void configure(DatePicker field) {
        var format = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.FRENCH)
                .withResolverStyle(ResolverStyle.STRICT);
        field.setPromptText("jj/mm/aaaa");
        field.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate value) {
                return value == null ? "" : format.format(value);
            }
            @Override public LocalDate fromString(String value) {
                return value == null || value.isBlank() ? null : LocalDate.parse(value.strip(), format);
            }
        });
    }
}
