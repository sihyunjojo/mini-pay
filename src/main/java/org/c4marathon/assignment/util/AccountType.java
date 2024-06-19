package org.c4marathon.assignment.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class AccountType {
    public final static String MAIN = "MAIN";
    public final static String SAVINGS = "SAVINGS";
    public final static String FLEXIBLE_SAVINGS = "FLEXIBLE_SAVINGS";
    public final static String FLEXIBLE_SAVINGS_INTEREST_RATE = "0.03";
    public final static String RECURRING_SAVINGS = "RECURRING_SAVINGS";
    public final static String RECURRING_SAVINGS_INTEREST_RATE = "0.05";
    public final static BigDecimal DAILY_CHARGING_LIMIT =  new BigDecimal("3000000");
    public final static BigDecimal RECHARGE_INCREMENT =  new BigDecimal("10000");


    public final static long ADMIN_ACCOUNT_ID = 1L;
}
