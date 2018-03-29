package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable{

	private Connection conn;
	private String 	   username = "nsaghafi",
					   password = "boadoo",
					   fn, mi, ln, s, bd, ad, se, sa, ss, dn, em;
	public RadioButton projectsYes,
					   dependentsYes;
	public TextField   fname, minit, lname, ssn, bdate, addr, sex, salary, superssn, dno, email;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		// Load JDBC driver
		try { Class.forName ("oracle.jdbc.driver.OracleDriver"); } 
		catch (ClassNotFoundException e) { System.out.println("\nDriver not loaded\n"); } 
		
		// Create Oracle DatabaseMetaData object
		try { conn = DriverManager.getConnection("jdbc:oracle:thin:@apollo.vse.gmu.edu:1521:ite10g", username, password); } 
		catch (SQLException e) { System.out.println("\nUnable to esablish connection to database"); }	
	}
	
	public void submitButton() throws IOException, SQLException{

		// if false returned by addEmployee then one or more of the inputs is incorrect
		if( !addEmployee() ) { 
			return; 
		}

		// If projects yes radio button is selected, open the projects window to assign the new employee to projects
		if(projectsYes.isSelected()){

			FXMLLoader ploader  = new FXMLLoader(getClass().getResource("Projects.fxml"));
			Stage 	   pstage   = new Stage();
			Parent 	   projects = (Parent) ploader.load();
			pstage.setScene(new Scene(projects));
			ProjectsController pcon = ploader.<ProjectsController>getController();
			pcon.setSsn(s);
			pstage.setTitle("Assign Projects");
			pstage.initModality(Modality.APPLICATION_MODAL);
			pstage.setResizable(false);
			pstage.centerOnScreen();
			pstage.showAndWait();
		}

		// If dependents yes radio button is selected, open the projects window to assign the new employee to projects
		if(dependentsYes.isSelected()){

			FXMLLoader dloader  = new FXMLLoader(getClass().getResource("Dependents.fxml"));
			Stage 	   dstage   = new Stage();
			Parent 	   dependents = (Parent) dloader.load();
			dstage.setScene(new Scene(dependents));
			DependentsController dcon = dloader.<DependentsController>getController();
			dcon.setSsn(s);
			dstage.setTitle("Assign Projects");
			dstage.initModality(Modality.APPLICATION_MODAL);
			dstage.setResizable(false);
			dstage.centerOnScreen();
			dstage.showAndWait();
		}
		
		String employeeReport = buildReport();
		
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Employee Successfully Added");
		alert.setHeaderText(null);
		alert.setContentText(employeeReport);
		alert.getDialogPane().setMinWidth(800);
		alert.showAndWait();
		System.exit(0);
	}

	private boolean addEmployee() throws SQLException {

		// Check textfields and make sure input is valid
		// Alert popup gives first invalid field to user
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Invalid Input");
		alert.setHeaderText(null);

		if( !checkFname() ) {
			alert.setContentText("First name must: \n"
					+ "1.) Only contain letters\n"
					+ "2.) Be 15 characters in length or shorter\n"
					+ "3.) Not be empty / null");
			alert.showAndWait();
			return false;
		}

		if( !checkMinit() ) {
			alert.setContentText("Middle initial may only be a single letter or left blank");
			alert.showAndWait();
			return false;
		}

		if( !checkLname() ) {
			alert.setContentText("Last name must: \n"
					+ "1.) Only contain letters\n"
					+ "2.) Be 15 characters in length or shorter\n"
					+ "3.) Not be empty / null");
			alert.showAndWait();
			return false;
		}

		if( !checkSsn() ) {
			alert.setContentText("Social security number must:\n"
					+ "1.) Only contain numbers\n"
					+ "2.) Be 9 digits in length\n"
					+ "3.) Not be empty / null\n"
					+ "4.) Be unique, ie not already stored in the database");
			alert.showAndWait();
			return false;
		}

		if( !checkBdate() ) {
			alert.setContentText("Birthdate must either be in form DD-MMM-YY or left blank\n"
					+ "For example: 01-APR-99");
			alert.showAndWait();
			return false;
		}

		if( !checkAddr() ) {
			alert.setContentText("Address must contain only letters, spaces, and commas or may be left blank\n"
					+ "For example: Fairfax, VA");
			alert.showAndWait();
			return false;
		}

		if( !checkSex() ) {
			alert.setContentText("Sex must be M, F, or left blank");
			alert.showAndWait();
			return false;
		}

		if( !checkSalary() ) {
			alert.setContentText("Salary must be numbers only or left blank");
			alert.showAndWait();
			return false;
		}

		if( !checkSuperSsn() ) {
			alert.setContentText("Supervisor SSN must be a 9 digit number which corresponds to an employee that already exists in the database, or left blank");
			alert.showAndWait();
			return false;
		}

		if( !checkDno() ) {
			alert.setContentText("Department number must be a 1 digit number which corresponds to a department that already exists in the database, or left blank");
			alert.showAndWait();
			return false;
		}

		if( !checkEmail() ) {
			alert.setContentText("Email must be a valid email in form user@domain.com or left blank");
			alert.showAndWait();
			return false;
		}

		// assign inputs to class variables 
		setVars();
		
		// Build the update to insert the new employee
		String update = String.format("INSERT INTO employee VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", 
															        fn, mi, ln, s,  bd, ad, se, sa, ss, dn, em);
		// Execute the update
		Statement s = conn.createStatement();
		s.executeUpdate(update);
		return true;
	}
	

	private String buildReport() throws SQLException {
		StringBuilder report = new StringBuilder();
		String empQuery = "SELECT * "+
						  "FROM employee "+
						  "WHERE ssn="+s;

		String projQuery = "SELECT pno, pname, hours "+
						   "FROM works_on, project "+
						   "WHERE pno=pnumber and essn="+s;

		String depQuery = "SELECT dependent_name, sex, bdate, relationship "+
						  "FROM dependent "+
						  "WHERE essn="+s;

		// Create statement and execute the first query
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(empQuery);

		// Print last name and SSN of retrieved employees
		report.append("Employee Information: \n");
		while(r.next()) {
			report.append(String.format("%s %s %s %s %s %s %s %s %s %s %s\n",
				r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5).subSequence(0,10), 
				r.getString(6), r.getString(7), r.getString(8), r.getString(9), r.getString(10), r.getString(11)));
		}

		r = s.executeQuery(projQuery);
		report.append("\nProjects: \n");
		while(r.next()) {
			report.append(String.format("%s %s %s\n", 
				r.getString(1), r.getString(2), r.getString(3)));
		}
		
		r = s.executeQuery(depQuery);
		report.append("\nDependents: \n");
		while(r.next()) {
			report.append(String.format("%s %s %s %s\n", 
				r.getString(1), r.getString(2), r.getString(3).subSequence(0,10), r.getString(4)));
		}

		return report.toString();
	}
	private void setVars() {
		fn = "'" + fname.getText() + "'";
		ln = "'" + lname.getText() + "'";
		s  = "'" +   ssn.getText() + "'";
		if(   minit.getLength() ==0 ||    minit.getText().equalsIgnoreCase("null")) mi="null"; else mi =    "'"+minit.getText()+"'";
		if(   bdate.getLength() ==0 ||    bdate.getText().equalsIgnoreCase("null")) bd="null"; else bd =    "'"+bdate.getText()+"'";
		if(    addr.getLength() ==0 ||     addr.getText().equalsIgnoreCase("null")) ad="null"; else ad =     "'"+addr.getText()+"'";
		if(     sex.getLength() ==0 ||      sex.getText().equalsIgnoreCase("null")) se="null"; else se =      "'"+sex.getText()+"'";
		if(  salary.getLength() ==0 ||   salary.getText().equalsIgnoreCase("null")) sa="null"; else sa =       salary.getText();
		if(superssn.getLength() ==0 || superssn.getText().equalsIgnoreCase("null")) ss="null"; else ss = "'"+superssn.getText()+"'";
		if(     dno.getLength() ==0 ||      dno.getText().equalsIgnoreCase("null")) dn="null"; else dn =          dno.getText();
		if(   email.getLength() ==0 ||    email.getText().equalsIgnoreCase("null")) em="null"; else em =    "'"+email.getText()+"'";
	}

	private boolean checkFname() { return  fname.getText().matches("^[a-zA-Z]{1,15}$"); }
	private boolean checkLname() { return  lname.getText().matches("^[a-zA-Z]{1,15}$"); }
	private boolean checkMinit() { return  minit.getText().equalsIgnoreCase("null") || minit.getText().matches("^[a-zA-Z]{0,1}$"); }
	private boolean checkSex()   { return    sex.getText().equalsIgnoreCase("null") ||   sex.getText().matches("^[f|F|m|M]{0,1}$"); }
	private boolean checkSalary(){ return salary.getText().equalsIgnoreCase("null") ||salary.getText().matches("^[0-9]{0,10}$"); }
	private boolean checkAddr()  { return   addr.getText().equalsIgnoreCase("null") ||  addr.getText().matches("^[a-zA-Z| |,]{0,30}$"); }
	private boolean checkBdate() { return  bdate.getText().equalsIgnoreCase("null") || bdate.getLength() == 0 || bdate.getText().matches("^\\d\\d-[a-zA-Z]{3}-\\d\\d$"); }
	private boolean checkEmail() { return  email.getLength() == 0 || email.getText().equalsIgnoreCase("null") || email.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"); }

	private boolean checkSsn() throws SQLException   { 
		if( !(ssn.getText().matches("^[0-9]{9}$")) ) {
			return false;
		}
		String query1 = String.format("SELECT ssn FROM employee WHERE ssn='%s'", ssn.getText());
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(query1);	
		return !r.next(); 
	}
	private boolean checkSuperSsn() throws SQLException   { 
		if(superssn.getLength()==0) {
			return true;
		}
		if( !(superssn.getText().matches("^[0-9]{9}$")) ) {
			return false;
		}
		String query1 = String.format("SELECT ssn FROM employee WHERE ssn='%s'", superssn.getText());
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(query1);	
		return r.next(); 
	}
	private boolean checkDno() throws SQLException   { 
		if(dno.getLength()==0) {
			return true;
		}
		if( !(dno.getText().matches("^[0-9]{1}$")) ) {
			return false;
		}
		String query1 = String.format("SELECT dnumber FROM department WHERE dnumber=%s", dno.getText());
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(query1);	
		return r.next(); 
	}
}
