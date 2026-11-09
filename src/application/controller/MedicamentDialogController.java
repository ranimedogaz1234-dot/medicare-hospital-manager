package application.controller;

import application.model.Medicament;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MedicamentDialogController {
    @FXML private Label dialogTitle;
    @FXML private TextField txtNom, txtDosage, txtIndications, txtPrix;
    @FXML private Spinner<Integer> spStock;

    @SuppressWarnings("unused")
    private Medicament medicament;

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
        spStock.setValueFactory(valueFactory);
    }

    public void setMedicament(Medicament m) {
        this.medicament = m;
        txtNom.setText(m.getNom());
        txtDosage.setText(m.getDosage());
        txtIndications.setText(m.getIndications());
        spStock.getValueFactory().setValue(m.getStock());
        txtPrix.setText(String.valueOf(m.getPrix()));
        dialogTitle.setText("Modifier Medicament");
    }

    public Medicament getMedicament() {
        Medicament m = new Medicament();
        m.setNom(txtNom.getText());
        m.setDosage(txtDosage.getText());
        m.setIndications(txtIndications.getText());
        m.setStock(spStock.getValue());
        try {
            m.setPrix(Double.parseDouble(txtPrix.getText()));
        } catch (NumberFormatException e) {
            m.setPrix(0.0);
        }
        return m;
    }
}
