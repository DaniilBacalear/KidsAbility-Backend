package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Entity(name = "Program")
@Table(name = "program")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Program {
    @Id
    @SequenceGenerator(
            name = "program_sequence",
            sequenceName = "program_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "program_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private String skillArea;
    @Column
    private String task;
    @Column
    private String taskName;
    @Column
    private String taskObjective;
    @Column
    private String prerequisiteSkills;
    @Column
    private String programMasteryCriteria;
    @Column
    private String targetMasteryCriteria;
    @Column
    private String revisionCriteria;
    @Column
    private String setUp;
    @Column
    private String materials;
    @Column
    private String verbalSd;
    // might need to change this to a list and create a target entity
    @Column
    private String targets;
    @Column
    private String teachingProcedure;
    @Column
    private String generalizations;
    @Column
    private String promptingProcedure;
    @Column
    private String reinforcementSchedule;
    @Column
    private String graphing;

    @Column
    private LocalDate startDate;
    @Column
    private LocalDate acquisitionDate;
    @Column
    private String itInitials;
    @Column
    private String bcba;
    @OneToOne(cascade = CascadeType.ALL)
    private ColdProbeSheet coldProbeSheet;

    @OneToMany
    private Set<ClientProgramSession> clientProgramSessions;

}
