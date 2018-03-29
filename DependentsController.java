package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DependentsController implements Initializable {

	private String username = "nsaghafi",
			       password = "boadoo",
			       update,
			       ssn,
			       dn, ds, db, dr;
	private Connection conn;
	public TextField depName, depSex, depBdate, depRelation;
	public Button dependentsSubmitButton;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		// Load JDBC driver
		try { Class.forName ("oracle.jdbc.driver.OracleDriver"); } 
		catch (ClassNotFoundException e) { System.out.println("\nDriver not loaded\n"); } 
		
		// Create Oracle DatabaseMetaData object
		try { conn = DriverManager.getConnection("jdbc:oracle:thin:@apollo.vse.gmu.edu:1521:ite10g", username, password); } 
		catch (SQLException e) { System.out.println("\nUnable to esablish connection to database"); }	
	}

	public void submitButton(ActionEvent e) throws SQLException {

		if( !addDependent() ) {
			return;
		}

		if( !addMoreDependents() ) {
			Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
			stage.close();;
		}
	}
	
	public boolean addDependent() throws SQLException {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Invalid Input");
		alert.setHeaderText(null);

		if( !checkDname() ) {
			alert.setContentText("First name must: \n"
					+ "1.) Only contain letters\n"
					+ "2.) Be 15 characters in length or shorter\n"
					+ "3.) Not be empty / null");
			alert.showAndWait();
			return false;
		}

		if( !checkDsex() ) {
			alert.setContentText("Sex must be M, F, or blank");
			alert.showAndWait();
			return false;
		}

		if( !checkDbdate() ) {
			alert.setContentText("Birthdate must be in form DD-MMM-YY or left blank\n"
					+ "For example: 09-SEP-89");
			alert.showAndWait();
			return false;
		}

		if( !checkRelationship() ) {
			alert.setContentText("Relationship must contain only letters or left blank");
			alert.showAndWait();
			return false;
		}
		update = String.format("INSERT INTO dependent VALUES (%s, '%s', '%s', '%s', '%s')", ssn, depName.getText(), depSex.getText(), depBdate.getText(), depRelation.getText());
		Statement s = conn.createStatement();
		s.executeUpdate(update);
		return true;
	}
	
	private boolean checkDname()        { return     depName.getText().matches("^[a-zA-Z]{1,15}$"); }
	private boolean checkRelationship() { return depRelation.getText().matches("^[a-zA-Z]{1,15}$"); }
	private boolean checkDsex()         { return      depSex.getText().equalsIgnoreCase("null") ||   depSex.getText().matches("^[f|F|m|M]{0,1}$"); }
	private boolean checkDbdate()       { return    depBdate.getText().equalsIgnoreCase("null") || depBdate.getLength() == 0 || depBdate.getText().matches("^\\d\\d-[a-zA-Z]{3}-\\d\\d$"); }

	public void setSsn(String ssn) {
		this.ssn=ssn;
	}
	
	public boolean addMoreDependents(){
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Add Dependents");
		alert.setHeaderText(null);
		alert.setContentText("Do you want to add another dependent?");
		ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		alert.getButtonTypes().setAll(yesButton, noButton);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yesButton){
			depName.clear();
			depSex.clear();
			depBdate.clear();
			depRelation.clear();
			return true;
		} 

		return false;
	}
}
