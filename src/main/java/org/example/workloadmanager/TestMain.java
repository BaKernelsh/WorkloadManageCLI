package org.example.workloadmanager;

import org.example.workloadmanager.Results.Results;
import org.example.workloadmanager.Results.Saving.CSVResultSaver;
import org.example.workloadmanager.Results.Saving.IResultSaver;
import org.example.workloadmanager.SettingWorkload.JobAssignment;
import org.example.workloadmanager.SettingWorkload.WorkerDefinition;
import org.example.workloadmanager.SettingWorkload.WorkloadConfig;
import org.example.workloadmanager.SettingWorkload.WorkloadConfigForWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java -jar JarName \"path to workload.json\" \"path to assign.json\" \"port\"");
            return;
        }

        String workloadDescriptionJson = Files.readString(Path.of(args[0]));
        WorkloadConfig workloadConfig = WorkloadConfig.fromJson(workloadDescriptionJson);
        System.out.println(workloadConfig);

        String jobAssignmentJson = Files.readString(Path.of(args[1]));
        JobAssignment jobAssignment = JobAssignment.fromJson(jobAssignmentJson);
        System.out.println(jobAssignment);

        if (args.length == 3) {
            int resultServerPort = Integer.valueOf(args[2]);
            ProgramConfig.resultServerPort = resultServerPort;
        }

        HashMap<String, WorkloadConfigForWorker> workerWorkloadConfigs = workloadConfig.assignWorkloadConfigsForWorkers(jobAssignment);
        for(var e : workerWorkloadConfigs.entrySet()){
            System.out.println(e.getKey() + " : " + e.getValue().toJson());
        }

        try {
            WorkloadGenerationContext generationContext = new WorkloadGenerationContext();
            Thread.sleep(3000);

            Scanner scanner = new Scanner(System.in);

            for(WorkerDefinition workerDef : jobAssignment.getWorkers()){
                Result<Void> createWorkerResult = generationContext.addWorker(workerDef.getWorkerID(), workerDef.getAddress(), workerDef.getPort());
                if(!createWorkerResult.isSuccess()) {
                    System.out.println("Couldn't connect to worker " + workerDef.getWorkerID() + " at " + workerDef.getAddress() + ":" + workerDef.getPort() + ".\n" +
                            "Continue ? [y\\n]");
                    if(scanner.nextLine().equalsIgnoreCase("y"))
                        continue;
                    else
                        System.exit(-1);
                }
            }

            Long sendResultIntervalNanos = workloadConfig.getSendResultIntervalMs() <=0 ?
                    workloadConfig.getSendResultIntervalMs() : workloadConfig.getSendResultIntervalMs() * 1_000_000;
            generationContext.setWorkloadParams(sendResultIntervalNanos,
                    workloadConfig.getWorkloadDurationMs() * 1_000_000,
                    0L);

            generationContext.sendConfigurationToWorkersAsync(workerWorkloadConfigs)
                    .thenAccept(results -> {

                        for (SendToWorkerResult result : results) {

                            if (!result.getResult().isSuccess()) {
                                System.out.printf(
                                        "Worker %s failed: %s%n",
                                        result.getWorkerId(),
                                        result.getResult().getErrorMessage());
                            }
                        }
                    }).join();

            generationContext.startWorkload()
                    .thenAccept(results -> {

                        for (SendToWorkerResult result : results) {

                            if (!result.getResult().isSuccess()) {
                                System.out.printf(
                                        "Worker %s failed starting: %s%n",
                                        result.getWorkerId(),
                                        result.getResult().getErrorMessage());
                            }
                        }
                    });

            while(generationContext.getWorkloadStarted().get())
                Thread.sleep(500);
            System.out.println("glowny - koniec generacji");

            if(generationContext.getResultsLatch().await(15, TimeUnit.SECONDS)){
                Results results = new Results(generationContext.getWorkersResults());

                IResultSaver saver = new CSVResultSaver(Path.of("results"));
                Result<Exception> saveResult =  saver.save(results);
                if(saveResult.isSuccess())
                    System.out.println("results saved");
                else {
                    System.err.println("error in saving results");
                    saveResult.getValue().printStackTrace();
                }
            }

            scanner.nextLine();
            scanner.close();




        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }



        //System.out.println(workloadConfig);
    }
}
