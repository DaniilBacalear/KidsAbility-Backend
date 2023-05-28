package com.kidsability.automation.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.kidsability.automation.factory.WorkBookFactory;
import com.kidsability.automation.model.*;
import com.kidsability.automation.util.DateUtil;
import com.microsoft.graph.models.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ExcelService {
    private static final int COLD_PROBE_MATRIX_ROW_START = 19;
    public static final int COLD_PROBE_MATRIX_ROW_END = 309;
    public static final int COLD_PROBE_MATRIX_ROW_GAP = 3;
    public static final int COLD_PROBE_MATRIX_COL_END = 31;

    public static final String GREY_RGB = "#C0C0C0";
    public static final String GREEN_RGB = "#00B050";
    public static final String RED_RGB = "#FF0000";

    public static final String Y = "   Y";
    public static final String N = "           N";



    private final SharePointService sharePointService;
    public ExcelService(SharePointService sharePointService) {
        this.sharePointService = sharePointService;
    }

    public void initColdProbeSheet(Program program) throws ExecutionException, InterruptedException {
        ColdProbeSheet coldProbeSheet = program.getColdProbeSheet();
        List<ColdProbeSheetItem> targets = coldProbeSheet.getColdProbeSheetItems();

        coldProbeSheet.setExcelRowEnd( COLD_PROBE_MATRIX_ROW_START + (targets.size() * COLD_PROBE_MATRIX_ROW_GAP) - 1);
        coldProbeSheet.setExcelColEnd(COLD_PROBE_MATRIX_COL_END);

        DriveItem coldProbeSheetDriveItem = sharePointService.getDriveItemById(coldProbeSheet.getSharePointId());

        String rangeAddress = getRangeAddress(COLD_PROBE_MATRIX_ROW_END, COLD_PROBE_MATRIX_COL_END);

        WorkbookRange workbookRange = sharePointService
                .getWorkBookRange(coldProbeSheetDriveItem, rangeAddress)
                .get();
        JsonArray matrix = workbookRange.formulas.getAsJsonArray();

        initColdProbeDefaults(coldProbeSheet, matrix);
        initColdProbeTargets(coldProbeSheet, matrix);

        sharePointService.updateWorkBookRange(coldProbeSheetDriveItem, rangeAddress, workbookRange);
        sharePointService.clearWorkBookRange(coldProbeSheetDriveItem,
                getRangeAddress(coldProbeSheet.getExcelRowEnd() + 1, 1, COLD_PROBE_MATRIX_ROW_END, COLD_PROBE_MATRIX_COL_END));

    }

    private void initColdProbeDefaults(ColdProbeSheet coldProbeSheet, JsonArray matrix) {
        int[] childCell = getRowColFromExcelName("D3");
        int[] codeCell = getRowColFromExcelName("D5");
        int[] objectiveCell = getRowColFromExcelName("D7");
        int[] sdCell = getRowColFromExcelName("D11");
        int[] taskNameCell = getRowColFromExcelName("J4");
        int[] exampleCell = getRowColFromExcelName("I7");
        int[] criterionToMasteryCell = getRowColFromExcelName("H13");
        int[] criteriaCell = getRowColFromExcelName("L12");

        matrix.get(childCell[0])
                .getAsJsonArray()
                .set(childCell[1], new JsonPrimitive(coldProbeSheet.getChild() == null ? "" : coldProbeSheet.getChild()));

        matrix.get(codeCell[0])
                .getAsJsonArray()
                .set(codeCell[1], new JsonPrimitive(coldProbeSheet.getCode()));

        matrix.get(objectiveCell[0])
                .getAsJsonArray()
                .set(objectiveCell[1], new JsonPrimitive(coldProbeSheet.getObjective()));

        matrix.get(sdCell[0])
                .getAsJsonArray()
                .set(sdCell[1], new JsonPrimitive(coldProbeSheet.getSd()));

        matrix.get(taskNameCell[0])
                .getAsJsonArray()
                .set(taskNameCell[1], new JsonPrimitive(coldProbeSheet.getTaskName()));

        matrix.get(exampleCell[0])
                .getAsJsonArray()
                .set(exampleCell[1], new JsonPrimitive(coldProbeSheet.getExample()));

        matrix.get(criterionToMasteryCell[0])
                .getAsJsonArray()
                .set(criterionToMasteryCell[1], new JsonPrimitive(coldProbeSheet.getCriterionToMastery()));

        matrix.get(criteriaCell[0])
                .getAsJsonArray()
                .set(criteriaCell[1], new JsonPrimitive(coldProbeSheet.getCriteria()));

    }

    private void initColdProbeTargets(ColdProbeSheet coldProbeSheet, JsonArray matrix) {
        List<ColdProbeSheetItem> targets = coldProbeSheet.getColdProbeSheetItems();
        for(int row = COLD_PROBE_MATRIX_ROW_START, targetIdx = 0; row < coldProbeSheet.getExcelRowEnd(); row += COLD_PROBE_MATRIX_ROW_GAP, targetIdx ++) {
            int col = 0;
            ColdProbeSheetItem target = targets.get(targetIdx);
            matrix.get(row).getAsJsonArray().set(col, new JsonPrimitive(target.getTargetName()));
        }
    }

    public void initMassTrialSheet(Program program) throws ExecutionException, InterruptedException {
        var massTrialSheet = program.getMassTrialSheet();
        var excelDriveItem = sharePointService.getDriveItemById(massTrialSheet.getSharePointId());
        var sessionId = sharePointService.getExcelSessionId(excelDriveItem);
        initMassTrialSheetTargets(massTrialSheet, excelDriveItem, sessionId);
        initMassTrialSheetDefaults(massTrialSheet, excelDriveItem, sessionId);
    }

    public void initMassTrialSheetTargets(MassTrialSheet massTrialSheet, DriveItem excelDriveItem, String sessionId) {
        var targets = massTrialSheet.getMassTrialSheetItems();
        for(var target : targets) {
            var oldName = "Sheet" + target.getSheetNumber();
            var newName = target.getTargetName();
            sharePointService.updateWorkSheetName(excelDriveItem, oldName, newName, sessionId);
        }
    }

    public void initMassTrialSheetDefaults(MassTrialSheet massTrialSheet, DriveItem excelDriveItem, String sessionId) throws ExecutionException, InterruptedException {
        var workBookSheetName = "Mass Trial Info";
        var massTrialSheetInfoWorkSheetRowEnd = 17;
        var massTrialSheetInfoWorkSheetColEnd = 14;
        var rangeAddress = getRangeAddress(massTrialSheetInfoWorkSheetRowEnd, massTrialSheetInfoWorkSheetColEnd);
        WorkbookRange workbookRange = sharePointService
                .getWorkBookRange(excelDriveItem, rangeAddress, workBookSheetName, sessionId)
                .get();
        JsonArray matrix = workbookRange.formulas.getAsJsonArray();

        List<int[]> defaultCellAddresses = new ArrayList<>();
        List<String> formulaValues = new ArrayList<>();

        int[] childCell = getRowColFromExcelName("C3");
        defaultCellAddresses.add(childCell);
        formulaValues.add(massTrialSheet.getChild());

        int[] taskNameCell = getRowColFromExcelName("F3");
        defaultCellAddresses.add(taskNameCell);
        formulaValues.add(massTrialSheet.getTaskName());

        int[] sdCell = getRowColFromExcelName("L3");
        defaultCellAddresses.add(sdCell);
        formulaValues.add(massTrialSheet.getSd());

        int[] targetMasteryCell = getRowColFromExcelName("C5");
        defaultCellAddresses.add(targetMasteryCell);
        formulaValues.add(massTrialSheet.getTargetMastery());

        int[] revisionCriteriaCell = getRowColFromExcelName("J5");
        defaultCellAddresses.add(revisionCriteriaCell);
        formulaValues.add(massTrialSheet.getRevisionCriteria());

        int[] promptLegendCell = getRowColFromExcelName("C11");
        defaultCellAddresses.add(promptLegendCell);
        formulaValues.add(massTrialSheet.getPromptLegend());

        int[] instructionsCell = getRowColFromExcelName("L11");
        defaultCellAddresses.add(instructionsCell);
        formulaValues.add(massTrialSheet.getInstructions());

        for(int i = 0; i < formulaValues.size(); i++) {
            int[] cell = defaultCellAddresses.get(i);
            String formulaValue = formulaValues.get(i);
            matrix.get(cell[0])
                    .getAsJsonArray()
                    .set(cell[1], new JsonPrimitive(formulaValue));
        }

        sharePointService.updateWorkBookRange(excelDriveItem, rangeAddress, workbookRange, workBookSheetName, sessionId);
    }

    private String getExcelColumnName(int colNum) {
        final StringBuilder sb = new StringBuilder();

        int num = colNum - 1;
        while (num >=  0) {
            int numChar = (num % 26)  + 65;
            sb.append((char)numChar);
            num = (num  / 26) - 1;
        }
        return sb.reverse().toString();
    }

    private int getExcelColumnNumber(String column) {
        int result = 0;
        for (int i = 0; i < column.length(); i++) {
            result *= 26;
            result += column.charAt(i) - 'A' + 1;
        }
        return result;
    }

    public String getRangeAddress(int rowEnd, int colEnd) {
        return "A1:" + getExcelColumnName(colEnd) + rowEnd;
    }

    public String getRangeAddress(int rowStart, int colStart, int rowEnd, int colEnd) {
        String start = getExcelColumnName(colStart) + rowStart;
        String end = getExcelColumnName(colEnd) + rowEnd;
        return start + ":" + end;
    }

    private int[] getRowColFromExcelName(String excelName) {
        int[] coordinates = new int[2];
        StringBuilder sb = new StringBuilder();
        int row;
        int col;
        for(int i = 0; i < excelName.length(); i++) {
            char curr = excelName.charAt(i);
            if(Character.isDigit(curr)) {
                row = Integer.parseInt(excelName.substring(i));
                col = getExcelColumnNumber(sb.toString());
                coordinates[0] = row - 1;
                coordinates[1] = col - 1;
                return coordinates;
            }
            else sb.append(curr);
        }
        return coordinates;
    }

    public String getCellAddress(int row, int col) {
        return "$" + getExcelColumnName(col) + "$" + row;
    }

    public String getRangeAddress(String topLeftCellAddress, String bottomRightCellAddress) {
        StringBuilder topLeftFormatted = new StringBuilder();
        StringBuilder bottomRightFormatted = new StringBuilder();

        for(char c : topLeftCellAddress.toCharArray()) {
            if(c != '$') topLeftFormatted.append(c);
        }

        for(char c : bottomRightCellAddress.toCharArray()) {
            if(c != '$') bottomRightFormatted.append(c);
        }

        return topLeftFormatted + ":" + bottomRightFormatted;
    }

    public void addColdProbeSession(ColdProbeSheet coldProbeSheet, ClientProgramSession session, Practitioner practitioner) throws Exception{
        DriveItem excelDriveItem = sharePointService.getDriveItemById(coldProbeSheet.getSharePointId());
        String workbookSessionId = sharePointService.getExcelSessionId(excelDriveItem);

        Map<String, Integer> targetNameToRowNum = coldProbeSheet
                .getColdProbeSheetItems()
                .stream()
                .collect(Collectors.toMap(ColdProbeSheetItem::getTargetName, ColdProbeSheetItem::getRowNum));

        List<ClientProgramSessionColdProbeRecord> updatedRecords = session.getClientProgramSessionColdProbeRecords();
        for(int i = 0; i < updatedRecords.size(); i++) {
            ClientProgramSessionColdProbeRecord updatedRecord = updatedRecords.get(i);
            if(updatedRecord.getIsOmitted()) {
                int targetRowNum = targetNameToRowNum.get(updatedRecord.getTarget());
                omitTargetColdProbe(targetRowNum, excelDriveItem, workbookSessionId);
                ClientProgramSessionColdProbeRecord replacement = updatedRecords.get(i + 1);
                int replacementTargetRowNum = targetNameToRowNum.get(replacement.getTarget());
                addColdProbeTarget(replacementTargetRowNum, replacement.getTarget(), excelDriveItem, workbookSessionId);
                i ++;
            }
        }

        Map<String, Boolean> targetNameToIsTargetMastered = coldProbeSheet
                .getColdProbeSheetItems()
                .stream()
                .collect(Collectors.toMap(ColdProbeSheetItem::getTargetName, ColdProbeSheetItem::getIsMastered));

        for(int i = 0; i < updatedRecords.size(); i++) {
            ClientProgramSessionColdProbeRecord updatedRecord = updatedRecords.get(i);
            if(updatedRecord.getIsRecorded()) {
                int targetRowNum = targetNameToRowNum.get(updatedRecord.getTarget());
                updateTargetRecordEntryColdProbe(targetRowNum, coldProbeSheet.getPersistedSessions(), updatedRecord.getIsMet(), targetNameToIsTargetMastered.get(updatedRecord.getTarget()), excelDriveItem, workbookSessionId);
            }
        }

        updateSessionDateAndPractitionerHeaderColdProbe(coldProbeSheet.getPersistedSessions(), practitioner.getInitials(),DateUtil.getToday(), excelDriveItem, workbookSessionId );
    }

    public void addColdProbeTarget(int targetRowNum, String target, DriveItem excelDriveItem, String workbookSessionId) throws Exception{
        String workSheetName = "Sheet1";
        int targetRowsTopLeftExcelRowNum = coldProbeTargetRowNumToExcelRowNum(targetRowNum);
        int targetRowsTopLeftExcelColNum = 1;

        int targetBoxTopLeftExcelRowNum = coldProbeTargetRowNumToExcelRowNum(targetRowNum) + 1;
        int targetBoxTopLeftExcelColNum = 1;
        int targetBoxBottomRightExcelRowNum = targetBoxTopLeftExcelRowNum + 1;
        int targetBoxBottomRightExcelColNum = 2;

        List<WorkbookRangeBorder> leftTopRightBorders = WorkBookFactory.getLeftTopRightBorders();
        List<WorkbookRangeBorder> leftBottomRightBorders = WorkBookFactory.getLeftBottomRightBorders();

        WorkbookRangeFill greyFill = WorkBookFactory.getWorkBookRangeFill(GREY_RGB);

        // create grey separator above target box
        String greyTargetSeparatorAddress = getRangeAddress(targetRowsTopLeftExcelRowNum, targetRowsTopLeftExcelColNum, targetRowsTopLeftExcelRowNum, 2);
        sharePointService.mergeCells(excelDriveItem, greyTargetSeparatorAddress, workbookSessionId);
        sharePointService.updateWorkBookCellFill(excelDriveItem, greyTargetSeparatorAddress, greyFill, workSheetName, workbookSessionId);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, greyTargetSeparatorAddress, leftTopRightBorders, workbookSessionId);

        // add grey separator for entire row
        String greyRowSeparatorAddress = getRangeAddress(targetRowsTopLeftExcelRowNum, targetRowsTopLeftExcelColNum + 2, targetRowsTopLeftExcelRowNum, COLD_PROBE_MATRIX_COL_END - 1);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, greyRowSeparatorAddress, leftTopRightBorders, workbookSessionId);
        sharePointService.updateWorkBookCellFill(excelDriveItem, greyRowSeparatorAddress, greyFill, workSheetName, workbookSessionId);

        // create target box

        WorkbookRangeFont targetFont = WorkBookFactory.getColdProbeWorkBookTargetFont();
        WorkbookRangeFill targetFill = WorkBookFactory.getColdProbeWorkBookTargetFill();
        WorkbookRangeFormat targetBoxFormat = WorkBookFactory.getColdProbeWorkBookTargetFormat();

        String topRowAddress = getRangeAddress(targetBoxTopLeftExcelRowNum, targetBoxTopLeftExcelColNum, targetBoxTopLeftExcelRowNum, targetBoxBottomRightExcelColNum);
        sharePointService.updateWorkBookCellFill(excelDriveItem, topRowAddress, targetFill, workSheetName, workbookSessionId);
        sharePointService.updateWorkBookCellFont(excelDriveItem, topRowAddress, targetFont, workbookSessionId);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, topRowAddress, leftTopRightBorders, workbookSessionId);

        String bottomRowAddress = getRangeAddress(targetBoxBottomRightExcelRowNum, targetBoxTopLeftExcelColNum, targetBoxBottomRightExcelRowNum, targetBoxBottomRightExcelColNum);
        sharePointService.updateWorkBookCellFill(excelDriveItem, bottomRowAddress, targetFill, workSheetName, workbookSessionId);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, bottomRowAddress, leftBottomRightBorders, workbookSessionId);

        String targetBoxAddress = getRangeAddress(targetBoxTopLeftExcelRowNum, targetBoxTopLeftExcelColNum, targetBoxBottomRightExcelRowNum, targetBoxBottomRightExcelColNum);
        sharePointService.mergeCells(excelDriveItem, targetBoxAddress, workbookSessionId);

        sharePointService.updateWorkBookCellFormat(excelDriveItem, targetBoxAddress, targetBoxFormat, workbookSessionId);

        // set borders, format and fonts for YN cells
        WorkbookRangeFormat yNFormat = WorkBookFactory.getWorkBookFormatYN();
        WorkbookRangeFont yNFont = WorkBookFactory.getWorkBookFontYN();

        // Y borders, format and fonts
        String yCellRowAddress = getRangeAddress(targetBoxTopLeftExcelRowNum, targetBoxTopLeftExcelColNum + 2, targetBoxTopLeftExcelRowNum, COLD_PROBE_MATRIX_COL_END - 1);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, yCellRowAddress, leftTopRightBorders, workbookSessionId);
        sharePointService.updateWorkBookCellFormat(excelDriveItem, yCellRowAddress, yNFormat, workbookSessionId);
        sharePointService.updateWorkBookCellFont(excelDriveItem, yCellRowAddress, yNFont, workbookSessionId);

        // N borders, format and fonts
        String nCellRowAddress = getRangeAddress(targetBoxTopLeftExcelRowNum + 1, targetBoxTopLeftExcelColNum + 1, targetBoxTopLeftExcelRowNum + 1, COLD_PROBE_MATRIX_COL_END - 1);
        sharePointService.updateWorkBookCellBorders(excelDriveItem, nCellRowAddress, leftBottomRightBorders, workbookSessionId);
        sharePointService.updateWorkBookCellFormat(excelDriveItem, nCellRowAddress, yNFormat, workbookSessionId);
        sharePointService.updateWorkBookCellFont(excelDriveItem, nCellRowAddress, yNFont, workbookSessionId);

        // set formula values
        String targetRowRangeAddress = getRangeAddress(targetBoxTopLeftExcelRowNum, targetBoxTopLeftExcelColNum, targetBoxBottomRightExcelRowNum, COLD_PROBE_MATRIX_COL_END - 1);
        WorkbookRange workbookRange = sharePointService.getWorkBookRange(excelDriveItem, targetRowRangeAddress, workSheetName, workbookSessionId).get();
        JsonArray matrix = workbookRange.formulas.getAsJsonArray();

        for(int i = 0; i < matrix.size(); i++) {
            for(int j = 0; j < matrix.get(i).getAsJsonArray().size(); j++) {
                if(i == 0 && j == 0) {
                    matrix.get(i).getAsJsonArray().set(j, new JsonPrimitive(target));
                }
                else if(i == 0 && j > 1) {
                    matrix.get(i).getAsJsonArray().set(j, new JsonPrimitive(Y));
                }
                else if(i == 1 && j > 1) {
                    matrix.get(i).getAsJsonArray().set(j, new JsonPrimitive(N));
                }
            }
        }

        sharePointService.updateWorkBookRange(excelDriveItem, targetRowRangeAddress, workbookRange);
    }

    // converts model target rowNum to excelRowNum within cold probe sheet
    public int coldProbeTargetRowNumToExcelRowNum(int targetRowNum) {
        return COLD_PROBE_MATRIX_ROW_START + targetRowNum * COLD_PROBE_MATRIX_ROW_GAP;
    }

    public void omitTargetColdProbe(int targetRowNum, DriveItem excelDriveItem, String workbookSessionId) {
        String workSheetName = "Sheet1";
        int targetBoxTopLeftExcelRowNum = coldProbeTargetRowNumToExcelRowNum(targetRowNum) + 1;
        int targetBoxTopLeftExcelColNum = 1;
        int targetBoxBottomRightExcelRowNum = targetBoxTopLeftExcelRowNum + 1;
        int targetBoxBottomRightExcelColNum = 2;
        String targetBoxRangeAddress = getRangeAddress(targetBoxTopLeftExcelRowNum, targetBoxTopLeftExcelColNum, targetBoxBottomRightExcelRowNum, targetBoxBottomRightExcelColNum);
        WorkbookRangeFill fill = WorkBookFactory.getWorkBookRangeFill(RED_RGB);
        sharePointService.updateWorkBookCellFill(excelDriveItem, targetBoxRangeAddress, fill, workSheetName, workbookSessionId);

        int targetRowsTopLeftExcelRowNum = coldProbeTargetRowNumToExcelRowNum(targetRowNum) + COLD_PROBE_MATRIX_ROW_GAP;
        int targetRowsTopLeftExcelColNum = 1;
        int targetRowsBottomRightExcelRowNum = targetRowsTopLeftExcelRowNum + 2;
        int targetRowsBottomRightExcelColNum = COLD_PROBE_MATRIX_COL_END;

        String targetRowsRangeAddress = getRangeAddress(targetRowsTopLeftExcelRowNum, targetRowsTopLeftExcelColNum, targetRowsBottomRightExcelRowNum, targetRowsBottomRightExcelColNum);
        sharePointService.shiftCellsDown(excelDriveItem, targetRowsRangeAddress, workbookSessionId);
    }

    public void updateTargetRecordEntryColdProbe(int targetRowNum, int persistedSessions, boolean isMet, boolean isMastered, DriveItem excelDriveItem, String workbookSessionId) {
        String workSheetName = "Sheet1";
        if(isMet) {
            int excelRow = coldProbeTargetRowNumToExcelRowNum(targetRowNum) + 1;
            int excelColStart  = 1;
            int excelCol = 2 + persistedSessions;
            String cellAddress = getCellAddress(excelRow, excelCol);
            WorkbookRangeFill metFill = WorkBookFactory.getWorkBookRangeFill(GREEN_RGB);
            sharePointService.updateWorkBookCellFill(excelDriveItem, cellAddress, metFill, workSheetName, workbookSessionId);
            if(isMastered) {
                String targetNameCellAddress = getCellAddress(excelRow, excelColStart);
                sharePointService.updateWorkBookCellFill(excelDriveItem, targetNameCellAddress, metFill, workSheetName, workbookSessionId);
            }
        }
        else {
            int excelRow = coldProbeTargetRowNumToExcelRowNum(targetRowNum) + 2;
            int excelCol = 2 + persistedSessions;
            String cellAddress = getCellAddress(excelRow, excelCol);
            WorkbookRangeFill unmetFill = WorkBookFactory.getWorkBookRangeFill(RED_RGB);
            sharePointService.updateWorkBookCellFill(excelDriveItem, cellAddress, unmetFill, workSheetName, workbookSessionId);
        }
    }

    public void updateSessionDateAndPractitionerHeaderColdProbe(int persistedSessions, String practitionerInitials, LocalDate date, DriveItem excelDriveItem , String workbookSessionId) throws ExecutionException, InterruptedException {
        String workSheetName = "Sheet1";
        int row = COLD_PROBE_MATRIX_ROW_START;
        int col = 2 + persistedSessions;
        String header = DateUtil.getMDY(date) + " " + practitionerInitials;
        String cellAddress = getCellAddress(row, col);
        WorkbookRange workbookRange = sharePointService.getWorkBookRange(excelDriveItem, cellAddress, workSheetName, workbookSessionId).get();
        workbookRange
                .formulas
                .getAsJsonArray()
                .get(0)
                .getAsJsonArray().set(0, new JsonPrimitive(header));
        sharePointService.updateWorkBookRange(excelDriveItem, cellAddress, workbookRange, workSheetName, workbookSessionId);
    }


}
