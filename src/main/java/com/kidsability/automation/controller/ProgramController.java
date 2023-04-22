package com.kidsability.automation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kidsability.automation.customexceptions.ResourceDoesNotExistException;
import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.ClientProgramSession;
import com.kidsability.automation.model.Practitioner;
import com.kidsability.automation.model.Program;
import com.kidsability.automation.model.ProgramTemplate;
import com.kidsability.automation.record.ClientProgramSessionRecord;
import com.kidsability.automation.record.ProgramHistoryRecord;
import com.kidsability.automation.record.ProgramTemplateRecord;
import com.kidsability.automation.service.PractitionerService;
import com.kidsability.automation.service.ProgramService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class ProgramController {
    private SessionManagementService sessionManagementService;
    private ProgramService programService;
    private PractitionerService practitionerService;
    public ProgramController(SessionManagementService sessionManagementService, ProgramService programService, PractitionerService practitionerService) {
        this.sessionManagementService = sessionManagementService;
        this.programService = programService;
        this.practitionerService = practitionerService;
    }

    @GetMapping("/program/template")
    public List<ProgramTemplateRecord> getProgramTemplates(@RequestHeader("sessionToken") String sessionToken) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        try {
            programService.fetchLatestProgramTemplates();
        }
        catch (Exception e) {
            // handle this later
        }
        List<ProgramTemplate> programTemplates = programService.getProgramTemplates();
        try {
            List<String> embeddableLinks = programService.getEmbeddableProgramTemplateLinks(programTemplates);
            List<ProgramTemplateRecord> programTemplateRecords = new ArrayList<>();
            for(int i = 0; i < programTemplates.size(); i++) {
                var programTemplateRecord = ProgramTemplateRecord.builder()
                        .name(programTemplates.get(i).getName())
                        .embeddableLink(embeddableLinks.get(i))
                        .build();
                programTemplateRecords.add(programTemplateRecord);
            }
            return programTemplateRecords;
        }
        catch (Exception e) {
            // handle later
            return programTemplates.stream()
                    .map(pt -> ProgramTemplateRecord.builder()
                            .name(pt.getName())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @GetMapping("/program/{programId}/history")
    public ProgramHistoryRecord getProgramHistory(@RequestHeader("sessionToken") String sessionToken, @PathVariable String programId) throws ExecutionException, InterruptedException {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Program program = programService.getProgram(Long.parseLong(programId));
        if(program == null) throw new ResourceDoesNotExistException();
        return new ProgramHistoryRecord(programService.getEmbeddableExcelSheet(program));
    }

    @GetMapping("/program/{programId}/session/active")
    public ClientProgramSessionRecord getActiveSession(@RequestHeader("sessionToken") String sessionToken, @PathVariable String programId) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Program program = programService.getProgram(Long.parseLong(programId));
        if(program == null) throw new ResourceDoesNotExistException();
        return programService.convertClientProgramSessionToRecord(program);
    }

    @PostMapping("/program/{programId}/session/active/persist")
    public void persistActiveSession(@RequestHeader("sessionToken") String sessionToken, @PathVariable String programId, @RequestBody Object body) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Program program = programService.getProgram(Long.parseLong(programId));
        Practitioner practitioner = practitionerService.getPractitioner(sessionToken);
        if(program == null) throw new ResourceDoesNotExistException();
        ClientProgramSession clientProgramSession = programService.getActiveClientProgramSession(program);
        if(clientProgramSession.getClientProgramSessionColdProbeRecords() != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            ClientProgramSession updatedClientProgramSession = mapper.convertValue(body, ClientProgramSession.class);
            programService.persistColdProbeProgramSession(practitioner, program, updatedClientProgramSession);
        }
        else {
            // handle mass-trial session save TODO
        }
    }

    @PostMapping("/program/{programId}/session/active/save")
    public void saveActiveSession(@RequestHeader("sessionToken") String sessionToken, @PathVariable String programId, @RequestBody Object body) {
        if(!sessionManagementService.isSessionActive(sessionToken)) throw new SessionTokenExpiredException();
        Program program = programService.getProgram(Long.parseLong(programId));
        if(program == null) throw new ResourceDoesNotExistException();
        ClientProgramSession clientProgramSession = programService.getActiveClientProgramSession(program);
        if(clientProgramSession.getClientProgramSessionColdProbeRecords() != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            ClientProgramSession updatedClientProgramSession = mapper.convertValue(body, ClientProgramSession.class);
            programService.saveColdProbeProgramSession(program, updatedClientProgramSession);
        }
        else {
            // handle mass-trial session save TODO
        }
    }
}
