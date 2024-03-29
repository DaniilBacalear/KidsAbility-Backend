package com.kidsability.automation.service;

import com.kidsability.automation.model.*;
import com.kidsability.automation.record.ClientProgramSessionRecord;
import com.kidsability.automation.repository.*;
import com.kidsability.automation.util.DateUtil;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ItemPreviewInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;
    private final ProgramTemplateRepository programTemplateRepository;
    private final SharePointService sharePointService;
    private final ClientRepository clientRepository;
    private final ColdProbeSheetRepository coldProbeSheetRepository;
    private final ColdProbeSheetItemRepository coldProbeSheetItemRepository;
    private final ExcelService excelService;
    private final ClientProgramSessionColdProbeRecordRepository clientProgramSessionColdProbeRecordRepository;
    private final ClientProgramSessionRepository clientProgramSessionRepository;
    private final ColdProbeSheetItemEntryRepository coldProbeSheetItemEntryRepository;

    private final MassTrialSheetRepository massTrialSheetRepository;
    private final MassTrialSheetItemRepository massTrialSheetItemRepository;
    private final ClientProgramSessionMassTrialRecordRepository clientProgramSessionMassTrialRecordRepository;
    private final MassTrialSheetItemEntryRepository massTrialSheetItemEntryRepository;
    public ProgramService(ProgramRepository programRepository, ProgramTemplateRepository programTemplateRepository,
                          SharePointService sharePointService, ClientRepository clientRepository,
                          ColdProbeSheetRepository coldProbeSheetRepository, ColdProbeSheetItemRepository coldProbeSheetItemRepository,
                          ExcelService excelService, ClientProgramSessionColdProbeRecordRepository clientProgramSessionColdProbeRecordRepository,
                          ClientProgramSessionRepository clientProgramSessionRepository, ColdProbeSheetItemEntryRepository coldProbeSheetItemEntryRepository,
                          MassTrialSheetRepository massTrialSheetRepository, MassTrialSheetItemRepository massTrialSheetItemRepository,
                          ClientProgramSessionMassTrialRecordRepository clientProgramSessionMassTrialRecordRepository,
                          MassTrialSheetItemEntryRepository massTrialSheetItemEntryRepository) {
        this.programRepository = programRepository;
        this.programTemplateRepository = programTemplateRepository;
        this.sharePointService = sharePointService;
        this.clientRepository = clientRepository;
        this.coldProbeSheetRepository = coldProbeSheetRepository;
        this.coldProbeSheetItemRepository = coldProbeSheetItemRepository;
        this.excelService = excelService;
        this.clientProgramSessionColdProbeRecordRepository = clientProgramSessionColdProbeRecordRepository;
        this.clientProgramSessionRepository = clientProgramSessionRepository;
        this.coldProbeSheetItemEntryRepository = coldProbeSheetItemEntryRepository;
        this.massTrialSheetRepository = massTrialSheetRepository;
        this.massTrialSheetItemRepository = massTrialSheetItemRepository;
        this.clientProgramSessionMassTrialRecordRepository = clientProgramSessionMassTrialRecordRepository;
        this.massTrialSheetItemEntryRepository = massTrialSheetItemEntryRepository;
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

    public String getEmbeddableExcelSheet(Program program) throws ExecutionException, InterruptedException {
        if(program.getColdProbeSheet() != null) {
            var coldProbeSheet = program.getColdProbeSheet();
            var coldProbeSheetDriveItem = sharePointService.getDriveItemById(coldProbeSheet.getSharePointId());
            return sharePointService.getEmbeddableLinkFuture(coldProbeSheetDriveItem).get().getUrl;
        }
        else {
            // TODO handle mass trial sheet
            var massTrialSheet = program.getMassTrialSheet();
            var massTrialSheetDriveItem = sharePointService.getDriveItemById(massTrialSheet.getSharePointId());
            return sharePointService.getEmbeddableLinkFuture(massTrialSheetDriveItem)
                    .get()
                    .getUrl;
        }
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
                ? "General/Resources DO NOT TOUCH!/mass trial sheet template.xlsx" : "General/Resources DO NOT TOUCH!/cold probe data template.xlsx";

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
        program.setProgress(0.0d);
        if(program.getColdProbeSheet() != null) {
            var coldProbeSheet = program.getColdProbeSheet();
            coldProbeSheet.setSharePointId(copiedSheetDriveItem.id);
            var coldProbeSheetItems = coldProbeSheet.getColdProbeSheetItems();
            for(int i = 0; i < coldProbeSheetItems.size(); i++) {
                var coldProbeSheetItem = coldProbeSheetItems.get(i);
                coldProbeSheetItem.setRowNum(i);
                coldProbeSheetItem.setOmitted(false);
                coldProbeSheetItem.setIsMastered(false);
            }
            coldProbeSheetItemRepository.saveAll(coldProbeSheetItems);
            coldProbeSheet.setPersistedSessions(0);
            coldProbeSheetRepository.save(coldProbeSheet);
        }
        else {
            // TODO PERSIST MASS TRIAL SHEET ITEMS
            var massTrialSheet = program.getMassTrialSheet();
            var massTrialSheetItems = massTrialSheet.getMassTrialSheetItems();
            var randomReviewItem = MassTrialSheetItem
                    .builder()
                    .omitted(false)
                    .isMastered(false)
                    .persistedSessions(0)
                    .targetName("Random Review")
                    .latestRating("None")
                    .build();
            massTrialSheetItems.add(0, randomReviewItem);
            for(int i = 0; i < massTrialSheetItems.size(); i++) {
                var massTrialSheetItem = massTrialSheetItems.get(i);
                massTrialSheetItem.setSheetNumber(i + 1);
                massTrialSheetItem.setOmitted(false);
                massTrialSheetItem.setIsMastered(false);
                massTrialSheetItem.setPersistedSessions(0);
                massTrialSheetItem.setLatestRating("None");
            }
            massTrialSheetItemRepository.saveAll(massTrialSheetItems);
            massTrialSheet.setSharePointId(copiedSheetDriveItem.id);
            massTrialSheetRepository.save(massTrialSheet);
        }
        programRepository.save(program);

        // associate client with saved program
        Client updatedClient = clientRepository.findByKidsAbilityId(client.getKidsAbilityId());
        updatedClient.addProgram(program);
        clientRepository.save(updatedClient);
        if(program.getColdProbeSheet() != null) {
            excelService.initColdProbeSheet(program);
        }
        else {
            excelService.initMassTrialSheet(program);
        }

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

    public ClientProgramSession getActiveClientProgramSession(Program program) {
        if(program.getClientProgramSessions() == null) {
            return createNewActiveClientProgramSession(program);
        }
        else {
           ClientProgramSession activeSession =  program.getClientProgramSessions()
                    .stream()
                    .filter(clientProgramSession -> clientProgramSession.getIsActive())
                    .findAny()
                    .orElse(null);
           if(activeSession == null) return createNewActiveClientProgramSession(program);
           else return activeSession;
        }
    }

    public ClientProgramSession createNewActiveClientProgramSession(Program program) {
        if(program.getColdProbeSheet() != null) {
            List<ClientProgramSessionColdProbeRecord> targets = program
                    .getColdProbeSheet()
                    .getColdProbeSheetItems()
                    .stream()
                    .filter(target -> !target.getOmitted() && !target.getIsMastered())
                    .sorted((a, b) -> a.getRowNum() - b.getRowNum())
                    .map(target -> {
                        var record = ClientProgramSessionColdProbeRecord
                                .builder()
                                .target(target.getTargetName())
                                .isMet(false)
                                .isRecorded(false)
                                .isOmitted(false)
                                .isInMaintenance(isColdProbeTargetInMaintenance(target.getTargetName(), program))
                                .canOmit(true)
                                .build();
                        return record;
                    }
                    )
                    .toList();

            for(int i = 0; i < targets.size(); i++) {
                var target = targets.get(i);
                target.setSequenceNumber(i);
                clientProgramSessionColdProbeRecordRepository.save(target);
            }

            ClientProgramSession clientProgramSession = ClientProgramSession
                    .builder()
                    .isActive(true)
                    .date(DateUtil.getToday())
                    .clientProgramSessionColdProbeRecords(targets)
                    .build();
            clientProgramSessionRepository.save(clientProgramSession);

            program.addClientProgramSession(clientProgramSession);
            programRepository.save(program);
            return clientProgramSession;
        }
        else {
            // TODO handle mass trial
            List<ClientProgramSessionMassTrialRecord> targets = program
                    .getMassTrialSheet()
                    .getMassTrialSheetItems()
                    .stream()
                    .filter(massTrialSheetItem -> !massTrialSheetItem.getOmitted() && !massTrialSheetItem.getIsMastered())
                    .map(massTrialSheetItem -> {
                        var record = ClientProgramSessionMassTrialRecord
                                .builder()
                                .isRecorded(false)
                                .isOmitted(false)
                                .prevRating(massTrialSheetItem.getLatestRating())
                                .rating("")
                                .isRandomReview(massTrialSheetItem.getTargetName().equals("Random Review"))
                                .target(massTrialSheetItem.getTargetName())
                                .yNSeries("")
                                .randomReviewTargetSeries("")
                                .canOmit(true)
                                .sequenceNumber(massTrialSheetItem.getSheetNumber())
                                .build();
                        return record;
                    })
                    .toList();

            clientProgramSessionMassTrialRecordRepository.saveAll(targets);

            ClientProgramSession clientProgramSession = ClientProgramSession
                    .builder()
                    .isActive(true)
                    .date(DateUtil.getToday())
                    .clientProgramSessionMassTrialRecords(targets)
                    .build();
            clientProgramSessionRepository.save(clientProgramSession);

            program.addClientProgramSession(clientProgramSession);
            programRepository.save(program);

            return clientProgramSession;
        }
    }

    public Boolean isColdProbeTargetInMaintenance(String target, Program program) {
        List<ColdProbeSheetItemEntry> entries = program
                .getColdProbeSheet()
                .getColdProbeSheetItems()
                .stream()
                .filter(item -> item.getTargetName().equalsIgnoreCase(target))
                .findFirst()
                .map(item -> item.getColdProbeSheetItemEntries())
                .orElse(null);
        int recentSuccesses = 3;
        if(entries == null || entries.size() < recentSuccesses) return false;
        entries.sort((a, b) -> {
            if (b.getId() > a.getId()) return 1;
            return -1;
        });
        for(int i = 0; i < recentSuccesses; i++) {
            if(!entries.get(i).getSuccess()) return false;
        }
        return true;
    }

    public ClientProgramSessionRecord convertClientProgramSessionToRecord(Program program) {
        ClientProgramSession clientProgramSession = getActiveClientProgramSession(program);
        if(program.getColdProbeSheet() != null) {
            ColdProbeSheet coldProbeSheet = program.getColdProbeSheet();
            List<ColdProbeSheetItem> coldProbeSheetItemsSorted = coldProbeSheet.getSortedColdProbeSheetItems();

            List<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecords = clientProgramSession
                    .getClientProgramSessionColdProbeRecords()
                    .stream()
                    .sorted((a, b) -> a.getSequenceNumber() - b.getSequenceNumber())
                    .toList();

            return ClientProgramSessionRecord
                    .builder()
                    .isActive(clientProgramSession.getIsActive())
                    .date(clientProgramSession.getDate())
                    .id(clientProgramSession.getId())
                    .clientProgramSessionColdProbeRecords(clientProgramSessionColdProbeRecords)
                    .build();

        }
        else {
            // TODO handle mass trial sheet
            MassTrialSheet massTrialSheet = program.getMassTrialSheet();
            List<ClientProgramSessionMassTrialRecord> clientProgramSessionMassTrialRecords = clientProgramSession
                    .getClientProgramSessionMassTrialRecords();
            return ClientProgramSessionRecord
                    .builder()
                    .isActive(clientProgramSession.getIsActive())
                    .date(clientProgramSession.getDate())
                    .id(clientProgramSession.getId())
                    .massTrialMasteredTargets(getMasteredTargetNames(massTrialSheet))
                    .clientProgramSessionMassTrialRecords(clientProgramSessionMassTrialRecords)
                    .build();
        }
    }

    public void persistColdProbeProgramSession(Practitioner practitioner, Program program, ClientProgramSession updates) throws Exception {
         saveColdProbeProgramSession(program, updates);
         Map<String, ColdProbeSheetItem> targetNameToColdProbeSheetItem  = new HashMap<>();

         ColdProbeSheet coldProbeSheet = program.getColdProbeSheet();

         List<ColdProbeSheetItem> coldProbeSheetItems = coldProbeSheet
                .getSortedColdProbeSheetItems();
         coldProbeSheetItems
                .forEach(coldProbeSheetItem -> targetNameToColdProbeSheetItem.put(coldProbeSheetItem.getTargetName(), coldProbeSheetItem));

         ClientProgramSession activeClientProgramSession = getActiveClientProgramSession(program);
         List<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecordsToSave = activeClientProgramSession
                .getClientProgramSessionColdProbeRecords();
        // convert ClientProgramSessionColdProbeRecords to ColdProbeSheetItemEntries
         for(int i = 0; i < clientProgramSessionColdProbeRecordsToSave.size(); i++) {
             ClientProgramSessionColdProbeRecord curr = clientProgramSessionColdProbeRecordsToSave.get(i);
             if(curr.getIsOmitted()) {
                 ColdProbeSheetItem omission = targetNameToColdProbeSheetItem.get(curr.getTarget());
                 ClientProgramSessionColdProbeRecord next = clientProgramSessionColdProbeRecordsToSave.get(i + 1);
                 ColdProbeSheetItem replacement = ColdProbeSheetItem
                         .builder()
                         .omitted(next.getIsOmitted())
                         .targetName(next.getTarget())
                         .isMastered(false)
                         .build();
                 if(next.getIsRecorded()) {
                     ColdProbeSheetItemEntry replacementItemEntry = ColdProbeSheetItemEntry
                             .builder()
                             .success(next.getIsMet())
                             .practitionerInitials(practitioner.getInitials())
                             .date(DateUtil.getToday())
                             .build();
                     coldProbeSheetItemEntryRepository.save(replacementItemEntry);
                     replacement.addColdProbeSheetItemEntry(replacementItemEntry);
                 }
                 coldProbeSheetItemRepository.save(omission);
                 coldProbeSheet.replaceItem(omission, replacement);
                 coldProbeSheetItemRepository.save(replacement);
                 i ++;
             }
             else if(curr.getIsRecorded()){
                 ColdProbeSheetItem coldProbeSheetItem = targetNameToColdProbeSheetItem.get(curr.getTarget());
                 if(isColdProbeTargetInMaintenance(curr.getTarget(), program) && curr.getIsMet()) coldProbeSheetItem.setIsMastered(true);
                 ColdProbeSheetItemEntry coldProbeSheetItemEntry = ColdProbeSheetItemEntry
                         .builder()
                         .success(curr.getIsMet())
                         .practitionerInitials(practitioner.getInitials())
                         .date(DateUtil.getToday())
                         .build();
                 coldProbeSheetItemEntryRepository.save(coldProbeSheetItemEntry);
                 coldProbeSheetItem.addColdProbeSheetItemEntry(coldProbeSheetItemEntry);
                 coldProbeSheetItemRepository.save(coldProbeSheetItem);
             }
         }

         coldProbeSheet.setPersistedSessions(coldProbeSheet.getPersistedSessions() + 1);
         coldProbeSheetRepository.save(coldProbeSheet);
         double progression = getColdProbeProgression(coldProbeSheet);
         program.setProgress(progression);
         programRepository.save(program);
         activeClientProgramSession.setClientProgramSessionColdProbeRecords(clientProgramSessionColdProbeRecordsToSave);
         activeClientProgramSession.setIsActive(false);
         activeClientProgramSession.setDate(DateUtil.getToday());
         clientProgramSessionRepository.save(activeClientProgramSession);
         excelService.addColdProbeSession(coldProbeSheet, activeClientProgramSession, practitioner);
    }

    public void persistMassTrialProgramSession(Practitioner practitioner, Program program, ClientProgramSession updates) throws ExecutionException, InterruptedException {
        saveMassTrialProgramSession(program, updates);

        Map<String, MassTrialSheetItem> targetNameToMassTrialSheetItem = new HashMap<>();
        MassTrialSheet massTrialSheet = program.getMassTrialSheet();

        var excelDriveItem = sharePointService.getDriveItemById(massTrialSheet.getSharePointId());
        var session = sharePointService.getExcelSessionId(excelDriveItem);

        List<MassTrialSheetItem> existingItems = massTrialSheet.getMassTrialSheetItems();

        List<ClientProgramSessionMassTrialRecord> updatedRecords = updates.getClientProgramSessionMassTrialRecords();

        for(var existingItem : existingItems) {
            targetNameToMassTrialSheetItem.put(existingItem.getTargetName(), existingItem);
        }
        for(var updatedRecord : updatedRecords) {
            if(!targetNameToMassTrialSheetItem.containsKey(updatedRecord.getTarget())) {
                MassTrialSheetItem newItem = MassTrialSheetItem
                        .builder()
                        .targetName(updatedRecord.getTarget())
                        .sheetNumber(targetNameToMassTrialSheetItem.size() + 1)
                        .omitted(updatedRecord.getIsOmitted())
                        .isMastered(false)
                        .persistedSessions(0)
                        .build();
                massTrialSheetItemRepository.save(newItem);
                massTrialSheet.addMassTrialSheetItem(newItem);
                targetNameToMassTrialSheetItem.put(newItem.getTargetName(), newItem);
                excelService.addMassTrialItem(massTrialSheet, newItem, excelDriveItem, session);
            }
            if(updatedRecord.getIsOmitted()) {
                MassTrialSheetItem item = targetNameToMassTrialSheetItem.get(updatedRecord.getTarget());
                item.setOmitted(true);
                massTrialSheetItemRepository.save(item);
            }
            if(updatedRecord.getIsRecorded()) {
                MassTrialSheetItem item = targetNameToMassTrialSheetItem.get(updatedRecord.getTarget());
                MassTrialSheetItemEntry massTrialSheetItemEntry = MassTrialSheetItemEntry
                        .builder()
                        .date(DateUtil.getToday())
                        .rating(updatedRecord.getRating())
                        .randomReviewTargetSeries(updatedRecord.getRandomReviewTargetSeries())
                        .yNSeries(updatedRecord.getYNSeries())
                        .practitionerInitials(practitioner.getInitials())
                        .build();
                massTrialSheetItemEntry.setSuccess();
                massTrialSheetItemEntryRepository.save(massTrialSheetItemEntry);
                item.addMassTrialSheetItemEntry(massTrialSheetItemEntry);
                excelService.recordMassTrialItemEntry(practitioner, massTrialSheet, item, massTrialSheetItemEntry, excelDriveItem, session);
                item.setPersistedSessions(item.getPersistedSessions() + 1);
                item.setLatestRating(updatedRecord.getRating());
                if(massTrialSheetItemEntry.getSuccess() && massTrialSheetItemEntry.getRating().equalsIgnoreCase("NP3")) {
                    item.setIsMastered(true);
                }
                massTrialSheetItemRepository.save(item);
            }
        }
        if(getMassTrialProgression(massTrialSheet) == 100) program.setIsMastered(true);
        program.setProgress(getMassTrialProgression(massTrialSheet));
        programRepository.save(program);
        ClientProgramSession activeClientProgramSession = getActiveClientProgramSession(program);
        activeClientProgramSession.setClientProgramSessionMassTrialRecords(updates.getClientProgramSessionMassTrialRecords());
        activeClientProgramSession.setDate(DateUtil.getToday());
        activeClientProgramSession.setIsActive(false);
        clientProgramSessionRepository.save(activeClientProgramSession);
    }

    public double getColdProbeProgression(ColdProbeSheet coldProbeSheet) {
        long masteredTargetCount = coldProbeSheet
                .getColdProbeSheetItems()
                .stream()
                .filter(ColdProbeSheetItem::getIsMastered)
                .count();
        long activeTargets = coldProbeSheet
                .getColdProbeSheetItems()
                .stream()
                .filter(coldProbeSheetItem -> !coldProbeSheetItem.getOmitted())
                .count();
        return (double) masteredTargetCount / activeTargets * 100d;
    }

    public double getMassTrialProgression(MassTrialSheet massTrialSheet) {
        List<MassTrialSheetItem> massTrialSheetItems = massTrialSheet.getMassTrialSheetItems();
        long mastered = massTrialSheetItems
                .stream()
                .filter(MassTrialSheetItem::getIsMastered)
                .count();
        long total = massTrialSheetItems
                .stream()
                .filter(massTrialSheetItem -> !massTrialSheetItem.getOmitted())
                .count();
        if(total == 0) return 0d;
        return (double) mastered / total * 100;
    }

    public List<String> getMasteredTargetNames(MassTrialSheet massTrialSheet) {
        List<MassTrialSheetItem> massTrialSheetItems = massTrialSheet.getMassTrialSheetItems();
        return massTrialSheetItems
                .stream()
                .filter(MassTrialSheetItem::getIsMastered)
                .map(MassTrialSheetItem::getTargetName)
                .toList();
    }

    public void saveColdProbeProgramSession(Program program, ClientProgramSession updates) {
        ClientProgramSession activeClientProgramSession = getActiveClientProgramSession(program);

        List<ClientProgramSessionColdProbeRecord> oldRecords = activeClientProgramSession
                .getClientProgramSessionColdProbeRecords();
        List<ClientProgramSessionColdProbeRecord> updatedRecords = updates
                .getClientProgramSessionColdProbeRecords();

        for(int i = 0; i < updatedRecords.size(); i++) {
            var curr = updatedRecords.get(i);
            curr.setSequenceNumber(i);
        }

        Map<String, ClientProgramSessionColdProbeRecord> omittedToReplacement = new HashMap<>();
        Map<String, ClientProgramSessionColdProbeRecord> targetNameToUpdatedRecord = new HashMap<>();

        for(int i = 0; i < updatedRecords.size(); i++) {
            ClientProgramSessionColdProbeRecord curr = updatedRecords.get(i);
            targetNameToUpdatedRecord.put(curr.getTarget(), curr);
            if(curr.getIsOmitted()) {
                omittedToReplacement.put(curr.getTarget(), updatedRecords.get(i + 1));
                targetNameToUpdatedRecord.put(updatedRecords.get(i + 1).getTarget(), updatedRecords.get(i + 1));
                i ++;
            }
        }

        List<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecordsToSave = new ArrayList<>();

        for(ClientProgramSessionColdProbeRecord oldRecord : oldRecords) {
            if(omittedToReplacement.containsKey(oldRecord.getTarget())) {
                ClientProgramSessionColdProbeRecord replacement = omittedToReplacement.get(oldRecord.getTarget());
                oldRecord.setIsOmitted(true);
                clientProgramSessionColdProbeRecordRepository.save(oldRecord);
                clientProgramSessionColdProbeRecordsToSave.add(oldRecord);
                clientProgramSessionColdProbeRecordRepository.save(replacement);
                clientProgramSessionColdProbeRecordsToSave.add(replacement);
            }
            else {
                ClientProgramSessionColdProbeRecord updated = targetNameToUpdatedRecord.get(oldRecord.getTarget());
                clientProgramSessionColdProbeRecordRepository.save(updated);
                clientProgramSessionColdProbeRecordsToSave.add(updated);
            }
        }
        activeClientProgramSession.setDate(DateUtil.getToday());
        activeClientProgramSession.setClientProgramSessionColdProbeRecords(clientProgramSessionColdProbeRecordsToSave);
        clientProgramSessionRepository.save(activeClientProgramSession);
    }

    public void saveMassTrialProgramSession(Program program, ClientProgramSession updates) {
        ClientProgramSession activeClientProgramSession = getActiveClientProgramSession(program);
        List<ClientProgramSessionMassTrialRecord> updatedRecords = updates
                .getClientProgramSessionMassTrialRecords();

        for(int i = 0; i < updatedRecords.size(); i++) {
            var updatedRecord = updatedRecords.get(i);
            updatedRecord.setSequenceNumber(i);
        }
        clientProgramSessionMassTrialRecordRepository.saveAll(updatedRecords);
        activeClientProgramSession.setDate(DateUtil.getToday());
        activeClientProgramSession.setClientProgramSessionMassTrialRecords(updatedRecords);
        clientProgramSessionRepository.save(activeClientProgramSession);
    }

}
