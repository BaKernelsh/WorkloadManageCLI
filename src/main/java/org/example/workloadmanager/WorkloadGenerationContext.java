package org.example.workloadmanager;

import lombok.Getter;
import org.eclipse.jetty.client.Destination;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.util.component.LifeCycle;
import org.example.workloadmanager.Network.ResultSending.IResultServer;
import org.example.workloadmanager.Network.GenerationWorker;
import org.example.workloadmanager.Results.WorkerResults;
import org.example.workloadmanager.SettingWorkload.WorkloadConfigForWorker;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkloadGenerationContext {

    private Long intervalDurationNanos;
    private int intervalCount;
    private Long workloadDurationNanos;
    private Long workloadStartTimeNanos;
    private Long heartbeatIntervalMs = ProgramConfig.heartbeatIntervalMs;

    private IResultServer resultServer;
    private final HashMap<String, GenerationWorker> workersById = new HashMap<>();
    private HashMap<String, WorkloadConfigForWorker> workloadConfigByWorkerID;
    private HttpClient workersCommunicationClient;
    private final Object clientDestinationsGuard = new Object();
    @Getter
    private final AtomicBoolean workloadStarted = new AtomicBoolean(false);
    private Thread waitForWorkloadEndThread;

    private final HashMap<String, WorkerResults> resultsByWorkerID = new HashMap<>();
    @Getter
    private CountDownLatch resultsLatch;


    public CompletableFuture<List<SendToWorkerResult>> sendConfigurationToWorkersAsync(
            Map<String, WorkloadConfigForWorker> configs) {

        ExecutorService executor = Executors.newFixedThreadPool(workersById.size());

        List<CompletableFuture<SendToWorkerResult>> futures = new ArrayList<>();

        for (Map.Entry<String, GenerationWorker> entry : workersById.entrySet()) {

            String workerId = entry.getKey();
            GenerationWorker worker = entry.getValue();

            CompletableFuture<SendToWorkerResult> future =
                    CompletableFuture.supplyAsync(
                            () -> new SendToWorkerResult(
                                    workerId,
                                    worker.sendWorkloadConfig(configs.get(workerId))),
                            executor);

            futures.add(future);
        }

        CompletableFuture<?>[] all =
                futures.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(all)
                .thenApply(v -> {
                    List<SendToWorkerResult> results = new ArrayList<>();

                    for (CompletableFuture<SendToWorkerResult> future : futures) {
                        results.add(future.join());
                    }

                    return results;
                });
    }

    public CompletableFuture<List<SendToWorkerResult>> startWorkload(){
        this.workloadStartTimeNanos = System.nanoTime();
        setWorkloadParams(this.intervalDurationNanos, this.workloadDurationNanos, this.workloadStartTimeNanos);
        workloadStarted.set(true);
        resultsLatch = new CountDownLatch(workersById.size());

        this.waitForWorkloadEndThread = new Thread(() -> {
            try {
                Thread.sleep(workloadDurationNanos / 1_000_000);
                System.out.println("czas workloadu zakonczony");
                workloadStarted.set(false);
            }catch (InterruptedException e){
                workloadStarted.set(false);
            }
        });
        this.waitForWorkloadEndThread.start();

        ExecutorService executor = Executors.newFixedThreadPool(workersById.size());

        List<CompletableFuture<SendToWorkerResult>> futures = new ArrayList<>();

        for (Map.Entry<String, GenerationWorker> entry : workersById.entrySet()) {

            String workerId = entry.getKey();
            GenerationWorker worker = entry.getValue();

            CompletableFuture<SendToWorkerResult> future =
                    CompletableFuture.supplyAsync(
                            () -> new SendToWorkerResult(
                                    workerId,
                                    worker.startWorkload()),
                            executor);

            futures.add(future);
        }

        CompletableFuture<?>[] all =
                futures.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(all)
                .thenApply(v -> {
                    List<SendToWorkerResult> results = new ArrayList<>();

                    for (CompletableFuture<SendToWorkerResult> future : futures) {
                        results.add(future.join());
                    }

                    return results;
                });
    }

    public HashMap<String, WorkerResults> getWorkersResults(){
        return resultsByWorkerID;
    }


    public WorkloadGenerationContext() throws Exception {
        this.resultServer = ProgramConfig.getNewResultServerImpl(this);
        if(this.resultServer == null)
            throw new IllegalArgumentException("Unknown IResultConfig implementation in program config");
        this.resultServer.start();

        createWorkersCommunicationClient();
    }

    private void createWorkersCommunicationClient() throws Exception {
        HttpClient client = new HttpClient();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {System.out.println("zamykam httpClient"); LifeCycle.stop(client);}));
        if (client.getHttpClientTransport() instanceof HttpClientTransportOverHTTP http1)
            http1.setInitializeConnections(true);
        client.start();
        this.workersCommunicationClient = client;
    }

    public void setWorkloadParams(Long intervalDurationNanos, Long workloadDurationNanos,
                                  Long workloadStartTimeNanos
    ){
        this.intervalDurationNanos = intervalDurationNanos;
        this.workloadDurationNanos = workloadDurationNanos;
        this.workloadStartTimeNanos = workloadStartTimeNanos;
        //ustawić ograniczenie na (workloadDuration / intervalDuration) żeby mieściło się w int
        if(intervalDurationNanos > 0)
            this.intervalCount = Math.toIntExact(Math.ceilDiv(workloadDurationNanos, intervalDurationNanos));
        else
            this.intervalCount = Math.toIntExact(Math.ceilDiv(workloadDurationNanos, heartbeatIntervalMs * 1_000_000));
        for(GenerationWorker worker : workersById.values()){
            worker.setWorkloadParams(intervalDurationNanos, workloadDurationNanos, workloadStartTimeNanos, intervalCount);
        }
    }

    public Result<Void> addWorker(String workerId, String address, int port){
        Result<Destination> createWorkerConnectionsResult = preCreateConnectionsToWorker(address, port);
        if(!createWorkerConnectionsResult.isSuccess())
            return Result.failure(createWorkerConnectionsResult.getErrorMessage());

        GenerationWorker worker = new GenerationWorker(workerId, createWorkerConnectionsResult.getValue(), address, port,
                workersCommunicationClient, heartbeatIntervalMs,
                workloadStarted);
        worker.setWorkloadParams(intervalDurationNanos, workloadDurationNanos, workloadStartTimeNanos, intervalCount);
        worker.startSendingHeartBeat();
        workersById.put(workerId, worker);
        return Result.success();
    }

    public void removeWorker(String workerId){
        GenerationWorker removed = workersById.remove(workerId);
        if(removed!=null){
            removed.stopSendingHeartBeat();
            synchronized (clientDestinationsGuard){
                workersCommunicationClient.removeDestination(removed.getDestination());
            }
        }
    }



    private Result<Destination> preCreateConnectionsToWorker(String address, int port){
        System.out.println(String.format("http://%s:%d/", address, port));
        Destination destination;
        synchronized (clientDestinationsGuard) {
            Request request = workersCommunicationClient.newRequest(String.format("http://%s:%d/", address, port));
            destination = workersCommunicationClient.resolveDestination(request);
        }
        // one for heartbeat, one for control
        int preCreate = 2;
        CompletableFuture<Void> completable = destination.getConnectionPool().preCreateConnections(preCreate);

        // Wait for the connections to be created.
        try {
            completable.get(5, TimeUnit.SECONDS);
            return Result.success(destination);
        }catch(ExecutionException | TimeoutException errorException){
            return Result.failure("Could not create connection to worker.");
        }catch(InterruptedException e){
            return Result.failure("Interrupted");
        }
    }

    public GenerationWorker getWorkerById(String id){
        return workersById.get(id);
    }

    public void putWorkerResults(String workerID, WorkerResults results){
        resultsByWorkerID.put(workerID, results);
        resultsLatch.countDown();
    }


}
