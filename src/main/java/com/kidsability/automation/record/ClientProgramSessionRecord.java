package com.kidsability.automation.record;

import com.kidsability.automation.model.ClientProgramSession;
import com.kidsability.automation.model.ClientProgramSessionColdProbeRecord;
import com.kidsability.automation.model.ClientProgramSessionMassTrialRecord;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

public record ClientProgramSessionRecord(Long id, LocalDate date, Boolean isActive, List<ClientProgramSessionColdProbeRecord> clientProgramSessionColdProbeRecords,
                                         List<ClientProgramSessionMassTrialRecord> clientProgramSessionMassTrialRecords, List<String> massTrialMasteredTargets) {

    @Builder
    public ClientProgramSessionRecord{}

}
