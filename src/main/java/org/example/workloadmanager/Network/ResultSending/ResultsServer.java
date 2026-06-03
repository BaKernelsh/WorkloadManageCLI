package org.example.workloadmanager.Network.ResultSending;

import io.grpc.Grpc;import io.grpc.InsecureServerCredentials;import io.grpc.Server;
import io.grpc.ServerBuilder;import io.grpc.stub.StreamObserver;
import java.io.IOException;import java.util.concurrent.TimeUnit;

public class ResultsServer implements IResultServer{

    private final int port;
    private final Server server;

    public ResultsServer(int port) {
        this.port = port;
        ServerBuilder<?> serverBuilder = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
        this.server = serverBuilder.addService(new ResultsService()).build();
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

        @Override
        public void sendAllResults(AllResults request, StreamObserver<ManagerResponse> responseObserver){
            responseObserver.onNext(acceptAllResults(request));
            responseObserver.onCompleted();
        }

        private ManagerResponse acceptAllResults(AllResults results){
            int resultCount = 0;
            for(var intervalResult : results.getResultsList()) {
                for(var opResList : intervalResult.getResultsByOpIdMap().values()){
                    resultCount += opResList.getResultsCount();
                }
            };
            System.out.println("Got results from worker "+results.getResults(0).getWorkerId() + " result count = " + resultCount);
            return ManagerResponse.newBuilder().setOk(true).build();
        }

    }



}
