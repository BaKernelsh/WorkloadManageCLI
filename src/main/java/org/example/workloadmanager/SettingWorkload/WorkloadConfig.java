package org.example.workloadmanager.SettingWorkload;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;


@ToString
public class WorkloadConfig {
    @Getter @Setter
    private String workloadID;
    @Getter @Setter
    private Long sendResultIntervalMs;
    @Getter @Setter
    private Long workloadDurationMs;
    @Getter @Setter
    private boolean sendResultsInIntervals;
    @Setter
    private HashMap<String, String> opIDtoOpDescription;


    public static WorkloadConfig fromJson(String json) {
        HashMap<String, String> opIDtoOpDescription = new HashMap<>();

        try{
            ObjectNode root = new ObjectMapper().readTree(json).asObject();
            String workloadID = root.get("workloadID").asString();
            Long sendResultIntervalMs = root.get("sendResultIntervalMs").asLong();
            Long workloadDurationMs = root.get("workloadDurationMs").asLong();

            //ObjectNode operations = root.get("operations").asObject();
            putOperationsDescriptionStringsInMap(root.get("operations").asObject(), opIDtoOpDescription);

            WorkloadConfig config = new WorkloadConfig();
            config.setWorkloadID(workloadID);
            config.setWorkloadDurationMs(workloadDurationMs);
            config.setSendResultIntervalMs(sendResultIntervalMs);
            config.setOpIDtoOpDescription(opIDtoOpDescription);
            return config;

        }catch(JacksonException e){
            e.printStackTrace();
            return null;
        }
    }

    private static void putOperationsDescriptionStringsInMap(ObjectNode operations, HashMap<String, String> map){
        for(Map.Entry<String, JsonNode> operation : operations.properties()){
            map.put(operation.getKey(), operation.getValue().toString());
        }
    }

    public HashMap<String, WorkloadConfigForWorker> assignWorkloadConfigsForWorkers(JobAssignment assignment){
        HashMap<String, WorkloadConfigForWorker> workerIDtoConfig = new HashMap<>();
        for(String workerID : assignment.getWorkerIds()){

            HashMap<String, WorkloadConfigForWorker.OperationConfigForWorker> workerOpsAsMap = assignment.getWorkerOpsAsMap(workerID);

            for(WorkloadConfigForWorker.OperationConfigForWorker workerOp : workerOpsAsMap.values())
                workerOp.setDescriptionJson(opIDtoOpDescription.get(workerOp.getOperationID()));

            WorkloadConfigForWorker workerConfig = new WorkloadConfigForWorker(workerOpsAsMap,
                                                        sendResultIntervalMs,
                                                        workloadDurationMs,
                                                        sendResultsInIntervals,
                                                        workerID);
            workerIDtoConfig.put(workerID, workerConfig);
        }
        return workerIDtoConfig;
    }


}
