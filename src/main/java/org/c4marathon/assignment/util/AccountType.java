package org.c4marathon.assignment.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountType {
    public static final String MAIN = "MAIN";
    public static final String SAVINGS = "SAVINGS";
    public static final String FLEXIBLE_SAVINGS = "FLEXIBLE_SAVINGS";
    public static final String FLEXIBLE_SAVINGS_INTEREST_RATE = "0.03";
    public static final String RECURRING_SAVINGS = "RECURRING_SAVINGS";
    public static final String RECURRING_SAVINGS_INTEREST_RATE = "0.05";
    public static final BigDecimal DAILY_CHARGING_LIMIT =  new BigDecimal("3000000");
    public static final BigDecimal RECHARGE_INCREMENT =  new BigDecimal("10000");

    public static final String RANDOM_CALCULATE= "RANDOM_CALCULATE";
    public static final String EQUALLY_CALCULATE= "EQUALLY_CALCULATE";

    public static final LocalDateTime REMIND_STANDARD_TIME = LocalDateTime.now().plusHours(24);

    public static final long ADMIN_ACCOUNT_ID = 1L;

}
