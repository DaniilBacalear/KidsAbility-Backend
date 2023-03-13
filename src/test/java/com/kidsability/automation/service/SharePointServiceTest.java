package com.kidsability.automation.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SharePointServiceTest {
    @Autowired
    private SharePointService sharePointService;

    @Test
    void getDriveItem_driveItemExists_returnDriveItem() {
        var path = "/General/Clients";
        var driveItem = sharePointService.getDriveItem(path);
        assertEquals("Clients", driveItem.name);
    }
    @Test
    void getDriveItem_driveItemDoesNotExist_returnNull() {
        var path = "/nothingHere";
        var driveItem = sharePointService.getDriveItem(path);
        assertNull(driveItem);
    }

    @Test
    void temp() throws IOException {
        var path = "/General/ProgramTemplates/Program example.pdf";
        var driveItem = sharePointService.getDriveItem(path);
        var base64 = sharePointService.getBase64Img(driveItem);
        System.out.println(base64);
    }

}