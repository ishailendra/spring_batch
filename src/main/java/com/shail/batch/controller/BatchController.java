//package com.shail.batch.controller;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.JobParametersInvalidException;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//
////@RestController
//public class BatchController {
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    @Qualifier("product.productJob")
//    private Job productJob;
//
//    @GetMapping("trigger")
//    public String triggerJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
//        System.err.println("**************** JOB STARTUP ****************");
//        Long start = System.currentTimeMillis();
//
//        LocalDate localDate = LocalDate.now();
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
//        String  formattedDate = localDate.format(dateFormatter);
//
////        LocalTime localTime = LocalTime.now();
////        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
////        String formattedTime = localTime.format(timeFormatter);
//
////        String outputFile = "src/main/resources/data/product_" + formattedDate + "_" + formattedTime + ".txt";
//        String outputFile = "src/main/resources/data/product_" + formattedDate + ".txt";
//
//        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
//        jobParametersBuilder.addString("outputFile", outputFile);
//        jobParametersBuilder.addDate("date", new Date());
//        jobLauncher.run(productJob, jobParametersBuilder.toJobParameters());
//
//        Long end = System.currentTimeMillis();
//
//        System.out.println("**********Time taken to complete job: " + (end - start)/1000 + " **********");
//
//        return "SUCCESS";
//    }
//}
