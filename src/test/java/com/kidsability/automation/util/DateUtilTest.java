package com.kidsability.automation.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
class DateUtilTest {
    @Test
    void givenLocalDate_returnFormattedString() {
        var localDate = LocalDate.of(2022,11,7);
        assertEquals("11/7/2022", DateUtil.getMDY(localDate));
    }

}