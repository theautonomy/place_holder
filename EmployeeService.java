package com.weili.datasource.domain.employee;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.weili.datasource.config.DataSourceContextHolder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    private final Map<String, DataSource> datasources;

    private final String DROPTABLE = """
            DROP TABLE IF EXISTS employee
            """;
    private final String CREATETABLE =
            """
            CREATE TABLE EMPLOYEE (
                EMPLOYEEID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                EMPLOYEENAME CHARACTER VARYING(255),
                EMPLOYEEROLE CHARACTER VARYING(255),
                PRIMARY KEY (EMPLOYEEID));
            """;

    @Transactional
    public void recreateTable() {
        entityManager.createNativeQuery(DROPTABLE).executeUpdate();
        jdbcTemplate.execute(CREATETABLE);
    }

    public List<Employee> getAllEmployeeDetailsWithJDBCTemplate(String dataSource) {
        return new JdbcTemplate(datasources.get(dataSource))
                .query(
                        "select * from employee",
                        (rs, rowNum) -> {
                            Employee e = new Employee();
                            e.setEmployeeId(rs.getInt("employeeId"));
                            e.setEmployeeName(rs.getString("employeeName"));
                            e.setEmployeeRole(rs.getString("employeeRole"));
                            return e;
                        });
    }

    public List<Employee> getAllEmployeeDetails() {
        try {
            return employeeRepository.findAll();
        } catch (RuntimeException ex) {
            throw new EmployeeDetailsNotFoundException(
                    "Could not find all employee details " + ex.getMessage());
        }
    }

    // The following annotation causes unexpected behavior
    // even though the transaction is read only.

    // Any transaction can NOT cross data sources
    // @Transactional(readOnly = true)
    public void listRecordsFromBoth() {
        DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");
        getAllEmployeeDetails().stream().forEach(System.out::println);

        DataSourceContextHolder.setBranchContext("DATASOURCE_TWO");
        getAllEmployeeDetails().stream().forEach(System.out::println);
    }

    @Transactional
    // default transaction level is read-write
    // same as: @Transactional(readOnly = false)
    public void save(Employee employee) {
        employeeRepository.save(employee);
    }

    // The following annotation causes unexpected behavior
    // as we are trying to cross data sources
    //
    // @Transactional(readOnly = false)
    public void insertToBothDatasources() {
        DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");
        Employee e1 = new Employee();
        e1.setEmployeeName("Employee1 in Datasource 1");
        e1.setEmployeeRole("DS1");
        employeeRepository.save(e1);

        DataSourceContextHolder.setBranchContext("DATASOURCE_TWO");
        System.out.println("\n\n====> Before insert - select all records from source two");
        System.out.println("---------------------");
        getAllEmployeeDetails().stream().forEach(System.out::println);

        DataSourceContextHolder.setBranchContext("DATASOURCE_TWO");
        Employee e2 = new Employee();
        e2.setEmployeeName("Employee2 in Datasource 2");
        e2.setEmployeeRole("DS2");
        employeeRepository.save(e2);
    }

    // Important - Caller to set the data source before a method that is annotated with
    // @Transactional
    // The following annotation works as long as we stay on one data source
    @Transactional(readOnly = false)
    public void insertToDatasourceOne() {

        // Don't do this here
        // This caused unexpected behavior
        // DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");

        System.out.println("\n\n ====> Before insert - select all records from source one");
        System.out.println("---------------------");
        getAllEmployeeDetails().stream().forEach(System.out::println);

        // Don't do this here
        // This caused unexpected behavior
        // DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");

        Employee e1 = new Employee();
        e1.setEmployeeName("Employee1 in Datasource 1");
        e1.setEmployeeRole("DS1");
        employeeRepository.save(e1);

        Employee e2 = new Employee();
        e2.setEmployeeName("Employee1 in Datasource 1");
        e2.setEmployeeRole("DS1");
        employeeRepository.save(e2);

        System.out.println("\n\n ====> After insert - select all records from source one");
        System.out.println("---------------------");
        getAllEmployeeDetails().stream().forEach(System.out::println);
    }

    // Important - Caller to set the data source before a method that is annotated with
    // @Transactional

    // The following annotation works as long as we
    //  * pre set the data source
    //  * stay on one data source
    @Transactional(readOnly = false)
    public void insertToDatasourceOneFailAndRollback() {
        // Don't do this here
        // DataSourceContextHolder.setBranchContext("DATASOURCE_ONE");

        Employee e1 = new Employee();
        e1.setEmployeeName("Employee1 in Datasource 1");
        e1.setEmployeeRole("DS1");
        employeeRepository.save(e1);

        // this insert fails during to primary key violation
        // the previous insert rolls back as well
        Employee e2 = new Employee();
        e1.setEmployeeId(1);
        e2.setEmployeeName("Employee1 in Datasource 1");
        e2.setEmployeeRole("DS1");
        employeeRepository.save(e2);
    }
}
