package org.example.workloadmanager.SettingWorkload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

@Builder
public class WorkloadConfigForWorker {

    private HashMap<String, OperationConfigForWorker> operationIDToConfig;
    private Long sendResultIntervalMs;
    private Long workloadDurationMs;
    private boolean sendResultsInIntervals;
    private String workerId;

    public String toJson(){

        ObjectNode root = new ObjectNode(JsonNodeFactory.instance);
        root.put("workerId", workerId);
        root.put("sendResultIntervalMs", sendResultIntervalMs);
        root.put("workloadDurationMs", workloadDurationMs);
        root.put("sendResultsInIntervals", sendResultsInIntervals);

        ObjectNode opsObjectNode = root.putObject("operations");
        for(Map.Entry<String, OperationConfigForWorker> opToConfigEntry : operationIDToConfig.entrySet()){
            ObjectNode thisOpObjectNode = opsObjectNode.putObject(opToConfigEntry.getKey());
            addOperationFields(thisOpObjectNode, opToConfigEntry.getValue());
        }
        return root.toString();
    }

    private void addOperationFields(ObjectNode opObjectNode, OperationConfigForWorker opConfig){
        opObjectNode.put("frequency", opConfig.getFrequency());
        opObjectNode.put("threadNumber", opConfig.getThreadNumber());
        opObjectNode.put("addTimeBetweenRequests", opConfig.isAddTimeBetweenRequests());
        opObjectNode.put("operationDescription", opConfig.getDescriptionJson());
    }

    @ToString
    @Builder
    public static class OperationConfigForWorker {
        @Getter
        private String operationID;
        @Getter
        private String frequency;
        @Getter
        private int threadNumber;
        @Getter
        private boolean addTimeBetweenRequests;
        @Getter @Setter
        private String descriptionJson;

    }

}
