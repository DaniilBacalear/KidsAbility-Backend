package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "ClientProgramSessionMassTrialRecord")
@Table(name = "client_program_session_mass_trial_record")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientProgramSessionMassTrialRecord {
    @Id
    @SequenceGenerator(
            name = "client_program_session_mass_trial_record_sequence",
            sequenceName = "client_program_session_mass_trial_record_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_program_session_mass_trial_record_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column
    private String target;
    @Column
    private Boolean isOmitted;
    @Column
    private Boolean isRecorded;
    @Column
    private Integer sequenceNumber;
    @Column
    private Boolean canOmit;
    @Column
    private Boolean isRandomReview;
    @Column
    private String yNSeries;
    @Column
    private String randomReviewTargetSeries;
    @Column
    private String rating;
    @Column
    private String prevRating;

}
