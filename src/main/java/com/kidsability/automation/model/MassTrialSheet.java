package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Entity(name = "MassTrialSheet")
@Table(name = "mass_trial_sheet")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class MassTrialSheet {
    @Id
    @SequenceGenerator(
            name = "mass_trial_sheet_sequence",
            sequenceName = "mass_trial_sheet_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "mass_trial_sheet_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private String child;
    @Column
    private String taskName;
    @Column
    private String sd;
    @Column
    private String targetMastery;
    @Column
    private String revisionCriteria;
    @Column
    private String promptLegend;
    @Column
    private String instructions;
}
