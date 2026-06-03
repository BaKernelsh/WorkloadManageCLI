package org.example.workloadmanager.Network;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.example.workloadmanager.ProgramConfig;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HeartbeatThread extends Thread{

    private final GenerationWorker worker;
    private final HttpClient heartbeatClient;
    @Setter @Getter
    private long heartbeatIntervalMs;
    private String uri;

    public HeartbeatThread(GenerationWorker worker,
                           HttpClient heartbeatClient, String address, int port,
                           Long heartbeatIntervalMs){
        this.worker = worker;
        this.heartbeatClient = heartbeatClient;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        this.uri = "http://" + address + ":" + port + "/status";
    }


    @Override
    public void run(){

        try {
            while(!isInterrupted()) {
                long t0 = System.nanoTime();
                ContentResponse response = heartbeatClient.newRequest(this.uri)
                        .timeout(3, TimeUnit.SECONDS)
                        .send();

                long t3 = System.nanoTime();

                String json = response.getContentAsString();
                //System.out.println(json);
                JsonNode resNode = new ObjectMapper().readTree(json);
                if(!resNode.isObject() || (resNode.isObject() && !resNode.has("status")))
                    ; //ustaw w gui "responding, unknown status"
                if(resNode.isObject()
                   && resNode.has("t1") && resNode.get("t1").isLong()
                   && resNode.has("t2") && resNode.get("t2").isLong())
                    worker.calculateAndAddIntervalOffset(t0,resNode.get("t1").asLong(), resNode.get("t2").asLong(), t3);

                Thread.sleep(heartbeatIntervalMs);
            }
        } catch (InterruptedException e) {
            return;
        } catch (TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }


}
