package org.example.workloadmanager.Results;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.example.workloadmanager.Util.MapHelpers.addAllToListInMapOrCreateNewList;

public class Results {

    @Getter
    private List<ISingleOperationResultWrapper> allResults = new ArrayList<>();
    @Getter
    private HashMap<String, List<ISingleOperationResultWrapper>> resultsByOpID = new HashMap<>();
    @Getter
    private HashMap<String, List<ISingleOperationResultWrapper>> resultsByWorkerID = new HashMap<>();


    public Results(HashMap<String, WorkerResults> resultsByWorkerID){
        resultsByWorkerID.forEach((workerID, singleWorkerResults) -> {
            allResults.addAll(singleWorkerResults.getAllResults());

            addAllToListInMapOrCreateNewList(this.resultsByWorkerID, workerID, singleWorkerResults.getAllResults());

            singleWorkerResults.getResultsByOpID().forEach((opId, opResults) -> {
                addAllToListInMapOrCreateNewList(resultsByOpID, opId, opResults);
            });

        });
    }

}
