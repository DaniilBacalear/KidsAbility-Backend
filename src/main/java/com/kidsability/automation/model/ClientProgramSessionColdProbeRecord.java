package com.kidsability.automation.model;

import jakarta.persistence.*;

// contains k, v (target, met)
@Entity(name = "ClientProgramSessionColdProbeRecord")
@Table(name = "client_program_session_cold_probe_record")
public class ClientProgramSessionColdProbeRecord {
    @Id
    @SequenceGenerator(
            name = "client_program_session_cold_probe_record_sequence",
            sequenceName = "client_program_session_cold_probe_record_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_program_session_cold_probe_record_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column
    private String target;
    @Column
    private Boolean isMet;

}
