package com.kidsability.automation.service;

import com.kidsability.automation.model.*;
import com.kidsability.automation.repository.*;
import com.kidsability.automation.util.DateUtil;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ItemPreviewInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;
    private final ProgramTemplateRepository programTemplateRepository;
    private final SharePointService sharePointService;
    private final ClientRepository clientRepository;
    private final ColdProbeSheetRepository coldProbeSheetRepository;
    private final ColdProbeSheetItemRepository coldProbeSheetItemRepository;
    private final ExcelService excelService;
    public ProgramService(ProgramRepository programRepository, ProgramTemplateRepository programTemplateRepository,
                          SharePointService sharePointService, ClientRepository clientRepository,
                          ColdProbeSheetRepository coldProbeSheetRepository, ColdProbeSheetItemRepository coldProbeSheetItemRepository,
                          ExcelService excelService) {
        this.programRepository = programRepository;
        this.programTemplateRepository = programTemplateRepository;
        this.sharePointService = sharePointService;
        this.clientRepository = clientRepository;
        this.coldProbeSheetRepository = coldProbeSheetRepository;
        this.coldProbeSheetItemRepository = coldProbeSheetItemRepository;
        this.excelService = excelService;
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
        var set = new HashSet<ProgramTemplate>(alreadySavedProgramTemplates);
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

    @Transactional
    public void createProgram(Program program, Client client) throws Exception {
        var resourcePath = program.getMassTrialSheet() != null
                ? "General/Resources/mass trial sheet template.xlsx" : "General/Resources/cold probe data template.xlsx";

        var programTemplateToCopy = programTemplateRepository.findByName(program.getProgramTemplate().getName());

        // async calls
        var resourceDriveItemFuture = sharePointService.getDriveItemByPathFuture(resourcePath);
        var clientRootDriveItemFuture = sharePointService.getDriveItemByIdFuture(client.getSharePointRootId());
        var programTemplateToCopyDriveItemFuture = sharePointService.getDriveItemByIdFuture(programTemplateToCopy.getSharePointId());



        var clientProgramsRootDriveItem = sharePointService.getChildren(clientRootDriveItemFuture.get())
                .stream()
                .filter(driveItem -> driveItem.name.equals("Programs"))
                .findFirst()
                .orElse(null);
        var createdProgramFolderDriveItem = sharePointService.createSubFolder(clientProgramsRootDriveItem, program.getName())
                .get();
        program.setSharePointId(createdProgramFolderDriveItem.id);

        var copiedFileName = program.getMassTrialSheet() != null ? "mass trial sheet.xlsx" : "cold probe sheet.xlsx";

        //async calls to copy program template and cold sheet/mass trial sheet to new program folder
        var copySheetFuture = sharePointService.copyItemFuture(resourceDriveItemFuture.get(), createdProgramFolderDriveItem, copiedFileName);
        var copyProgramTemplateFuture = sharePointService.copyItemFuture(programTemplateToCopyDriveItemFuture.get(),
                createdProgramFolderDriveItem, programTemplateToCopy.getName());

        var copiedSheetDriveItem = copySheetFuture.get();
        var copiedProgramTemplateDriveItem = copyProgramTemplateFuture.get();
        sharePointService.awaitCopyCompletion(copiedSheetDriveItem);
        // set attributes of new program and save it to db
        program.setIsMastered(false);
        program.setProgramTemplate(programTemplateRepository.findByName(program.getProgramTemplate().getName()));
        program.setSharePointId(createdProgramFolderDriveItem.id);
        program.setClient(clientRepository.findByKidsAbilityId(client.getKidsAbilityId()));
        program.setStartDate(DateUtil.getToday());
        if(program.getColdProbeSheet() != null) {
            var coldProbeSheet = program.getColdProbeSheet();
            coldProbeSheet.setSharePointId(copiedSheetDriveItem.id);
            var coldProbeSheetItems = coldProbeSheet.getColdProbeSheetItems();
            for(int i = 0; i < coldProbeSheetItems.size(); i++) {
                var coldProbeSheetItem = coldProbeSheetItems.get(i);
                coldProbeSheetItem.setRowNum(i);
                coldProbeSheetItem.setOmitted(false);
            }
            coldProbeSheetItemRepository.saveAll(coldProbeSheetItems);
            coldProbeSheetRepository.save(coldProbeSheet);
        }
        else {
            // TO DO PERSIST MASS TRIAL SHEET ITEMS
        }
        programRepository.save(program);

        // associate client with saved program
        Client updatedClient = clientRepository.findByKidsAbilityId(client.getKidsAbilityId());
        updatedClient.addProgram(program);
        clientRepository.save(updatedClient);

        excelService.initColdProbeSheet(program);
    }

    public Boolean isProgramValid(Program program) throws Exception {
        if(program == null) return false;
        if(program.getName() == null || program.getName().isBlank()) return false;
        fetchLatestProgramTemplates();
        var programTemplate = program.getProgramTemplate();
        if(programTemplate == null
                || programTemplate.getName() == null
                || !programTemplateRepository.existsProgramTemplateByName(programTemplate.getName())) return false;
        // program must have either a cold probe sheet or a mass trial sheet but not both
        return program.getColdProbeSheet() != null ^ program.getMassTrialSheet() != null;

    }

}
