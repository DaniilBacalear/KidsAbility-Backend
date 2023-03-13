package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Entity(name = "ProgramTemplate")
@Table(
        name = "program_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "name_unique", columnNames = "name")
        }
)
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProgramTemplate {
    @Id
    @SequenceGenerator(
            name = "program_template_sequence",
            sequenceName = "program_template_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "program_template_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private String name;
    @Column
    private String link;

}
