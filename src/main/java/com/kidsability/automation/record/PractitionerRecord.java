package com.kidsability.automation.record;

import lombok.Builder;

public record PractitionerRecord(String firstName, String lastName, Boolean isAdmin, String email) {
    @Builder
    public PractitionerRecord{}
}
