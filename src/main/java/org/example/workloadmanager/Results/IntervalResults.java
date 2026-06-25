package org.example.workloadmanager.Results;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntervalResults {
    @Getter
    private final int intervalID;
    @Getter
    private final String workerID;
    private HashMap<String, List<ISingleOperationResultWrapper>> resultsByOpID;


    public static IntervalResults fromProtobuf(org.example.workloadmanager.Network.ResultSending.IntervalResults intervalResults, Long clockOffset){
        return new IntervalResults(intervalResults, clockOffset);
    }

    private IntervalResults(org.example.workloadmanager.Network.ResultSending.IntervalResults intervalResults, Long clockOffset){
        this.intervalID = Integer.valueOf(intervalResults.getIntervalId());
        this.workerID = intervalResults.getWorkerId();

        this.resultsByOpID = new HashMap<>();
        intervalResults.getResultsByOpIdMap().forEach((opId, opResults) -> {
            List<ISingleOperationResultWrapper> resultlist = new ArrayList<>();
            opResults.getResultsList().forEach(result -> {
                resultlist.add(SingleOperationWrapperProtobuf.createAndCalculateProperties(result, clockOffset));
            });
            resultsByOpID.put(opId, resultlist);
        });
    }



    public HashMap<String, List<ISingleOperationResultWrapper>> getResults() {
        return resultsByOpID;
    }
}
