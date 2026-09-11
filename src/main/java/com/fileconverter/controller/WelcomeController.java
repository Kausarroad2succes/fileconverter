package com.fileconverter.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Button;

import java.io.IOException;

public class WelcomeController {
    @FXML
    private Button logoutButton;


    @FXML
    void handleLogout(){
        Stage currStage= (Stage) logoutButton.getScene().getWindow();
        try {
            FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/fileconverter/fxml/login.fxml"));
            Parent root=loader.load();
            Scene newScene=new Scene(root);
            currStage.setScene(newScene);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
