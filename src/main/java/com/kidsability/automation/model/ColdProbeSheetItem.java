package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "ColdProbeSheetItem")
@Table(name = "cold_probe_sheet_item")
@Component
@Builder
@AllArgsConstructor
@Getter
@Setter
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
    private String targetName;
    @Column
    private Boolean omitted;
    @Column
    private Boolean isMastered;
    @Column
    private Integer rowNum;
    @OneToMany
    @JoinColumn(name = "cold_probe_sheet_item_id")
    private List<ColdProbeSheetItemEntry> coldProbeSheetItemEntries;

    public void addColdProbeSheetItemEntry(ColdProbeSheetItemEntry coldProbeSheetItemEntry) {
        if(coldProbeSheetItemEntries == null) {
            coldProbeSheetItemEntries = new ArrayList<>();
        }
        coldProbeSheetItemEntries.add(coldProbeSheetItemEntry);
    }
}
