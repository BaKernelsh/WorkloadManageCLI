package org.example.workloadmanager.Results;

import org.example.workloadmanager.Network.ResultSending.OperationResult;


public class SingleOperationWrapperProtobuf implements ISingleOperationResultWrapper{

    private final OperationResult result;
    private Long adjustedOperationEndTime;
    private Long operationDurationNs;

    public SingleOperationWrapperProtobuf(OperationResult result){
        this.result = result;
    }

    public static ISingleOperationResultWrapper createAndCalculateProperties(OperationResult result, Long clockOffset){
        SingleOperationWrapperProtobuf wrapper = new SingleOperationWrapperProtobuf(result);
        wrapper.calcAndSetOperationDuration();
        wrapper.calcAndSetAdjustedOperationEndTime(clockOffset);
        return wrapper;
    }

    public void calcAndSetAdjustedOperationEndTime(Long offset){
        adjustedOperationEndTime = result.getEndTime() + offset;
    }

    public Long calcAndSetOperationDuration(){
        operationDurationNs = result.getEndTime() - result.getStartTime();
        return operationDurationNs;
    }

    @Override
    public Long getStartTime() {
        return result.getStartTime();
    }

    @Override
    public Long getEndTime() {
        return result.getEndTime();
    }

    @Override
    public Long getAdjustedEndTime() {
        return adjustedOperationEndTime;
    }

    @Override
    public Long getOperationDuration() {
        return operationDurationNs;
    }

    @Override
    public String getError() {
        return result.getError();
    }

}
