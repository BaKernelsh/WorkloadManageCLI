package org.example.workloadmanager.Network.ResultSending;

import io.grpc.Grpc;import io.grpc.InsecureServerCredentials;import io.grpc.Server;
import io.grpc.ServerBuilder;import io.grpc.stub.StreamObserver;
import lombok.Setter;
import org.example.workloadmanager.Results.WorkerResults;
import org.example.workloadmanager.WorkloadGenerationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ResultsServer implements IResultServer{

    private final int port;
    private final Server server;

    public ResultsServer(int port, WorkloadGenerationContext generationContext) {
        this.port = port;
        ServerBuilder<?> serverBuilder = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
        this.server = serverBuilder.addService(new ResultsService(generationContext)).build();
    }
    @Override
    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ResultsServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    @Override
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }



    private static class ResultsService extends ResultsServiceGrpc.ResultsServiceImplBase{

        private HashMap<String, org.example.workloadmanager.Network.ResultSending.AllResults> protobufResultsByWorkerID = new HashMap<>();

        private final WorkloadGenerationContext generationContext;

        public ResultsService(WorkloadGenerationContext generationContext) {
            this.generationContext = generationContext;
        }

        @Override
        public void sendAllResults(AllResults request, StreamObserver<ManagerResponse> responseObserver){
            responseObserver.onNext(acceptAllResults(request));
            responseObserver.onCompleted();
        }

        private ManagerResponse acceptAllResults(AllResults results){
            String workerID = results.getWorkerId();
            int resultCount = 0;
            if(results.getResultsList().isEmpty()){
                System.out.println("worker dał 0 wynikow");
                generationContext.putWorkerResults(workerID, wrapResults(results, workerID));
                return ManagerResponse.newBuilder().setOk(true).build();
            }
            for(var intervalResult : results.getResultsList()) {
                for(var opResList : intervalResult.getResultsByOpIdMap().values()){
                    resultCount += opResList.getResultsCount();
                }
            };

            System.out.println("Got results from worker "+ workerID + " result count = " + resultCount);

            generationContext.putWorkerResults(workerID, wrapResults(results, workerID));

            return ManagerResponse.newBuilder().setOk(true).build();
        }

        private WorkerResults wrapResults(org.example.workloadmanager.Network.ResultSending.AllResults allResults, String workerID){
            if(allResults.getResultsList().isEmpty())
                return WorkerResults.empty();
            Long[] workerOffsets = generationContext.getWorkerById(workerID).getIntervalOffsets();
            return WorkerResults.fromProtobuf(allResults, workerOffsets);
        }

    }


}
