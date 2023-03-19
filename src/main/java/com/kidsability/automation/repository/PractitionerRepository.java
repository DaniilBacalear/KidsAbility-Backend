package com.kidsability.automation.repository;

import com.kidsability.automation.model.Practitioner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {
    public Practitioner findByEmail(String Email);
    public Practitioner findBySessionToken(String sessionToken);
    

}
