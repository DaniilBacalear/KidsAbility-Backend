package com.kidsability.automation.repository;

import com.kidsability.automation.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    public Client findByKidsabilityId(String kidsabilityId);
}
