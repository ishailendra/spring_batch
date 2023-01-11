package com.shail.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("product.productJob")
    private Job productJob;

    @Autowired
    @Qualifier("fileImport.productFileImportJob")
    private Job productFileImportJob;

    @Scheduled(initialDelay = 5000, fixedDelay = 1800000)//cron = "0 0/2 * * * *"
    public void scheduler() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Long start = System.currentTimeMillis();

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String  formattedDate = localDate.format(dateFormatter);

//        LocalTime localTime = LocalTime.now();
//        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
//        String formattedTime = localTime.format(timeFormatter);

//        String outputFile = "src/main/resources/data/product_" + formattedDate + "_" + formattedTime + ".txt";
//        String outputFile = "src/main/resources/data/product_" + formattedDate + ".txt";

//        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
//        jobParametersBuilder.addString("outputFile", outputFile);
//        jobParametersBuilder.addDate("date", new Date());
//        jobLauncher.run(productJob, jobParametersBuilder.toJobParameters());

        String inputFile = "src/main/resources/data/product_" + formattedDate + ".txt";
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("inputFile", inputFile);
        jobParametersBuilder.addDate("date", new Date());
        jobLauncher.run(productFileImportJob, jobParametersBuilder.toJobParameters());

        Long end = System.currentTimeMillis();

        System.out.println("**********Time taken to complete job: " + (end - start)/1000 + " **********");
    }
}
