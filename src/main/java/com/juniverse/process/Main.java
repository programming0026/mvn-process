package com.juniverse.process;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.juniverse.process.gui.ProcessFrame;

import javax.swing.SwingUtilities;

/**
 * Visor de procesos del sistema operativo con interfaz Swing y tema oscuro FlatLaf.
 *
 * @author Java Universe
 */
public class Main {

    
    
    /**
     * Aplica el tema oscuro FlatLaf y abre la ventana principal.
     * @param args no se usan
     */
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(ProcessFrame::new);
    }
}
