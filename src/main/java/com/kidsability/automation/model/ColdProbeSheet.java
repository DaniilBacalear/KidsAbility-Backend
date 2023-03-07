package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Entity(name = "ColdProbeSheet")
@Table(name = "cold_probe_sheet")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ColdProbeSheet {
    @Id
    @SequenceGenerator(
            name = "cold_probe_sheet_sequence",
            sequenceName = "cold_probe_sheet_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cold_probe_sheet_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private String child;
    @Column
    private String code;
    @Column
    private String taskName;
    @Column
    private String objective;
    @Column
    private String example;
    @Column
    private String sd;
    @Column
    private String criterionToMastery;
    @Column
    private String criteria;
    @OneToMany
    private Set<ColdProbeSheetItem> coldProbeSheetItems;
}
