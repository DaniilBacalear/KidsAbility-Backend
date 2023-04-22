package com.kidsability.automation.record;

import com.kidsability.automation.model.ClientProgramSession;
import com.kidsability.automation.model.ClientProgramSessionColdProbeRecord;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

public record ClientProgramSessionRecord(Long id, LocalDate date, Boolean isActive
        , List<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecords) {

    @Builder
    public ClientProgramSessionRecord{}

}
