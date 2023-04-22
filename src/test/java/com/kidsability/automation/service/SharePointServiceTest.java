package com.kidsability.automation.service;

import com.kidsability.automation.factory.WorkBookFactory;
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
    @Autowired
    private ExcelService excelService;

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

    @Test
    void getCellFill() throws Exception {
        var id = "01JLKQL4SQU6TKB7K3BFGJMY4PXBBTGHWM";
        var cellAddress = excelService.getCellAddress(21, 4);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFill(excelDriveItem, cellAddress).get();
        var a = 1;

    }

    @Test
    void getCellFont() throws Exception {
        var id = "01JLKQL4SQU6TKB7K3BFGJMY4PXBBTGHWM";
        var cellAddress = excelService.getCellAddress(23, 3);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFont(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void getCellFormat() throws Exception {
        var id = "01JLKQL4SQU6TKB7K3BFGJMY4PXBBTGHWM";
        var cellAddress = excelService.getCellAddress(23, 3);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFormat(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void getCellBorders() throws Exception {
        var id = "01JLKQL4SQU6TKB7K3BFGJMY4PXBBTGHWM";
        var cellAddress = excelService.getCellAddress(23, 3);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellBorders(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void setFill() throws Exception {
        var id = "01JLKQL4SQU6TKB7K3BFGJMY4PXBBTGHWM";
        var cellAddress = excelService.getCellAddress(23, 3);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var green = "#00B050";
        var workBookRangeFill = WorkBookFactory.getWorkBookRangeFill(green);
        sharePointService.updateWorkBookCellFillAsync(excelDriveItem, cellAddress, workBookRangeFill);
    }


}