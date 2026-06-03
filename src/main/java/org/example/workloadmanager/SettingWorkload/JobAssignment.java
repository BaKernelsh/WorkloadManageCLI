package org.example.workloadmanager.SettingWorkload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

@ToString
public class JobAssignment {


    private HashMap<String, WorkerDefinition> workersById;


    public static JobAssignment fromJson(String json){

        HashMap<String, WorkerDefinition> workersById = new HashMap<>();

        try {
            ObjectNode root = new ObjectMapper().readTree(json).asObject();
            for(Map.Entry<String, JsonNode> property : root.properties()){
                String workerID = property.getKey();
                //if(workerIDToOpConfigs.containsKey(workerID)) //powtorka - blad
                ObjectNode workerConfig = property.getValue().asObject();

                String address = workerConfig.get("address").asString(); //TODO walidacja
                Integer port = workerConfig.get("port").asInt();

                WorkerDefinition workerDef = new WorkerDefinition();
                workerDef.setWorkerID(workerID);
                workerDef.setAddress(address);
                workerDef.setPort(port);

                ObjectNode workerOperations = workerConfig.get("ops").asObject();
                for(Map.Entry<String, JsonNode> operation : workerOperations.properties()){
                    String opID = operation.getKey();
                    ObjectNode opConfigFromJson = operation.getValue().asObject();
                    WorkloadConfigForWorker.OperationConfigForWorker opConfig =
                            WorkloadConfigForWorker.OperationConfigForWorker.builder()
                                    .operationID(opID)
                                    .frequency(opConfigFromJson.get("frequency").asString())
                                    .threadNumber(opConfigFromJson.get("threadNumber").asInt())
                                    .addTimeBetweenRequests(opConfigFromJson.get("addTimeBetweenRequests").asBoolean())
                            .build();

                    workerDef.addOperationConfig(opConfig);
                }

                workersById.put(workerID, workerDef);
            }

            JobAssignment assignment = new JobAssignment();
            assignment.workersById = workersById;
            return assignment;

        }catch(JacksonException e){
            return null;
        }

    }

    public HashMap<String, WorkloadConfigForWorker.OperationConfigForWorker> getWorkerOpsAsMap(String workerID){

        HashMap<String, WorkloadConfigForWorker.OperationConfigForWorker> opIdToOpConfig = new HashMap<>();
        for(WorkloadConfigForWorker.OperationConfigForWorker opConfig : workersById.get(workerID).getOperationsConfigs())
            opIdToOpConfig.put(opConfig.getOperationID(), opConfig);
        return opIdToOpConfig;
    }

    public Set<String> getWorkerIds(){
        return workersById.keySet();
    }

    public List<WorkerDefinition> getWorkers(){
        return List.copyOf(workersById.values());
    }

}
