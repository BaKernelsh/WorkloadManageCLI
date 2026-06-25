package org.example.workloadmanager;

import lombok.Getter;
import lombok.Setter;
import org.example.workloadmanager.Network.ResultSending.IResultServer;
import org.example.workloadmanager.Network.ResultSending.ResultsServer;

@Getter @Setter
public class ProgramConfig {

    public static long heartbeatIntervalMs = 500;

    public static String resultServerImpl = "grpc";
    public static int resultServerPort = 12344;
    public static IResultServer getNewResultServerImpl(WorkloadGenerationContext generationContext){
        if(resultServerImpl.equals("grpc"))
            return new ResultsServer(resultServerPort, generationContext);
        return null;
    }



}
