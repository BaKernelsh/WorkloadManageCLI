package org.example.workloadmanager.GUI;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.example.workloadmanager.Result;
import org.example.workloadmanager.WorkloadGenerationContext;

public class WorkersListItemController extends Node {

    @FXML
    private TextField workerIDTextField;
    @FXML
    private Label duplicateWorkerIDLabel;
    @FXML
    private TextField addressTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button connectButton;

    @Setter
    private WorkloadGenerationContext workloadGenContext;

    @FXML
    private void addWorker(){

/*        Task<Result<Void>> task = new Task<Result<Void>>() {
            @Override
            protected Result<Void> call() throws Exception {
                if(workloadGenContext!=null){

                }

            }
        }*/

    }

}
