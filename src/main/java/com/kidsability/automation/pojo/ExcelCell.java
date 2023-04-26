package com.kidsability.automation.pojo;

import com.microsoft.graph.models.WorkbookRangeBorder;
import com.microsoft.graph.models.WorkbookRangeFill;
import com.microsoft.graph.models.WorkbookRangeFont;
import com.microsoft.graph.models.WorkbookRangeFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class ExcelCell {
    private String address;
    private WorkbookRangeFill fill;
    private WorkbookRangeFont font;
    private WorkbookRangeFormat format;
    private List<WorkbookRangeBorder> borders;
}
