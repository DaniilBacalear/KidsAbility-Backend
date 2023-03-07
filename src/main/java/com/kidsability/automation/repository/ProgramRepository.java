package com.kidsability.automation.repository;

import com.kidsability.automation.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
}
