package com.juniverse.process.util;

import javax.swing.*;

public final class MessageUtil {
    private MessageUtil() {

    }

    /**
     * Muestra un mensaje de error.
     *
     * @param msg mensaje
     * @param title título
     */
    public static void showError(String msg, String title) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje de error.
     *
     * @param msg mensaje
     * @param title título
     * @param ex excepción
     */
    public static void showError(String msg, String title, Exception ex) {
        JOptionPane.showMessageDialog(null, msg + "\n" + ex.getMessage(),
                title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje de error.
     *
     * @param msg mensaje
     * @param ex excepción
     */
    public static void showError(String msg, Exception ex) {
        showError(msg, "Error", ex);
    }

    /**
     * Muestra un mensaje informativo.
     *
     * @param msg mensaje
     * @param title título
     */
    public static void showInfo(String msg, String title) {
        JOptionPane.showMessageDialog(null, msg, title,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
