package com.kidsability.automation.data;

import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.ColdProbeSheet;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.model.Program;
import com.kidsability.automation.repository.ClientRepository;
import com.kidsability.automation.repository.ColdProbeSheetRepository;
import com.kidsability.automation.repository.PractitionerRepository;
import com.kidsability.automation.service.ClientService;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    private PractitionerRepository practitionerRepository;
    private ClientRepository clientRepository;
    private ClientService clientService;
    private ColdProbeSheetRepository coldProbeSheetRepository;
    public DataLoader(PractitionerRepository practitionerRepository,
                      ClientRepository clientRepository,
                      ClientService clientService, ColdProbeSheetRepository coldProbeSheetRepository) {
        this.practitionerRepository = practitionerRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.coldProbeSheetRepository = coldProbeSheetRepository;
    }
    public void run(ApplicationArguments args) {
        populatePractitioners();
        populateClients();
        populatePrograms();
        var p = practitionerRepository.findByEmail("dbacalea@uwaterloo.ca");
        var clients = p.getClients();
        for(var c : clients) {
            var programs = c.getPrograms();
            for(var program : programs) {
                System.out.println(program.toString());
            }
        }
    }

    private void populatePractitioners() {
        try {
            var encoder = new BCryptPasswordEncoder();
            Practitioner practitioner = Practitioner.builder()
                    .password(encoder.encode("1234"))
                    .email("dbacalea@uwaterloo.ca")
                    .isAdmin(true)
                    .firstName("Daniil")
                    .lastName("Bacalear")
                    .build();
            practitionerRepository.save(practitioner);
        }
        catch (Exception e) {

        }
    }

    private void populateClients() {
        try {
            clientService.createClient("c1");
            var c1 = clientService.getClient("c1");
            var practitioner = practitionerRepository.findByEmail("dbacalea@uwaterloo.ca");
            practitioner.addClient(c1);
            practitionerRepository.save(practitioner);
        }
        catch (Exception e) {

        }
    }

    private void populatePrograms() {
        var c1 = clientService.getClient("c1");
        var coldProbeSheet = ColdProbeSheet.builder()
                .criteria("test")
                .build();
        var program = Program.builder()
                .taskName("test program")
                .coldProbeSheet(coldProbeSheet)
                .build();

        c1.addProgram(program);
        clientRepository.save(c1);
    }


}
