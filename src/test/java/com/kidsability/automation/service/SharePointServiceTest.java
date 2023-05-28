package com.kidsability.automation.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.kidsability.automation.factory.WorkBookFactory;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.pojo.ExcelCell;
import com.kidsability.automation.util.DateUtil;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.WorkbookRangeBorderCollectionPage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        var id = "01JLKQL4TYFIBUDB4MUJHLLBTKZ6UUR3KG";
        var cellAddress = excelService.getCellAddress(20, 1);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFill(excelDriveItem, cellAddress).get();
        var a = 1;

    }

    @Test
    void getCellFont() throws Exception {
        var id = "01JLKQL4TYFIBUDB4MUJHLLBTKZ6UUR3KG";
        var cellAddress = excelService.getCellAddress(20, 1);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFont(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void getCellFormat() throws Exception {
        var id = "01JLKQL4TYFIBUDB4MUJHLLBTKZ6UUR3KG";
        var cellAddress = excelService.getCellAddress(26, 4);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellFormat(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void getCellBorders() throws Exception {
        var id = "01JLKQL4TYFIBUDB4MUJHLLBTKZ6UUR3KG";
        var cellAddress = excelService.getCellAddress(29, 3);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var res = sharePointService.getWorkBookCellBorders(excelDriveItem, cellAddress).get();
        var a = 1;
    }

    @Test
    void setFill() throws Exception {
        var id = "01JLKQL4TYFIBUDB4MUJHLLBTKZ6UUR3KG";
        var cellAddress = excelService.getCellAddress(42, 1);
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var green = "#00B050";
        var workBookRangeFill = WorkBookFactory.getWorkBookRangeFill(green);
        var sessionToken = sharePointService.getExcelSessionId(excelDriveItem);
        sharePointService.updateWorkBookCellFill(excelDriveItem, "A42:D42", workBookRangeFill,"Sheet1", sessionToken);
        
    }

    @Test
    void changeWorksheetName() {
        var id ="01JLKQL4UQABEVZOJODFGJTTCFHSOA2VAI";
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var oldName = "Sheet28";
        var newName = "Sheet1";
        var sessionToken = sharePointService.getExcelSessionId(excelDriveItem);
        sharePointService.updateWorkSheetName(excelDriveItem, oldName, newName, sessionToken);
    }

    @Test
    void initMassTrialDefaults() {
        var id = "01JLKQL4UMU6CMM6YJHRG2OV7THFLEWLBO";
        var excelDriveItem = sharePointService.getDriveItemById(id);
        var sessionToken = sharePointService.getExcelSessionId(excelDriveItem);

        var workBookSheetName = "Mass Trial Info";
        var massTrialSheetInfoWorkSheetRowEnd = 17;
        var massTrialSheetInfoWorkSheetColEnd = 14;
        var rangeAddress = excelService.getRangeAddress(massTrialSheetInfoWorkSheetRowEnd, massTrialSheetInfoWorkSheetColEnd);
        try {
            WorkbookRange workbookRange = sharePointService
                    .getWorkBookRange(excelDriveItem, rangeAddress, workBookSheetName, sessionToken)
                    .get();
        }
        catch (Exception e) {
            var message = e.getMessage();
            var a = 1;
        }
    }

}