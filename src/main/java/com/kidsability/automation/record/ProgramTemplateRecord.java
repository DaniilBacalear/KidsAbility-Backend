package com.kidsability.automation.record;

import lombok.Builder;

public record ProgramTemplateRecord(String name, String embeddableLink) {
    @Builder
    public ProgramTemplateRecord{}

}
