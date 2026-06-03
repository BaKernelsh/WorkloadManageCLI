package org.example.workloadmanager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;

public class HelloController {
    //private final AppContext context = new AppContext();
    private final WorkloadGenerationContext workloadGenContext = new WorkloadGenerationContext();

    @FXML
    private Label welcomeText;

    public HelloController() throws Exception {
    }

    @FXML
    protected void onHelloButtonClick() throws Exception {
        //HttpClient client = context.getWorkersCommunicationClient();

        //ContentResponse res = client.GET("http://localhost:8090/status");

        //welcomeText.setText(res.getContentAsString());
        //client.stop();
    }


}
