package com.kidsability.automation.context;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SharePointContext {
    @Value("${share_point_ctx.site_id}")
    private String siteId;
}
