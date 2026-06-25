package org.example.workloadmanager.Network;


import lombok.Getter;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.Destination;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.StringRequestContent;
import org.example.workloadmanager.Result;
import org.example.workloadmanager.SettingWorkload.WorkloadConfigForWorker;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GenerationWorker {
    @Getter
    private Destination destination;
    private String address;
    private int port;
    private String uri;

    private String workerID;

    private Long intervalDurationNanos;
    private Long workloadDurationNanos;
    private Long workloadStartTimeNanos;
    @Getter
    private Long[] intervalOffsets;
    private final Object paramsGuard = new Object();

    private final HeartbeatThread heartbeatThread;

    private final AtomicBoolean workloadStarted;

    //TODO uzupełnić offsety - bo możliwe, że nie każdy intervalID będzie miał przypisany - uzupełnić brakujące poprzednim ?
    public void calculateAndAddIntervalOffset(Long t0, Long t1, Long t2, Long t3){
        synchronized (paramsGuard) {
            if (workloadStarted.get() && t0 >= workloadStartTimeNanos && t0 < workloadStartTimeNanos + workloadDurationNanos) {
                try {
                    long offset = Math.addExact(Math.subtractExact(t1, t0) / 2, Math.subtractExact(t2, t3) / 2);

                    long timeBetweenStartAndHeartbeat = t0 - workloadStartTimeNanos;
                    int intervalNumber;
                    if(intervalDurationNanos > 0)
                        intervalNumber = Math.toIntExact(Math.floorDiv(timeBetweenStartAndHeartbeat, intervalDurationNanos));
                    else
                        intervalNumber = Math.toIntExact(Math.floorDiv(timeBetweenStartAndHeartbeat, heartbeatThread.getHeartbeatIntervalMs() * 1_000_000));

                    if (intervalNumber < intervalOffsets.length - 1)
                        intervalOffsets[intervalNumber] = offset;
                } catch (ArithmeticException e) {
                    //log
                    System.out.println("ArithmeticException during offset calculation");
                }
            }
        }
    }

    public Long getIntervalOffset(int intervalNumber){
        if(intervalNumber < intervalOffsets.length-1)
            return intervalOffsets[intervalNumber];
        return null;
    }

    public void setWorkloadParams(Long intervalDurationNanos, Long workloadDurationNanos,
                                  Long workloadStartTimeNanos,
                                  int intervalCount
                                  ){
        synchronized (paramsGuard) {
            this.intervalDurationNanos = intervalDurationNanos;
            this.workloadDurationNanos = workloadDurationNanos;
            this.workloadStartTimeNanos = workloadStartTimeNanos;
            this.intervalOffsets = new Long[intervalCount];
        }
    }

    public GenerationWorker(String workerID,
                            Destination destination, String address, int port,
                            HttpClient heartbeatClient, Long heartbeatIntervalMs,
                            AtomicBoolean workloadStarted) {
        this.workerID = workerID;
        this.destination = destination;
        this.uri = "http://" + address + ":" + port;
        this.heartbeatThread = new HeartbeatThread(this, heartbeatClient,
                                                    address, port,
                                                    heartbeatIntervalMs);
        this.workloadStarted = workloadStarted;
    }

    public void startSendingHeartBeat(){
        this.heartbeatThread.start();
    }

    public void stopSendingHeartBeat(){
        this.heartbeatThread.interrupt();
        try {
            this.heartbeatThread.join();
        }catch (InterruptedException e){
            ;
        }
    }

    public Result<Void> sendWorkloadConfig(WorkloadConfigForWorker configForWorker){
        try {
            ContentResponse resp =
                    this.destination.getHttpClient().newRequest(uri + "/setup")
                            .method("POST")
                            .timeout(5, TimeUnit.SECONDS)
                            .body(new StringRequestContent("application/json", configForWorker.toJson()))
                            .send();
            System.out.println(resp.getContentAsString());
            if(resp.getStatus() == 200)
                return Result.success();
            else
                return Result.failure(new ObjectMapper().readTree(resp.getContentAsString()).asObject().get("status").asString());

        } catch (InterruptedException e) {
            return Result.failure("Interrupted");
        } catch (TimeoutException e) {
            return Result.failure("Timeout");
        }catch(ExecutionException e){
            return Result.failure(e.getCause().getMessage());
        }
    }

    public Result<Void> startWorkload(){
        try{
            ContentResponse resp =
                    this.destination.getHttpClient().newRequest(uri + "/generation")
                            .method("POST")
                            .timeout(5, TimeUnit.SECONDS)
                            .body(new StringRequestContent("application/json", "{\"action\": \"START\"}"))
                            .send();
            System.out.println(resp.getContentAsString());
            if(resp.getStatus() == 200)
                return Result.success();
            else
                return Result.failure(resp.getContentAsString());

        } catch (InterruptedException e) {
            return Result.failure("Interrupted");
        } catch (TimeoutException e) {
            return Result.failure("Timeout");
        }catch(ExecutionException e){
            return Result.failure(e.getCause().getMessage());
        }
    }
}
