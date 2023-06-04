package com.kidsability.automation.model;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

@Entity(name = "BehaviourSessionItem")
@Table(name = "behaviour_session_item")
@Component
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class BehaviourSessionItem {
    @Id
    @SequenceGenerator(
            name = "behaviour_session_item_sequence",
            sequenceName = "behaviour_session_item_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "behaviour_session_item_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private String name;
    @Column
    private Integer frequency;
}
