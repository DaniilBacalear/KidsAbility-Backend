package com.kidsability.automation.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity(name = "ClientProgramSession")
@Table(name = "client_program_session")
public class ClientProgramSession {
    @Id
    @SequenceGenerator(
            name = "client_program_session_sequence",
            sequenceName = "client_program_session_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_program_session_sequence"
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
    @OneToMany(mappedBy = "clientProgramSession")
    private Set<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecords;

}
