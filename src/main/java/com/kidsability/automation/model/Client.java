package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "Client")
@Table(
        name = "client",
        uniqueConstraints = {
                @UniqueConstraint(name = "client_kidsability_id_unique", columnNames = "kidsability_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
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
    @Column(
            name = "kidsability_id",
            nullable = false
    )
    private String kidsabilityId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Program> programs;

    public void addProgram(Program program) {
        if(programs == null) programs = new HashSet<>();
        programs.add(program);
    }
}
