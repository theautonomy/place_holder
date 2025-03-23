package com.weili.datasource.domain.schedule;

import java.util.List;

import com.weili.datasource.domain.employee.EmployeeDetailsNotFoundException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    private final String DROPTABLE = """
            DROP TABLE IF EXISTS SCHEDULE
            """;
    private final String CREATETABLE =
            """
            CREATE TABLE SCHEDULE (
                SCHEDULEID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
                NAME CHARACTER VARYING(255),
                CONTACT CHARACTER VARYING(255),
                PRIMARY KEY (SCHEDULEID));
            """;

    @Transactional
    public void recreateTable() {
        entityManager.createNativeQuery(DROPTABLE).executeUpdate();
        jdbcTemplate.execute(CREATETABLE);
    }

    public List<Schedule> getAllEmployeeDetails() {
        try {
            return scheduleRepository.findAll();
        } catch (RuntimeException ex) {
            throw new EmployeeDetailsNotFoundException(
                    "Could not find all employee details " + ex.getMessage());
        }
    }

    @Transactional
    public void insertSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }
}
