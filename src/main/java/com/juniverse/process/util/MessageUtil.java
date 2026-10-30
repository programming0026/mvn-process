package com.juniverse.process.util;

import javax.swing.*;

public final class MessageUtil {
    /**
     * Instantiates a new message util.
     */
    private MessageUtil() {

    }

    /**
     * Show error.
     *
     * @param msg the msg
     * @param title the title
     */
    public static void showError(String msg, String title) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show error.
     *
     * @param msg the msg
     * @param title the title
     * @param ex the ex
     */
    public static void showError(String msg, String title, Exception ex) {
        JOptionPane.showMessageDialog(null, msg + "\n" + ex.getMessage(),
                title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show error.
     *
     * @param msg the msg
     * @param ex the ex
     */
    public static void showError(String msg, Exception ex) {
        showError(msg, "Error", ex);
    }

    /**
     * Show info.
     *
     * @param msg the msg
     * @param title the title
     */
    public static void showInfo(String msg, String title) {
        JOptionPane.showMessageDialog(null, msg, title,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
