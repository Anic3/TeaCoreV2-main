package me.tuanvo0022.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class NumberUtil {
    public static String formatAmount(double amount) {
        if (Double.isNaN(amount) || amount < 0) {
            return "N/A";
        }

        if (amount < 100_000) {
            return String.format("%.0f", amount);
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

        return formatter.format(amount);
    }
    
    public static String formatShort(double amount) {
        if (amount >= 1_000_000_000_000.0) {
            return String.format("%.2fT", amount / 1_000_000_000_000.0);
        } else if (amount >= 1_000_000_000.0) {
            return String.format("%.2fB", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000.0) {
            return String.format("%.2fM", amount / 1_000_000.0);
        } else if (amount >= 1_000.0) {
            return String.format("%.2fK", amount / 1_000.0);
        } else {
            return String.format("%.0f", amount);
        }
    }
    
    public static double parseAmount(String input) throws NumberFormatException {
        input = input.toLowerCase();
        char suffix = input.charAt(input.length() - 1);
        String numberPart = input.substring(0, input.length() - 1);
        double amount;
        switch(suffix) {
            case 'b':
                amount = Double.parseDouble(numberPart) * 1.0E9D;
                break;
            case 'k':
                amount = Double.parseDouble(numberPart) * 1000.0D;
                break;
            case 'm':
                amount = Double.parseDouble(numberPart) * 1000000.0D;
                break;
            case 't':
                amount = Double.parseDouble(numberPart) * 1.0E12D;
                break;
            default:
                amount = Double.parseDouble(input);
        }
        return amount;
    }

    public static String formatDuration(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0)
            return days + "d " + hours + "h";
        if (hours > 0)
            return hours + "h " + minutes + "m";
        if (minutes > 0)
            return minutes + "m";
        return seconds + "s";
    }
}