package com.medicore.app.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.medicore.app.models.Appointment;
import com.medicore.app.models.ApptmType;
import com.medicore.app.models.Employee;
import com.medicore.app.models.Schedule;
import com.medicore.app.repository.AppointmentRepository;
import com.medicore.app.repository.ApptmTypeRepository;
import com.medicore.app.repository.EmployeeRepository;
import com.medicore.app.repository.ScheduleRepository;
import com.medicore.app.services.AppointmentService;

import jakarta.transaction.Transactional;


@Component
public class ScheduleTaskService {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    private final AppointmentRepository appointmentRepo;
    private final EmployeeRepository employeeRepo;
    private final ScheduleRepository scheduleRepo;
    private final ApptmTypeRepository apptmTypeRepo;
    private final AppointmentService appointmentService;


    public ScheduleTaskService(
            AppointmentRepository appointmentRepo,
            EmployeeRepository employeeRepo,
            ScheduleRepository scheduleRepo,
            ApptmTypeRepository apptmTypeRepo,
            AppointmentService appointmentService) {
        this.appointmentRepo = appointmentRepo;
        this.employeeRepo = employeeRepo;
        this.scheduleRepo = scheduleRepo;
        this.apptmTypeRepo = apptmTypeRepo;
        this.appointmentService = appointmentService;
    }

    //@Scheduled(cron = "0 59 23 * * SUN", zone = "America/Bogota")
    @Transactional
    public void generateWeeklyAppointments() {
        generateWeekIfMissing(LocalDate.now(BOGOTA_ZONE).plusWeeks(1));
    }

    @Transactional
    public void generateCurrentWeekAppointments() {
        generateWeekIfMissing(LocalDate.now(BOGOTA_ZONE));
    }

    private void generateWeekIfMissing(LocalDate referenceDate) {
        LocalDate monday = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        OffsetDateTime weekStart = monday.atStartOfDay().atOffset(ZoneOffset.ofHours(-5));
        OffsetDateTime nextWeekStart = monday.plusWeeks(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-5));

        if (appointmentRepo.existsByDateTimeGreaterThanEqualAndDateTimeLessThan(weekStart, nextWeekStart)) {
            return;
        }

        if (!scheduleRepo.existsByDateBetween(monday, sunday)) {
            createSchedule(monday);
        }
        createAppointments(monday, sunday);
    }

    private void createSchedule(LocalDate monday) {
        List<Employee> employees = employeeRepo.findAll();

        for (Employee employee : employees) {
            LocalDate day = monday;

            for (int i = 0; i < 7; i++) {
                LocalTime beginning = getRandomWorkHour();
                LocalTime end = beginning.plusHours(8).plusMinutes(30);

                Schedule schedule = new Schedule();
                schedule.setEmployee(employee);
                schedule.setBeginning(beginning);
                schedule.setEnd(end);
                schedule.setDate(day);

                scheduleRepo.save(schedule);
                
                day = day.plusDays(1);
            }
        }
    }

    
    private static LocalTime getRandomWorkHour() {
        //beginning workhours between 7am and 11am
        int randomHour = ThreadLocalRandom.current().nextInt(7, 11);
        return LocalTime.of(randomHour, 0);
    }

    private void createAppointments(LocalDate monday, LocalDate sunday) {
        List<Schedule> schedules = scheduleRepo.findByDateBetween(monday, sunday);

        for (Schedule schedule : schedules) {
            Employee employee = schedule.getEmployee();

            ApptmType type = apptmTypeRepo.findByRole(employee.getRole())
                    .orElse(null);

            if (type == null) {
                continue; 
            }

            LocalTime appointmentHour = schedule.getBeginning();

            while (appointmentHour.isBefore(schedule.getEnd())) {
                Appointment appointment = new Appointment();
                appointment.setAvailable(true);
                
                OffsetDateTime dateTime = appointmentService.formatDateTime(schedule.getDate(), appointmentHour);
                appointment.setDateTime(dateTime);
                appointment.setEmployee(employee);
                appointment.setApptmType(type);

                appointmentRepo.save(appointment);

                appointmentHour = appointmentHour.plusMinutes(30);
            }
        }
    }
}