package org.example.workloadmanager;

import org.eclipse.jetty.client.HttpClient;

public class AppContext {

    HttpClient workersCommunicationClient = new HttpClient();

    public AppContext() throws Exception {
        workersCommunicationClient.setFollowRedirects(false);
        workersCommunicationClient.start();
    }

    public HttpClient getWorkersCommunicationClient(){
        return workersCommunicationClient;
    }

}
