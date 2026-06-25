package org.example.workloadmanager.Results.Saving;

import org.example.workloadmanager.Result;
import org.example.workloadmanager.Results.ISingleOperationResultWrapper;
import org.example.workloadmanager.Results.Results;
import org.example.workloadmanager.Results.WorkerResults;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


public class CSVResultSaver implements IResultSaver{

    private final Path saveDirectory;

    public CSVResultSaver(Path saveDirectory){
        this.saveDirectory = saveDirectory;
    }

    @Override
    public Result<Exception> save(Results results){
        try {
            Files.createDirectories(saveDirectory);

            // Save all results
            saveList(
                    saveDirectory.resolve("allResults.csv"),
                    results.getAllResults()
            );

            // Save grouped by operation ID
            saveGrouped(
                    saveDirectory.resolve("resultsByOpID"),
                    results.getResultsByOpID()
            );

            // Save grouped by worker ID
            saveGrouped(
                    saveDirectory.resolve("resultsByWorkerID"),
                    results.getResultsByWorkerID()
            );

            return Result.success(null);
        }catch(Exception e){
            return Result.failure(e, e.getMessage());
        }
    }

    private void saveGrouped(
            Path directory,
            Map<String, List<ISingleOperationResultWrapper>> groupedResults
    ) throws IOException {

        Files.createDirectories(directory);

        for (Map.Entry<String, List<ISingleOperationResultWrapper>> entry : groupedResults.entrySet()) {
            String fileName = sanitize(entry.getKey()) + ".csv";

            saveList(
                    directory.resolve(fileName),
                    entry.getValue()
            );
        }
    }

    private void saveList(
            Path file,
            List<ISingleOperationResultWrapper> results
    ) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {

            writer.write(
                    "startTime,endTime,adjustedEndTime,operationDurationNs,error"
            );
            writer.newLine();

            for (ISingleOperationResultWrapper result : results) {
                writer.write(toCsvRow(result));
                writer.newLine();
            }
        }
    }

    private String toCsvRow(ISingleOperationResultWrapper result) {
        return String.join(",",
                value(result.getStartTime()),
                value(result.getEndTime()),
                value(result.getAdjustedEndTime()),
                value(result.getOperationDuration()),
                escapeCsv(result.getError())
        );
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private String sanitize(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

