package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    @JoinColumn(name = "cold_probe_sheet_id")
    private List<ColdProbeSheetItem> coldProbeSheetItems;

    public void replaceItem(ColdProbeSheetItem omission, ColdProbeSheetItem replacement) {
        var coldProbeSheetItems = this.getColdProbeSheetItems();
        Collections.sort(coldProbeSheetItems, (a, b) -> a.getRowNum() - b.getRowNum());
        for(int i = 0; i < coldProbeSheetItems.size(); i++) {
            var item = coldProbeSheetItems.get(i);
            if(item.getTargetName().equals(omission.getTargetName())) {
                item.setOmitted(true);
                replacement.setRowNum(i);
                for(int j = i; j < coldProbeSheetItems.size(); j++) {
                    item = coldProbeSheetItems.get(j);
                    item.setRowNum(j + 1);
                }
                coldProbeSheetItems.add(replacement);
                break;
            }
        }
    }

    public List<ColdProbeSheetItem> getSortedColdProbeSheetItems() {
        var coldProbeSheetItems = this.getColdProbeSheetItems();
        if(coldProbeSheetItems == null) return null;
        Collections.sort(coldProbeSheetItems, (a, b) -> a.getRowNum() - b.getRowNum());
        return coldProbeSheetItems;
    }

}
