package com.kidsability.automation.service;
import com.kidsability.automation.model.*;
import com.kidsability.automation.repository.*;
import com.kidsability.automation.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BehaviourService {
    private final BehaviourRepository behaviourRepository;
    private final SharePointService sharePointService;
    private final ClientRepository clientRepository;
    private final BehaviourSessionRepository behaviourSessionRepository;
    private final BehaviourSessionItemRepository behaviourSessionItemRepository;
    private final BehaviourItemRepository behaviourItemRepository;
    private final ExcelService excelService;
    public BehaviourService(BehaviourRepository behaviourRepository, SharePointService sharePointService, ClientRepository clientRepository,
                            BehaviourSessionRepository behaviourSessionRepository, BehaviourSessionItemRepository behaviourSessionItemRepository,
                            BehaviourItemRepository behaviourItemRepository, ExcelService excelService) {
        this.behaviourRepository = behaviourRepository;
        this.sharePointService = sharePointService;
        this.clientRepository = clientRepository;
        this.behaviourSessionRepository = behaviourSessionRepository;
        this.behaviourSessionItemRepository = behaviourSessionItemRepository;
        this.behaviourItemRepository = behaviourItemRepository;
        this.excelService = excelService;
    }
    public void initBehaviour(Client client) throws ExecutionException, InterruptedException {
        var behaviourResourcePath = "General/Resources DO NOT TOUCH!/behaviour.xlsx";

        var behaviourResourceDriveItemFuture = sharePointService.getDriveItemByPathFuture(behaviourResourcePath);
        var clientRootDriveItemFuture = sharePointService.getDriveItemByIdFuture(client.getSharePointRootId());

        var clientBehaviouralRootDriveItem = sharePointService.getChildren(clientRootDriveItemFuture.get())
                .stream()
                .filter(driveItem -> driveItem.name.equals("Behavioural Data"))
                .findFirst()
                .orElse(null);

        var copyBehaviourSheetFuture = sharePointService.copyItemFuture(behaviourResourceDriveItemFuture.get(), clientBehaviouralRootDriveItem, "behaviour.xlsx");

        var copiedBehaviourSheetDriveItem = copyBehaviourSheetFuture.get();
        sharePointService.awaitCopyCompletion(copiedBehaviourSheetDriveItem);

        Behaviour behaviour = Behaviour
                .builder()
                .sharePointId(copiedBehaviourSheetDriveItem.id)
                .build();
        behaviourRepository.save(behaviour);
        client.setBehaviour(behaviour);
        clientRepository.save(client);

    }

    public BehaviourSession getActiveBehaviourSession(Behaviour behaviour) {
        var behaviourSessions = behaviour.getBehaviourSessions();
        if(behaviourSessions == null) {
            BehaviourSession activeBehaviourSession =  createNewActiveBehaviourSession(behaviour);
            behaviour.addBehaviourSession(activeBehaviourSession);
            behaviourRepository.save(behaviour);
            return activeBehaviourSession;
        }
        var activeBehaviourSession = behaviourSessions
                .stream()
                .filter(BehaviourSession::getIsActive)
                .findFirst()
                .orElse(null);
        var today = DateUtil.getToday();
        if(activeBehaviourSession == null) {
            activeBehaviourSession = createNewActiveBehaviourSession(behaviour);
            behaviour.addBehaviourSession(activeBehaviourSession);
            behaviourRepository.save(behaviour);
            return activeBehaviourSession;
        }
        else if(!activeBehaviourSession.getDate().equals(today)) {
            activeBehaviourSession.setIsActive(false);
            behaviourSessionRepository.save(activeBehaviourSession);
            activeBehaviourSession = createNewActiveBehaviourSession(behaviour);
            behaviour.addBehaviourSession(activeBehaviourSession);
            behaviourRepository.save(behaviour);
            return activeBehaviourSession;
        }
        else return activeBehaviourSession;
    }

    public BehaviourSession createNewActiveBehaviourSession(Behaviour behaviour) {
        List<BehaviourItem> behaviourItems = behaviour.getBehaviourItems();
        if(behaviourItems != null) {
            BehaviourSession activeBehaviourSession =  BehaviourSession
                    .builder()
                    .behaviourSessionItems(
                            behaviourItems
                                    .stream()
                                    .map(behaviourItem -> {
                                        var behaviourSessionItem = BehaviourSessionItem
                                                .builder()
                                                .frequency(0)
                                                .name(behaviourItem.getName())
                                                .build();
                                       return behaviourSessionItemRepository.save(behaviourSessionItem);

                                    })
                                    .toList()
                    )
                    .date(DateUtil.getToday())
                    .isActive(true)
                    .build();
            return behaviourSessionRepository.save(activeBehaviourSession);
        }
        else {
            BehaviourSession behaviourSession = BehaviourSession
                    .builder()
                    .date(DateUtil.getToday())
                    .isActive(true)
                    .behaviourSessionItems(new ArrayList<>())
                    .build();
            return behaviourSessionRepository.save(behaviourSession);
        }
    }

    public void saveActiveBehaviourSession(Client client, BehaviourSession updatedBehaviourSession) throws ExecutionException, InterruptedException {
        List<BehaviourSessionItem> updatedSessionItems = updatedBehaviourSession.getBehaviourSessionItems();
        if(updatedSessionItems == null || updatedSessionItems.size() == 0) return;
        Behaviour behaviour = client.getBehaviour();

        BehaviourSession oldSession = getActiveBehaviourSession(behaviour);
        updateOldBehaviourSession(oldSession, updatedBehaviourSession);
        updateBehaviourItems(oldSession, behaviour);
        excelService.updateBehaviourSheet(behaviour, oldSession);

    }

    private void updateOldBehaviourSession(BehaviourSession oldSession, BehaviourSession updatedSession) {
        List<BehaviourSessionItem> oldSessionItems = oldSession.getBehaviourSessionItems();
        List<BehaviourSessionItem> updatedSessionItems = updatedSession.getBehaviourSessionItems();
        if(oldSessionItems == null) {
            if(updatedSessionItems == null || updatedSessionItems.size() == 0) return;
            behaviourSessionItemRepository.saveAll(updatedSessionItems);
            oldSession.setBehaviourSessionItems(updatedSessionItems);
            behaviourSessionRepository.save(oldSession);
        }
        else {
            Map<String, BehaviourSessionItem> nameToOldSessionItem = new HashMap<>();
            for(var oldSessionItem : oldSessionItems) {
                nameToOldSessionItem.put(oldSessionItem.getName(), oldSessionItem);
            }
            for(var updatedSessionItem : updatedSessionItems) {
                if(!nameToOldSessionItem.containsKey(updatedSessionItem.getName())) {
                    behaviourSessionItemRepository.save(updatedSessionItem);
                    oldSession.addBehaviourSessionItem(updatedSessionItem);
                    nameToOldSessionItem.put(updatedSessionItem.getName(), updatedSessionItem);
                }
                var oldSessionItem = nameToOldSessionItem.get(updatedSessionItem.getName());
                if(!(oldSessionItem.getName().equals(updatedSessionItem.getName()) && oldSessionItem.getFrequency() == updatedSessionItem.getFrequency())) {
                    oldSessionItem.setFrequency(updatedSessionItem.getFrequency());
                    behaviourSessionItemRepository.save(oldSessionItem);
                }
            }
            behaviourSessionRepository.save(oldSession);
        }
    }

    private void updateBehaviourItems(BehaviourSession behaviourSession, Behaviour behaviour) {
        List<BehaviourItem> existingBehaviourItems = behaviour.getBehaviourItems();
        List<BehaviourSessionItem> sessionItems = behaviourSession.getBehaviourSessionItems();
        if(sessionItems == null || sessionItems.size() == 0) return;
        if(existingBehaviourItems == null) {
            for(int i = 0; i < sessionItems.size(); i++) {
                var sessionItem = sessionItems.get(i);
                BehaviourItem behaviourItem = BehaviourItem
                        .builder()
                        .name(sessionItem.getName())
                        .excelColNum(i + 2)
                        .build();
                behaviourItemRepository.save(behaviourItem);
                behaviour.addBehaviourItem(behaviourItem);
            }
            behaviourRepository.save(behaviour);
        }
        else {
            Map<String, BehaviourItem> nameToExistingBehaviourItem = new HashMap<>();
            for(var existingBehaviourItem : existingBehaviourItems) {
                nameToExistingBehaviourItem.put(existingBehaviourItem.getName(), existingBehaviourItem);
            }

            for(var sessionItem : sessionItems) {
                if(!nameToExistingBehaviourItem.containsKey(sessionItem.getName())) {
                    BehaviourItem behaviourItem = BehaviourItem
                            .builder()
                            .name(sessionItem.getName())
                            .excelColNum(nameToExistingBehaviourItem.size() + 2)
                            .build();
                    behaviourItemRepository.save(behaviourItem);
                    behaviour.addBehaviourItem(behaviourItem);
                    nameToExistingBehaviourItem.put(behaviourItem.getName(), behaviourItem);
                }
            }
            behaviourRepository.save(behaviour);
        }
    }

    public String getExcelEmbeddableLink(Behaviour behaviour) throws ExecutionException, InterruptedException {
        var driveItem = sharePointService.getDriveItemById(behaviour.getSharePointId());
        return sharePointService
                .getEmbeddableLinkFuture(driveItem)
                .get()
                .getUrl;
    }

}
