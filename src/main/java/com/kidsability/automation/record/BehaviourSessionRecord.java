package com.kidsability.automation.record;

import com.kidsability.automation.model.BehaviourSession;
import lombok.Builder;

public record BehaviourSessionRecord(BehaviourSession behaviourSession) {

    @Builder
    public BehaviourSessionRecord{}
}
