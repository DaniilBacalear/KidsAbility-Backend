package com.kidsability.automation.repository;

import com.kidsability.automation.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query(
            value = "SELECT * FROM Client;",
            nativeQuery = true)
    public List<Client> findAllByQuery();
    public Client findByKidsAbilityId(String kidsAbilityId);
}
