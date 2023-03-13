package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Entity(name = "Program")
@Table(name = "program")
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Program {
    @Id
    @SequenceGenerator(
            name = "program_sequence",
            sequenceName = "program_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "program_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private LocalDate startDate;
    @Column
    private LocalDate acquisitionDate;
    @OneToOne(cascade = CascadeType.ALL)
    private ColdProbeSheet coldProbeSheet;
    @OneToOne(cascade = CascadeType.PERSIST)
    private ProgramTemplate programTemplate;
    @OneToMany
    private Set<ClientProgramSession> clientProgramSessions;

}
