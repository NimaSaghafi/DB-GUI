package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController implements Initializable{

	private String 	username = "nsaghafi",
					password = "boadoo";
	private Connection conn;
	public TextField ssnTextField;
	public Button ssnButton;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		// Give initial focus to textfield
		ssnButton.setFocusTraversable(false);

		// Load JDBC driver
		try { Class.forName ("oracle.jdbc.driver.OracleDriver"); } 
		catch (ClassNotFoundException e) { System.out.println("\nDriver not loaded\n"); } 
		
		// Create Oracle DatabaseMetaData object
		try {
			conn = DriverManager.getConnection("jdbc:oracle:thin:@apollo.vse.gmu.edu:1521:ite10g", username, password);
		} catch (SQLException e) { e.printStackTrace(); }
	}

	public void enterSsn(ActionEvent event) throws SQLException, IOException {
		if(!ssnTextField.getText().matches("^[0-9]{9}$")){
			Alert a = new Alert(AlertType.WARNING);
			a.setTitle("Invalid SSN");
			a.setHeaderText(null);
			a.setContentText("Please enter a valid 9 digit social security number");
			a.showAndWait();
			return;
		}
		String query1 = String.format("SELECT mgrssn FROM department WHERE mgrssn='%s'", ssnTextField.getText());

		// Create statement and execute the first query
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(query1);
		
		// If the given SSN belongs to a manager then r.next() will return true
		if(r.next()) {

			ssnTextField.clear();
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene mainScene = new Scene(root);
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setScene(mainScene);
			window.setTitle("Add a New Employee");
			window.centerOnScreen();
			window.show();
		}
		// Else the provided SSN does not belong to a manager and should be rejected
		else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Login Error");
			alert.setHeaderText(null);
			alert.setContentText("The SSN you provided does not belong to a manager.");
			alert.showAndWait();
			System.exit(0);
		}
	}
}
