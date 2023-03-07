package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Entity(name = "ColdProbeSheetItemEntry")
@Table(name = "cold_probe_sheet_item_entry")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ColdProbeSheetItemEntry {
    @Id
    @SequenceGenerator(
            name = "cold_probe_sheet_item_entry_sequence",
            sequenceName = "cold_probe_sheet_item_entry_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "cold_probe_sheet_item_entry_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private Boolean success;
    @Column
    private LocalDate date;
    @Column
    private String instructorInitials;
    @ManyToOne
    @JoinColumn(name = "cold_probe_sheet_item_id")
    private ColdProbeSheetItem coldProbeSheetItem;

}
