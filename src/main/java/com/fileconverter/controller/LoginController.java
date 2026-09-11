package com.fileconverter.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class LoginController {

    // decleaing content
    @FXML
    private TextField userName;
    @FXML
    private PasswordField passInput;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorMsg;
    @FXML
    void handleLogin (){
        String u=userName.getText();
        String p=passInput.getText();
        if(!u.equals("kausar") || !p.equals("1234")){
        errorMsg.setText("Username or password is incorrect!");
        }
        else {

            Stage currentStage = (Stage) userName.getScene().getWindow();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fileconverter/fxml/welcome.fxml"));
                Parent root = loader.load();
                Scene newScene = new Scene(root);
                currentStage.setScene(newScene);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }



}
