package com.kidsability.automation.controller;

import com.kidsability.automation.customexceptions.SessionTokenExpiredException;
import com.kidsability.automation.model.ProgramTemplate;
import com.kidsability.automation.record.ProgramTemplateRecord;
import com.kidsability.automation.service.ProgramService;
import com.kidsability.automation.service.SessionManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProgramController {
    private SessionManagementService sessionManagementService;
    private ProgramService programService;
    public ProgramController(SessionManagementService sessionManagementService, ProgramService programService) {
        this.sessionManagementService = sessionManagementService;
        this.programService = programService;
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
}
