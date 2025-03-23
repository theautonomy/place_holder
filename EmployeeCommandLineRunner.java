package com.weili.datasource;

import java.util.List;

import com.weili.datasource.config.DataSourceContextHolder;
import com.weili.datasource.domain.employee.Employee;
import com.weili.datasource.domain.employee.EmployeeService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class EmployeeCommandLineRunner implements CommandLineRunner {

    private final EmployeeService employeeService;

    public EmployeeCommandLineRunner(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public void run(String... args) throws Exception {
        // switch to use data source one
        DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");
        System.out.println("\n\nSelect all records from source one");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nInsert one record to data source one");
        System.out.println("---------------------");
        Employee e1 = new Employee();
        e1.setEmployeeName("Employee1 in Datasource 1");
        e1.setEmployeeRole("DS1");
        employeeService.save(e1);

        System.out.println("\n\nSelect all records from source one");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        // switch to use data source two
        DataSourceContextHolder.setBranchContext("DATASOURCE_TWO");
        System.out.println("\n\nSelect all records from source two");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nInsert to one data source two");
        System.out.println("---------------------");
        Employee e2 = new Employee();
        e2.setEmployeeName("Employee2 in Datasource 2");
        e2.setEmployeeRole("DS2");
        employeeService.save(e2);

        System.out.println("\n\nSelect all records from source two");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        DataSourceContextHolder.setBranchContext("DATASOURCE_THREE");
        System.out.println("\n\nSelect all records from source three");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nInsert one record to data source three");
        System.out.println("---------------------");
        Employee e3 = new Employee();
        e3.setEmployeeName("Employee3 in Datasource 3");
        e3.setEmployeeRole("DS3");
        employeeService.save(e3);

        System.out.println("\n\nSelect all records from source three");
        System.out.println("---------------------");
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nSelect records from both data sources");
        System.out.println("---------------------");
        employeeService.listRecordsFromBoth();

        System.out.println("\n\nInsert two records to data source one");
        System.out.println("---------------------");

        // Important - set the data source before a method that is annotated with @Transactional
        DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");
        employeeService.insertToDatasourceOne();

        System.out.println("\n\nSelect records from both data sources");
        System.out.println("---------------------");
        employeeService.listRecordsFromBoth();

        System.out.println(
                "\n\nFail to insert records to data source one - rollback in both data sources");
        System.out.println("---------------------");
        DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");
        try {
            employeeService.insertToDatasourceOneFailAndRollback();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n\nSelect records from both data sources (one and two)");
        System.out.println("---------------------");
        employeeService.listRecordsFromBoth();

        System.out.println("\n\nInsert one record to two data sources (one and two)");
        System.out.println("---------------------");
        employeeService.insertToBothDatasources();

        System.out.println("\n\nSelect records from both data sources (one and two)");
        System.out.println("---------------------");
        employeeService.listRecordsFromBoth();

        System.out.println("\n\nSelect all records from default data source");
        System.out.println("---------------------");
        DataSourceContextHolder.clearBranchContext();
        employeeService.getAllEmployeeDetails().stream().forEach(System.out::println);

        System.out.println("\n\nSelect all records from all data sources using JDBCTemplate");
        System.out.println("---------------------");
        List.of("DATASOURCE_ONE", "DATASOURCE_TWO", "DATASOURCE_THREE")
                .forEach(
                        s -> {
                            System.out.println("\n====> From table %s".formatted(s));
                            employeeService.getAllEmployeeDetailsWithJDBCTemplate(s).stream()
                                    .forEach(System.out::println);
                        });

        // Recreate table after test
        List.of("DATASOURCE_ONE", "DATASOURCE_TWO", "DATASOURCE_THREE")
                .forEach(
                        s -> {
                            DataSourceContextHolder.setBranchContext(s);
                            employeeService.recreateTable();
                        });
    }
}
