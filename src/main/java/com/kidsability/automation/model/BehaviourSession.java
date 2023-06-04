package com.kidsability.automation.model;

import com.kidsability.automation.repository.BehaviourSessionItemRepository;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "BehaviourSession")
@Table(name = "behaviour_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviourSession {
    @Id
    @SequenceGenerator(
            name = "behaviour_session_sequence",
            sequenceName = "behaviour_session_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "behaviour_session_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private LocalDate date;
    @Column
    private Boolean isActive;
    @OneToMany
    @JoinColumn(name = "behaviour_session_id")
    private List<BehaviourSessionItem> behaviourSessionItems;

    public void addBehaviourSessionItem(BehaviourSessionItem behaviourSessionItem) {
        if(this.getBehaviourSessionItems() == null) {
            this.behaviourSessionItems = new ArrayList<>();
        }
        this.behaviourSessionItems.add(behaviourSessionItem);
    }
}
