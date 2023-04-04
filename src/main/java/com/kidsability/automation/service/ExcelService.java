package com.kidsability.automation.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kidsability.automation.model.ColdProbeSheet;
import com.kidsability.automation.model.ColdProbeSheetItem;
import com.kidsability.automation.model.Program;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.WorkbookRange;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ExcelService {
    private static final int COLD_PROBE_MATRIX_ROW_START = 19;
    public static final int COLD_PROBE_MATRIX_ROW_GAP = 3;
    public static final int COLD_PROBE_MATRIX_COL_START = 16;
    private final SharePointService sharePointService;
    public ExcelService(SharePointService sharePointService) {
        this.sharePointService = sharePointService;
    }

    public void initColdProbeSheet(Program program) throws ExecutionException, InterruptedException {
        ColdProbeSheet coldProbeSheet = program.getColdProbeSheet();
        List<ColdProbeSheetItem> targets = coldProbeSheet.getColdProbeSheetItems();

        coldProbeSheet.setExcelRowEnd( COLD_PROBE_MATRIX_ROW_START + (targets.size() * COLD_PROBE_MATRIX_ROW_GAP) - 1);
        coldProbeSheet.setExcelColEnd(COLD_PROBE_MATRIX_COL_START);

        DriveItem coldProbeSheetDriveItem = sharePointService.getDriveItemById(coldProbeSheet.getSharePointId());

        String rangeAddress = getRangeAddress(coldProbeSheet.getExcelRowEnd(), coldProbeSheet.getExcelColEnd());

        WorkbookRange workbookRange = sharePointService
                .getWorkBookRange(coldProbeSheetDriveItem, rangeAddress)
                .get();
        JsonArray matrix = workbookRange.formulas.getAsJsonArray();

        initColdProbeDefaults(coldProbeSheet, matrix);
        initColdProbeTargets(coldProbeSheet, matrix);

        var res = sharePointService.updateWorkBookRange(coldProbeSheetDriveItem, rangeAddress, workbookRange);
        var temp = 1;


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

    private String getRangeAddress(int rowEnd, int colEnd) {
        return "A1:" + getExcelColumnName(colEnd) + rowEnd;
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
}
