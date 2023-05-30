package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Entity(name = "MassTrialSheetItemEntry")
@Table(name = "mass_trial_sheet_item_entry")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class MassTrialSheetItemEntry {
    @Id
    @SequenceGenerator(
            name = "mass_trial_sheet_item_entry_sequence",
            sequenceName = "mass_trial_sheet_item_entry_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "mass_trial_sheet_item_entry_sequence"
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
    private String yNSeries;
    @Column
    private String practitionerInitials;

    // random review targets seperated by # delimiter
    @Column
    private String randomReviewTargetSeries;
    @Column
    private String rating;
    public Integer getYCount() {
        if(this.yNSeries == null || this.yNSeries.isBlank()) return 0;
        int yCount = 0;
        for(char curr : this.yNSeries.toCharArray()) {
            if(curr == 'Y') yCount ++;
        }
        return yCount;
    }

    public Integer getNCount() {
        if(this.yNSeries == null || this.yNSeries.isBlank()) return 0;
        int nCount = 0;
        for(char curr : this.yNSeries.toCharArray()) {
            if(curr == 'N') nCount ++;
        }
        return nCount;
    }
    public Double getScore() {
        int y = getYCount();
        int n = getNCount();
        int total = y + n;
        if(total == 0) return 0.0d;
        return (double) y / total * 100;
    }

    public void setSuccess() {
        this.success = getScore() >= 80d;
    }
}
