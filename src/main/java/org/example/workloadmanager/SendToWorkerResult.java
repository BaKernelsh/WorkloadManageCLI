package org.example.workloadmanager;

import lombok.Getter;

@Getter
public class SendToWorkerResult {
    private final String workerId;
    private final Result<Void> result;

    public SendToWorkerResult(String workerId, Result<Void> result) {
        this.workerId = workerId;
        this.result = result;
    }
}
