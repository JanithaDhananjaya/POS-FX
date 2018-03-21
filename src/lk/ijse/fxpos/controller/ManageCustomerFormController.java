/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.ijse.fxpos.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.fxpos.view.util.tblmodel.CustomerTM;

/**
 * FXML Controller class
 *
 * @author ranjith-suranga
 */
public class ManageCustomerFormController implements Initializable {

    @FXML
    private JFXTextField txtID;
    @FXML
    private JFXTextField txtName;
    @FXML
    private JFXTextField txtAddress;
    @FXML
    private JFXTextField txtSalary;
    @FXML
    private TableView<CustomerTM> tblCustomers;
    
    private Connection connection;
    private boolean update = false;
    @FXML
    private JFXButton btnDelete;
    
    private void loadAllCustomers() throws SQLException{
        
        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT * FROM Customer");
        
        ArrayList<CustomerTM> alCustomers = new ArrayList<>();
        
        while (rst.next()){
            CustomerTM customer = new CustomerTM(
                    rst.getString(1),
                    rst.getString(2),
                    rst.getString(3),
                    rst.getDouble(4));
            alCustomers.add(customer);
        }
        
        tblCustomers.setItems(FXCollections.observableArrayList(alCustomers));
        
        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                CustomerTM customer = observable.getValue();
                
                if (customer == null){
                    update = false;
                    return;
                }
                update = true;
                btnDelete.setDisable(!update);
                txtID.setText(customer.getId());
                txtName.setText(customer.getName());
                txtAddress.setText(customer.getAddress());
                txtSalary.setText(customer.getSalary() + "");
            }
        });        
        
        if (alCustomers.size() > 0){
            tblCustomers.getSelectionModel().clearAndSelect(0);
        }
        
    }
    
    private void clearAllTextFileds(){
        txtID.setText("");
        txtName.setText("");
        txtAddress.setText("");
        txtSalary.setText("");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        tblCustomers.getColumns().get(0).setStyle("-fx-alignment: CENTER;");
        tblCustomers.getColumns().get(3).setStyle("-fx-alignment: CENTER_RIGHT;");
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblCustomers.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("salary"));
        
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/JDBC", "root", "mysql");
            loadAllCustomers();
        } catch (SQLException ex) {
            Logger.getLogger(ManageCustomerFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @FXML
    private void navigateToMain(MouseEvent event) throws IOException {
        Label lblMainNav = (Label) event.getSource();
        Stage primaryStage = (Stage) lblMainNav.getScene().getWindow();
        
        Parent root = FXMLLoader.load(this.getClass().getResource("/lk/ijse/fxpos/view/MainForm.fxml"));
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }

    @FXML
    private void btnNewCustomerOnAction(ActionEvent event) {
        tblCustomers.getSelectionModel().clearSelection();
        clearAllTextFileds();
        btnDelete.setDisable(!update);
        txtID.requestFocus();
    }

    @FXML
    private void btnSaveOnAction(ActionEvent event) throws SQLException {
        
        String id = txtID.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        double salary = Double.parseDouble(txtSalary.getText());
        
        String sql = null;
        if (!update){
            sql = "INSERT INTO Customer VALUES (?,?,?,?)";
        }else{
            sql = "UPDATE Customer SET name=?, address=?, salary=? WHERE id=?";
        }
        PreparedStatement pstm = connection.prepareStatement(sql);
        if (!update){
            pstm.setObject(1, id);
            pstm.setObject(2, name);
            pstm.setObject(3, address);
            pstm.setObject(4, salary);            
        }else{
            pstm.setObject(4, id);
            pstm.setObject(1, name);
            pstm.setObject(2, address);
            pstm.setObject(3, salary);            
        }

        
        int affectedRows = pstm.executeUpdate();
        
        if (affectedRows > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Customer has been saved successfully", ButtonType.OK);
            alert.setTitle("Customer Saved !");
            alert.setHeaderText("Customer Saved !");
            alert.showAndWait();
            loadAllCustomers();
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "Customer can't be saved", ButtonType.OK);
            alert.setTitle("Save Failed !");
            alert.setHeaderText("Failed to Save!");
            alert.showAndWait();            
        }
        
        btnNewCustomerOnAction(event);
    }

    @FXML
    private void btnDeleteOnAction(ActionEvent event) throws SQLException {
        
        PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
        pstm.setObject(1, txtID.getText());
        int affectedRows = pstm.executeUpdate();
        
        if (affectedRows > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Customer has been deleted successfully", ButtonType.OK);
            alert.setTitle("Customer Deleted !");
            alert.setHeaderText("Customer Deleted !");
            alert.showAndWait();
            loadAllCustomers();            
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR, "Customer can't be deleted", ButtonType.OK);
            alert.setTitle("Delete Failed !");
            alert.setHeaderText("Failed to Delete!");
            alert.showAndWait();             
        }
        
        btnNewCustomerOnAction(event);
        
    }
    
}
