package com.kidsability.automation.repository;

import com.kidsability.automation.model.ProgramTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramTemplateRepository extends JpaRepository<ProgramTemplate, Long> {
    public ProgramTemplate findByName(String name);
    public Boolean existsProgramTemplateByName(String name);

}
