package com.finflow.transaction.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * TXN-20260710-A7F3K2 — support xodimi telefonda o'qiy oladigan format.
 * Chalkashtiruvchi belgilar (0/O, 1/I) alfavitdan chiqarilgan.
 */
@Component
public class ReferenceGenerator {

    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int SUFFIX_LENGTH = 6;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return "TXN-" + LocalDate.now(ZoneOffset.UTC).format(DATE_FMT) + "-" + suffix;
    }
}
