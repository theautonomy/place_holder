package com.weili.datasource;

import java.util.List;

import com.weili.datasource.config.DataSourceContextHolder;
import com.weili.datasource.domain.schedule.Schedule;
import com.weili.datasource.domain.schedule.ScheduleService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ScheduleCommandLineRunner implements CommandLineRunner {

    private final ScheduleService scheduleService;

    public ScheduleCommandLineRunner(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Override
    public void run(String... args) throws Exception {
        // switch to use data source one
        DataSourceContextHolder.setBranchContext("DATASOURCE_TWO");
        System.out.println("\n\nSelect all schedule records from source two");
        System.out.println("---------------------");
        scheduleService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nInsert one schedule record to data source two");
        System.out.println("---------------------");
        Schedule schedule = new Schedule();
        schedule.setName("Schedule");
        schedule.setContact("DS2");
        scheduleService.insertSchedule(schedule);

        System.out.println("\n\nSelect all schedule records from source two");
        System.out.println("---------------------");
        scheduleService.getAllEmployeeDetails().stream().forEach(System.out::println);

        // Recreate table after test
        List.of("DATASOURCE_ONE", "DATASOURCE_TWO", "DATASOURCE_THREE")
                .forEach(
                        s -> {
                            DataSourceContextHolder.setBranchContext(s);
                            scheduleService.recreateTable();
                        });
    }
}
