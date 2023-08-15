package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.sql.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
        Properties config = new Properties();
        config.load(in);
        Class.forName(config.getProperty("jdbc.driver"));
        try (Connection cn = DriverManager.getConnection(
                config.getProperty("jdbc.url"),
                config.getProperty("jdbc.username"),
                config.getProperty("jdbc.password"))) {
//        try {
//            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
//            data.put("store", store);
            data.put("connect", cn);
//            JobDetail job = newJob(Rabbit.class).build();
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
//            SimpleScheduleBuilder times = simpleSchedule()
////                    .withIntervalInSeconds(10)
////                    .withIntervalInSeconds(getTime())
//                    .withIntervalInSeconds(5)
//                    .repeatForever();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(config.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);;
            scheduler.shutdown();
//            System.out.println(store);
//        } catch (SchedulerException se) {
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static int getTime() {
//        Properties config = new Properties();
//        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
//            config.load(in);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Integer.parseInt(config.getProperty("rabbit.interval"));
//    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            //            List<Long> store = (List<Long>) context.getJobDetail().getJobDataMap().get("store");
            //            store.add(System.currentTimeMillis());
            Connection c = (Connection) context.getJobDetail().getJobDataMap().get("connect");
            try (PreparedStatement pr = c.prepareStatement("insert into rabbit(created_date) values (?)")) {
                pr.setDate(1, new Date(System.currentTimeMillis()));
                pr.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}