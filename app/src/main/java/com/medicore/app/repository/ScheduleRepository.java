package com.medicore.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer>{
	boolean existsByDateBetween(LocalDate start, LocalDate end);
	List<Schedule> findByDateBetween(LocalDate start, LocalDate end);
	List<Schedule> findByEmployeeDocumentNumber(String documentNumber);
}
