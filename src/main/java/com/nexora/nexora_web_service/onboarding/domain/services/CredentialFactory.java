package com.nexora.nexora_web_service.onboarding.domain.services;

import java.security.SecureRandom;

/**
 * Building-scoped credential rules. The per-building letter is what makes
 * identities unique across contracts (so onboarding only ADDS users, never
 * overwrites): resident1_a, resident1_b … never collide.
 */
public final class CredentialFactory {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DOMAIN = "@nexbell.app";

    private CredentialFactory() {}

    /** 1 → "a", 26 → "z", 27 → "aa" … (spreadsheet-style, derived from the building id). */
    public static String buildingLetter(long buildingId) {
        StringBuilder sb = new StringBuilder();
        long n = buildingId;
        while (n > 0) {
            long r = (n - 1) % 26;
            sb.insert(0, (char) ('a' + r));
            n = (n - 1) / 26;
        }
        return sb.length() == 0 ? "a" : sb.toString();
    }

    public static String residentEmail(int n, String letter) {
        return "resident" + n + "_" + letter + DOMAIN;
    }

    public static String doormanEmail(int n, String letter) {
        return "doorman" + n + "_" + letter + DOMAIN;
    }

    public static String residentPassword() {
        return "resident!" + twoDigits();
    }

    public static String doormanPassword() {
        return "doorman!" + twoDigits();
    }

    /** Exactly two random digits — the only random part of the temp password. */
    private static String twoDigits() {
        return String.format("%02d", RANDOM.nextInt(100));
    }
}
