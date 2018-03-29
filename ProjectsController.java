package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import javafx.application.Platform;
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

public class ProjectsController implements Initializable{

	private String username = "nsaghafi",
			       password = "boadoo",
			       ssn;
	private Connection conn;
	public Button submitButton;
	public double[] hours = new double[6];
	public TextField p1, p2, p3, p10, p20, p30;

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

		try {
			hours[0] = Double.parseDouble(p1.getText());
			hours[1] = Double.parseDouble(p2.getText());
			hours[2] = Double.parseDouble(p3.getText());
			hours[3] = Double.parseDouble(p10.getText());
			hours[4] = Double.parseDouble(p20.getText());
			hours[5] = Double.parseDouble(p30.getText());
		}
		catch(NumberFormatException n) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Input Error");
			alert.setHeaderText(null);
			alert.setContentText("Please only enter numbers into the input fields. Decimal points are allowed.");
			alert.showAndWait();
			return;
		}

		double sum = 0;
		for(double d:hours) 
			sum+=d;		

		if(sum > 40) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Maximum Hours Exceeded");
			alert.setHeaderText(null);
			alert.setContentText("Total number of hours worked across ALL projects may not exceed 40.");
			alert.showAndWait();
		}
		else {
			addProjects();
			Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
		    stage.close();;
		}
	}
	
	public void setSsn(String ssn) {
		this.ssn=ssn;
	}

	public void addProjects() throws SQLException {
		String update;
		int pno;
		for(int i = 0 ; i < 6 ; i++) {
			switch(i) {
				case 0: pno = 1;
						break;
				case 1: pno = 2;
						break;
				case 2: pno = 3;
						break;
				case 3: pno = 10;
						break;
				case 4: pno = 20;
						break;
				case 5: pno = 30;
						break;
				default: pno = 0;
			}

			if(hours[i] > 0) {
				update = String.format("INSERT INTO works_on VALUES (%s, %s, %s)", ssn, String.valueOf(pno), String.valueOf(hours[i]));
				Statement s = conn.createStatement();
				s.executeUpdate(update);
			}
		}	
	}
}
