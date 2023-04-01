package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity(name = "Program")
@Table(name = "program")
@Component
@Builder
@AllArgsConstructor
@Getter
@Setter
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
    private String name;
    @Column
    private String sharePointId;
    @Column
    private LocalDate startDate;
    @Column
    private LocalDate acquisitionDate;
    @Column
    private Boolean isMastered;
    @OneToOne
    private ColdProbeSheet coldProbeSheet;

    @OneToOne
    private MassTrialSheet massTrialSheet;
    @OneToOne
    private ProgramTemplate programTemplate;

    @ManyToOne
    private Client client;
    @OneToMany
    @JoinColumn(name = "program_id")
    private List<ClientProgramSession> clientProgramSessions;

    // CHANGE THESE TO INCLUDE MASS TRIAL SHEET OPTION!!!!!!!!
    @Override
    public int hashCode() {
        return Objects.hashCode(this.getClient() + this.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null) return false;
        return this.hashCode() == obj.hashCode();
    }

}
