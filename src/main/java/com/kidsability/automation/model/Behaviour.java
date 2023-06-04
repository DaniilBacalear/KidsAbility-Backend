package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Behaviour")
@Table(name = "behaviour")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Behaviour {
    @Id
    @SequenceGenerator(
            name = "behaviour_sequence",
            sequenceName = "behaviour_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "behaviour_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private String sharePointId;
    @OneToMany
    @JoinColumn(name = "behaviour_id")
    private List<BehaviourSession> behaviourSessions;
    @OneToMany
    @JoinColumn(name = "behaviour_id")
    private List<BehaviourItem> behaviourItems;

    public void addBehaviourSession(BehaviourSession behaviourSession) {
        if(this.getBehaviourSessions() == null) {
            this.behaviourSessions = new ArrayList<>();
        }
        this.behaviourSessions.add(behaviourSession);
    }

    public void addBehaviourItem(BehaviourItem behaviourItem) {
        if(this.getBehaviourItems() == null) {
            this.behaviourItems = new ArrayList<>();
        }
        this.behaviourItems.add(behaviourItem);
    }

}
