package com.kidsability.automation.factory;

import com.microsoft.graph.models.WorkbookRangeBorder;
import com.microsoft.graph.models.WorkbookRangeFill;
import com.microsoft.graph.models.WorkbookRangeFont;
import com.microsoft.graph.models.WorkbookRangeFormat;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class WorkBookFactory {
    public static WorkbookRangeFill getWorkBookRangeFill(String colour) {
        WorkbookRangeFill workbookRangeFill = new WorkbookRangeFill();
        workbookRangeFill.color = colour;
        return workbookRangeFill;
    }

    public static WorkbookRangeFont getWorkBookFontYN() {
        WorkbookRangeFont workbookRangeFont = new WorkbookRangeFont();
        workbookRangeFont.color = "#000000";
        workbookRangeFont.name = "Arial";
        workbookRangeFont.size = 10.0;
        workbookRangeFont.italic = false;
        workbookRangeFont.bold = true;
        return workbookRangeFont;
    }

    public static WorkbookRangeFormat getWorkBookFormatYN() {
        WorkbookRangeFormat workbookRangeFormat = new WorkbookRangeFormat();
        workbookRangeFormat.columnWidth = 59.25;
        workbookRangeFormat.rowHeight = 12.75;
        workbookRangeFormat.horizontalAlignment = "Left";
        workbookRangeFormat.verticalAlignment = "Bottom";
        workbookRangeFormat.wrapText = false;
        return workbookRangeFormat;
    }

    public static WorkbookRangeFont getColdProbeWorkBookTargetFont() {
        WorkbookRangeFont workbookRangeFont = new WorkbookRangeFont();
        workbookRangeFont.bold = false;
        workbookRangeFont.color = "#000000";
        workbookRangeFont.italic = false;
        workbookRangeFont.name = "Calibri";
        workbookRangeFont.size = 10.0;
        workbookRangeFont.underline = "None";
        return workbookRangeFont;
    }

    public static WorkbookRangeFill getColdProbeWorkBookTargetFill() {
        return getWorkBookRangeFill("#FFFFFF");
    }

    public static WorkbookRangeFormat getColdProbeWorkBookTargetFormat() {
        WorkbookRangeFormat workbookRangeFormat = new WorkbookRangeFormat();
        workbookRangeFormat.columnWidth = 66.75;
        workbookRangeFormat.horizontalAlignment = "Left";
        workbookRangeFormat.rowHeight = 12.75;
        workbookRangeFormat.verticalAlignment = "Top";
        workbookRangeFormat.wrapText = true;
        return workbookRangeFormat;
    }

    public static List<WorkbookRangeBorder> getColdProbeWorkBookTargetBorders() {
        List<WorkbookRangeBorder> workbookRangeBorders = new ArrayList<>();
        List<String> sideIndices = List.of(
                "EdgeTop", "EdgeBottom", "EdgeLeft", "EdgeRight",
                "InsideVertical", "InsideHorizontal", "DiagonalDown", "DiagonalUp"
        );

        String color = "#000000";
        List<String> styles = List.of(
                "Continuous", "None", "Continuous", "None",
                "None", "None", "None", "None"
        );
        String weight = "Thin";

        for(int i = 0; i < sideIndices.size(); i++) {
            WorkbookRangeBorder workbookRangeBorder = new WorkbookRangeBorder();
            workbookRangeBorder.sideIndex = sideIndices.get(i);
            workbookRangeBorder.id = sideIndices.get(i);
            workbookRangeBorder.color = color;
            workbookRangeBorder.style = styles.get(i);
            workbookRangeBorder.weight = weight;
            workbookRangeBorders.add(workbookRangeBorder);
        }
        return workbookRangeBorders;
    }

    public static List<WorkbookRangeBorder> getLeftTopRightBorders() {
        List<WorkbookRangeBorder> workbookRangeBorders = new ArrayList<>();
        List<String> sideIndices = List.of(
                "EdgeTop", "EdgeBottom", "EdgeLeft", "EdgeRight"
        );

        String color = "#000000";
        List<String> styles = List.of(
                "Continuous", "None", "Continuous", "Continuous"
        );
        String weight = "Thin";

        for(int i = 0; i < sideIndices.size(); i++) {
            WorkbookRangeBorder workbookRangeBorder = new WorkbookRangeBorder();
            workbookRangeBorder.sideIndex = sideIndices.get(i);
            workbookRangeBorder.id = sideIndices.get(i);
            workbookRangeBorder.color = color;
            workbookRangeBorder.style = styles.get(i);
            workbookRangeBorder.weight = weight;
            workbookRangeBorders.add(workbookRangeBorder);
        }
        return workbookRangeBorders;
    }

    public static List<WorkbookRangeBorder> getLeftBottomRightBorders() {
        List<WorkbookRangeBorder> workbookRangeBorders = new ArrayList<>();
        List<String> sideIndices = List.of(
                "EdgeTop", "EdgeBottom", "EdgeLeft", "EdgeRight"
        );

        String color = "#000000";
        List<String> styles = List.of(
                "None", "Continuous", "Continuous", "Continuous"
        );
        String weight = "Thin";

        for(int i = 0; i < sideIndices.size(); i++) {
            WorkbookRangeBorder workbookRangeBorder = new WorkbookRangeBorder();
            workbookRangeBorder.sideIndex = sideIndices.get(i);
            workbookRangeBorder.id = sideIndices.get(i);
            workbookRangeBorder.color = color;
            workbookRangeBorder.style = styles.get(i);
            workbookRangeBorder.weight = weight;
            workbookRangeBorders.add(workbookRangeBorder);
        }
        return workbookRangeBorders;
    }


}
