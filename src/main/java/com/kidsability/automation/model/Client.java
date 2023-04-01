package com.kidsability.automation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Client")
@Table(
        name = "client",
        uniqueConstraints = {
                @UniqueConstraint(name = "client_kids_ability_id_unique", columnNames = "kids_ability_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Client {
    @Id
    @SequenceGenerator(
            name = "client_sequence",
            sequenceName = "client_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column(nullable = false, name = "kids_ability_id")
    private String kidsAbilityId;

    @Column
    private String sharePointRootId;

    @OneToMany
    @JsonBackReference
    @JoinColumn(name = "client_id")
    private Set<Program> programs;

    public void addProgram(Program program) {
        if(this.getPrograms() == null) {
            this.programs = new HashSet<>();
        }
        programs.add(program);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.kidsAbilityId);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        return obj instanceof Client
                && ((Client) obj).getKidsAbilityId().equals(this.getKidsAbilityId());
    }
}
