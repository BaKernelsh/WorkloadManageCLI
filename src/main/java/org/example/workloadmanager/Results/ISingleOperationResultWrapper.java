package org.example.workloadmanager.Results;

public interface ISingleOperationResultWrapper {

    public void calcAndSetAdjustedOperationEndTime(Long offset);
    public Long calcAndSetOperationDuration();

    public Long getStartTime();
    public Long getEndTime();
    public Long getAdjustedEndTime();
    public Long getOperationDuration();
    public String getError();

}
