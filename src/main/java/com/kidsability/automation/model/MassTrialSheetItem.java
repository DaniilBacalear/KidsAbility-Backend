package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Entity(name = "MassTrialSheetItem")
@Table(name = "mass_trial_sheet_item")
@Component
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class MassTrialSheetItem {

    @Id
    @SequenceGenerator(
            name = "mass_trial_sheet_item_sequence",
            sequenceName = "mass_trial_sheet_item_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "mass_trial_sheet_item_sequence"
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
    private Integer sheetNumber;
    @Column
    private Integer persistedSessions;
    @Column
    private String latestRating;

    @OneToMany
    @JoinColumn(name = "mass_trial_sheet_item_id")
    private List<MassTrialSheetItemEntry> massTrialSheetItemEntries;
}
