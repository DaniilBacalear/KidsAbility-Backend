package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
@Getter
@Setter
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
    @Column
    private String sharePointId;

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        return obj instanceof ProgramTemplate
                && ((ProgramTemplate) obj).getName().equals(this.getName());
    }

}
