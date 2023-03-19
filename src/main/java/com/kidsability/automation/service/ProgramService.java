package com.kidsability.automation.service;

import com.kidsability.automation.model.Program;
import com.kidsability.automation.model.ProgramTemplate;
import com.kidsability.automation.repository.ProgramRepository;
import com.kidsability.automation.repository.ProgramTemplateRepository;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ItemPreviewInfo;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProgramService {
    private ProgramRepository programRepository;
    private ProgramTemplateRepository programTemplateRepository;
    private SharePointService sharePointService;
    public ProgramService(ProgramRepository programRepository, ProgramTemplateRepository programTemplateRepository, SharePointService sharePointService) {
        this.programRepository = programRepository;
        this.programTemplateRepository = programTemplateRepository;
        this.sharePointService = sharePointService;
    }

    public Program getProgram(Long id) {
        var program = programRepository.findById(id);
        return program.orElse(null);
    }

    public List<String> getEmbeddableProgramTemplateLinksFromPrograms(List<Program> programs) throws Exception {
        List<CompletableFuture<ItemPreviewInfo>> futures = new ArrayList<>();
        for(var program : programs) {
            var programTemplateDriveItem = sharePointService.getDriveItemById(program.getProgramTemplate().getSharePointId());
            futures.add(sharePointService.getEmbeddableLinkFuture(programTemplateDriveItem));
        }
        List<String> embeddableProgramTemplateLinks = new ArrayList<>();
        for(var future : futures) {
            var itemPreviewInfo = future.get();
            embeddableProgramTemplateLinks.add(itemPreviewInfo.getUrl);
        }
        return embeddableProgramTemplateLinks;
    }

    public List<String> getEmbeddableProgramTemplateLinks(List<ProgramTemplate> programTemplates) throws Exception {
        List<CompletableFuture<ItemPreviewInfo>> futures = new ArrayList<>();
        for(var programTemplate : programTemplates) {
            var programTemplateDriveItem = sharePointService.getDriveItemById(programTemplate.getSharePointId());
            futures.add(sharePointService.getEmbeddableLinkFuture(programTemplateDriveItem));
        }
        List<String> embeddableProgramTemplateLinks = new ArrayList<>();
        for(var future : futures) {
            var itemPreviewInfo = future.get();
            embeddableProgramTemplateLinks.add(itemPreviewInfo.getUrl);
        }
        return embeddableProgramTemplateLinks;
    }


    public String getEmbeddableProgramTemplate(Program program) throws Exception {
        var template = program.getProgramTemplate();
        var templateDriveItem = sharePointService.getDriveItemById(template.getSharePointId());
        return sharePointService.getEmbeddableLinkFuture(templateDriveItem).get().getUrl;
    }

    public void fetchLatestProgramTemplates() throws Exception {
        var alreadySavedProgramTemplates = programTemplateRepository.findAll();
        var set = new HashSet<ProgramTemplate>();
        for(var savedTemplate : alreadySavedProgramTemplates) {
            set.add(savedTemplate);
        }
        var programTemplateRootPath = "General/Program Templates";
        var programTemplateRootDriveItem = sharePointService.getDriveItemByPath(programTemplateRootPath);
        try {
            List<DriveItem> children = sharePointService.getChildren(programTemplateRootDriveItem);
            children.forEach(child -> {
                var programTemplate = ProgramTemplate.builder()
                        .name(child.name)
                        .sharePointId(child.id)
                        .build();
                if(!set.contains(programTemplate)) {
                    programTemplateRepository.save(programTemplate);
                    set.add(programTemplate);
                }
            });
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<ProgramTemplate> getProgramTemplates() {
        return programTemplateRepository.findAll();
    }

}
