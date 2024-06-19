package org.c4marathon.assignment.exception;

public class InsufficientBalanceException extends IllegalArgumentException{
    public InsufficientBalanceException() {
        super("잔액이 불충분합니다.");
    }
}
