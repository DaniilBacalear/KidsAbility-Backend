package com.kidsability.automation.record;

import lombok.Builder;

import java.time.LocalDate;

public record ProgramRecord(Long id, String name, LocalDate startDate, Boolean isMastered, String embeddableProgramTemplateLink, Double progress) {
    @Builder
    public ProgramRecord{}
}
