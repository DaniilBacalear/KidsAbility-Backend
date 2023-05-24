package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.BadRequestException;
import com.kidsability.automation.customexceptions.ResourceDoesNotExistException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.model.Program;
import com.kidsability.automation.record.ClientRecord;
import com.kidsability.automation.record.PractitionerRecord;
import com.kidsability.automation.record.ProgramHistoryRecord;
import com.kidsability.automation.record.ProgramRecord;
import com.kidsability.automation.service.ClientService;
import com.kidsability.automation.service.PractitionerService;
import com.kidsability.automation.service.ProgramService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class PractitionerController {
    private SessionManagementService sessionManagementService;
    private PractitionerService practitionerService;
    private ClientService clientService;
    private ProgramService programService;
    public PractitionerController(SessionManagementService sessionManagementService,
                                  PractitionerService practitionerService,
                                  ClientService clientService,
                                  ProgramService programService) {
        this.sessionManagementService = sessionManagementService;
        this.practitionerService = practitionerService;
        this.clientService = clientService;
        this.programService = programService;
    }

    @GetMapping("/practitioner")
    public PractitionerRecord getPractitioner(@RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        return PractitionerRecord.builder()
                .firstName(practitioner.getFirstName())
                .lastName(practitioner.getLastName())
                .isAdmin(practitioner.getIsAdmin())
                .email(practitioner.getEmail())
                .build();
    }
    @GetMapping("/practitioner/client")
    public List<ClientRecord> getClients(@RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        return practitioner.getClients()
                .stream()
                .map(client -> new ClientRecord(client.getKidsAbilityId()))
                .collect(Collectors.toList());
    }
    @GetMapping("/practitioner/client/{kidsAbilityId}/program")
    public List<ProgramRecord> getPrograms(@RequestHeader("sessionToken") String sessionToken, @PathVariable String kidsAbilityId) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        Client client = practitioner.getClients()
                .stream()
                .filter(c -> c.getKidsAbilityId().equals(kidsAbilityId))
                .findFirst()
                .orElse(null);

        if(client == null) return null;

        List<Program> programs = new ArrayList<>(client.getPrograms());

        List<String> embeddableProgramTemplateLinks = new ArrayList<>();
        try {
            embeddableProgramTemplateLinks = programService.getEmbeddableProgramTemplateLinksFromPrograms(programs);
        }
        catch (Exception e) {
            // Handle this later
            return programs.stream()
                    .map(p -> ProgramRecord.builder()
                            .name(p.getName())
                            .startDate(p.getStartDate())
                            .id(p.getId())
                            .isMastered(p.getIsMastered())
                            .build()
                    )
                    .collect(Collectors.toList());
        }
        List<ProgramRecord> programRecords = new ArrayList<>();
        for(int i = 0; i < programs.size(); i++) {
            Program program = programs.get(i);
            String embeddableProgramTemplateLink = embeddableProgramTemplateLinks.get(i);
            ProgramRecord programRecord = ProgramRecord.builder()
                    .id(program.getId())
                    .name(program.getName())
                    .startDate(program.getStartDate())
                    .isMastered(program.getIsMastered())
                    .embeddableProgramTemplateLink(embeddableProgramTemplateLink)
                    .progress(program.getProgress())
                    .build();
            programRecords.add(programRecord);
        }
        return programRecords;
    }

    @GetMapping("/practitioner/client/{kidsAbilityId}/program/{id}")
    public ProgramRecord getProgram(@RequestHeader("sessionToken") String sessionToken, @PathVariable String kidsAbilityId, @PathVariable String id) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        Client client = practitioner.getClients()
                .stream()
                .filter(c -> c.getKidsAbilityId().equals(kidsAbilityId))
                .findFirst()
                .orElse(null);

        if(client == null) return null;
        Program program = client.getPrograms()
                .stream()
                .filter(p -> p.getId() == Long.parseLong(id))
                .findFirst()
                .orElse(null);
        String embeddableProgramTemplateLink = null;
        if(program == null) return null;
        try {
            embeddableProgramTemplateLink = programService.getEmbeddableProgramTemplate(program);
        }
        catch (Exception e) {
            // handle later
            embeddableProgramTemplateLink = null;
        }
        return ProgramRecord.builder()
                .name(program.getName())
                .startDate(program.getStartDate())
                .id(program.getId())
                .isMastered(program.getIsMastered())
                .embeddableProgramTemplateLink(embeddableProgramTemplateLink)
                .build();
    }

    @PostMapping("/practitioner/client/{kidsAbilityId}/program")
    public void createProgram(@RequestBody Program program, @PathVariable String kidsAbilityId, @RequestHeader("sessionToken") String sessionToken) throws Exception {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Client client = clientService.getClient(kidsAbilityId);
        if(client == null) throw new ResourceDoesNotExistException();
        if(!programService.isProgramValid(program)) throw new BadRequestException("Program format is invalid");
        programService.createProgram(program, client);

    }

    @PostMapping("/practitioner/client")
    public void addClientToPractitioner(@RequestHeader("sessionToken") String sessionToken, @RequestBody Client client) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        if(!clientService.clientExists(client.getKidsAbilityId())) throw new ResourceDoesNotExistException();
        Client toAdd = clientService.getClient(client.getKidsAbilityId());
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        practitioner.addClient(toAdd);
        practitionerService.savePractitioner(practitioner);
    }


}
