package org.example.workloadmanager.SettingWorkload;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class WorkerDefinition {
    @Getter @Setter
    private String workerID;
    @Getter @Setter
    private String address;
    @Getter @Setter
    private Integer port;
    @Getter
    private List<WorkloadConfigForWorker.OperationConfigForWorker> operationsConfigs;

    public WorkerDefinition(){
        this.operationsConfigs = new ArrayList<>();
    }

    public void addOperationConfig(WorkloadConfigForWorker.OperationConfigForWorker opConfig){
        operationsConfigs.add(opConfig);
    }

}
