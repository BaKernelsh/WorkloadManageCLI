package org.example.workloadmanager.Network.ResultSending;

import java.io.IOException;

public interface IResultServer {

    public void start() throws IOException;
    public void stop() throws InterruptedException;
}
