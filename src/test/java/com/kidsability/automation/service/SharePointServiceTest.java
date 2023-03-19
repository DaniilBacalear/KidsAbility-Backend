package com.kidsability.automation.service;

import com.kidsability.automation.model.Client;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SharePointServiceTest {
    @Autowired
    private SharePointService sharePointService;

    @MockBean
    private Client clientMock;

    @Test
    void getDriveItem_driveItemExists_returnDriveItem() {
        var path = "/General/Clients";
        var driveItem = sharePointService.getDriveItemByPath(path);
        assertEquals("Clients", driveItem.name);
    }
    @Test
    void getDriveItem_driveItemDoesNotExist_returnNull() {
        var path = "/nothingHere";
        var driveItem = sharePointService.getDriveItemByPath(path);
        assertNull(driveItem);
    }

    @Test
    void base64Encoding() throws IOException {
        var path = "/General/ProgramTemplates/Program example.docx";
        var driveItem = sharePointService.getDriveItemByPath(path);
        var base64 = sharePointService.getBase64Img(driveItem);
        System.out.println(base64);
    }

    @Test
    void createFolder() {
        Mockito.when(clientMock.getKidsAbilityId())
                .thenReturn("c2");
        sharePointService.createClientFolders(clientMock);
    }


}