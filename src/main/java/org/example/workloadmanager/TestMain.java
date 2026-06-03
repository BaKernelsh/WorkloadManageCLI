package org.example.workloadmanager;

import org.example.workloadmanager.SettingWorkload.JobAssignment;
import org.example.workloadmanager.SettingWorkload.WorkerDefinition;
import org.example.workloadmanager.SettingWorkload.WorkloadConfig;
import org.example.workloadmanager.SettingWorkload.WorkloadConfigForWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class TestMain {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Main <file>");
            return;
        }

        String workloadDescriptionJson = Files.readString(Path.of(args[0]));
        WorkloadConfig workloadConfig = WorkloadConfig.fromJson(workloadDescriptionJson);
        System.out.println(workloadConfig);

        String jobAssignmentJson = Files.readString(Path.of(args[1]));
        JobAssignment jobAssignment = JobAssignment.fromJson(jobAssignmentJson);
        System.out.println(jobAssignment);

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
                    });

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

            scanner.nextLine();
            scanner.close();




        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }



        //System.out.println(workloadConfig);
    }
}
