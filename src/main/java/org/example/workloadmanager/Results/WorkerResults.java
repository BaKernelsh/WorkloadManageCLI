package org.example.workloadmanager.Results;

import lombok.Getter;
import org.example.workloadmanager.Network.ResultSending.AllResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.example.workloadmanager.Util.MapHelpers.addAllToListInMapOrCreateNewList;


public class WorkerResults {

    @Getter
    private List<ISingleOperationResultWrapper> allResults;
    @Getter
    private HashMap<String, List<ISingleOperationResultWrapper>> resultsByOpID;


    public static WorkerResults empty(){
        return new WorkerResults(new ArrayList<>(), new HashMap<>());
    }

    public static WorkerResults fromProtobuf(AllResults allResultsProtobuf, Long[] timeOffsets){
        List<ISingleOperationResultWrapper> allResults = new ArrayList<>();
        HashMap<String, List<ISingleOperationResultWrapper>> resultsByOpID = new HashMap<>();


        allResultsProtobuf.getResultsList().forEach(intervalResults -> {
            int intervalID = Integer.valueOf(intervalResults.getIntervalId());
            String workerID = intervalResults.getWorkerId();
            Long thisIntervalOffset;

            if(timeOffsets!=null && timeOffsets.length != 0){
                if(isIntervalIdOutOfRange(intervalID, timeOffsets))
                    thisIntervalOffset = timeOffsets[timeOffsets.length-1];
                else
                    thisIntervalOffset = timeOffsets[intervalID];
            }
            else{
                System.err.println("worker offsets array was null");
                thisIntervalOffset = 0L;
            }
            if(thisIntervalOffset == null)
                thisIntervalOffset = 0L;

            IntervalResults intervResultsImpl = IntervalResults.fromProtobuf(intervalResults, thisIntervalOffset);

            intervResultsImpl.getResults().forEach((opId, opResults) ->{
                allResults.addAll(opResults);

                addAllToListInMapOrCreateNewList(resultsByOpID, opId, opResults);
            });
        });

        return new WorkerResults(allResults, resultsByOpID);
    }

    private WorkerResults(List<ISingleOperationResultWrapper> allResults,
                          HashMap<String, List<ISingleOperationResultWrapper>> resultsByOpID){
        this.allResults = allResults;
        this.resultsByOpID = resultsByOpID;
    }

    private static boolean isIntervalIdOutOfRange(Integer intervalID, Long[] offsets){
        return offsets.length - 1 < intervalID;
    }



}
