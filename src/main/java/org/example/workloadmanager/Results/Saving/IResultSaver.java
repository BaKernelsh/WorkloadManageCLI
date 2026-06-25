package org.example.workloadmanager.Results.Saving;

import org.example.workloadmanager.Result;
import org.example.workloadmanager.Results.Results;


import java.io.IOException;

public interface IResultSaver {

    public Result<Exception> save(Results results);
}
