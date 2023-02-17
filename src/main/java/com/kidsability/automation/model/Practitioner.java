package com.kidsability.automation.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Entity(name = "Practitioner")
@Table(
        name = "practitioner",
        uniqueConstraints = {
                @UniqueConstraint(name = "practitioner_email_unique", columnNames = "email")
        }
)
@Component
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Practitioner {
    @Id
    @SequenceGenerator(
            name = "practitioner_sequence",
            sequenceName = "practitioner_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "practitioner_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;
    @Column(
            name = "is_admin",
            nullable = false
    )
    private Boolean isAdmin;
    @Column(
            name = "email",
            updatable = false,
            nullable = false
    )
    private String email;
    @Column(name = "session_token")
    private String sessionToken;
    @Column(name = "last_active")
    private Instant lastActive;
    @Column(
            name = "password",
            nullable = false
    )
    private String password;
    @Column(name = "temp_password")
    private String tempPassword;
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

}
