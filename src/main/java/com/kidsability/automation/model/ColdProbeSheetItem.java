package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Entity(name = "ColdProbeSheetItem")
@Table(name = "cold_probe_sheet_item")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ColdProbeSheetItem {
    @Id
    @SequenceGenerator(
            name = "cold_probe_sheet_item_sequence",
            sequenceName = "cold_probe_sheet_item_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cold_probe_sheet_item_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private String name;
    @ManyToOne
    @JoinColumn(name = "cold_probe_sheet_id")
    private ColdProbeSheet coldProbeSheet;

    @OneToMany(mappedBy = "coldProbeSheetItem")
    private Set<ColdProbeSheetItemEntry> coldProbeSheetItemEntries;
}
