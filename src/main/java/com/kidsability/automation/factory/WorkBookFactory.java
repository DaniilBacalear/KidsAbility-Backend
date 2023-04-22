package com.kidsability.automation.factory;

import com.microsoft.graph.models.WorkbookRangeFill;
import com.microsoft.graph.models.WorkbookRangeFont;
import com.microsoft.graph.models.WorkbookRangeFormat;

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


}
