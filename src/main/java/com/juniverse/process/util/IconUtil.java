package com.juniverse.process.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IconUtil {
    /** The Constant log. */
    private static final Logger log
            = Logger.getLogger(IconUtil.class.getName());

    /**
     * Instantiates a new icon util.
     */
    private IconUtil() {

    }

    /**
     * Carga un icono desde el classpath (carpeta resources).
     *
     * @param path Ruta relativa desde resources
     * @return ImageIcon o null si no se encuentra
     */
    public static ImageIcon loadIcon(String path) {
        try {
            URL iconURL = IconUtil.class.getResource(path);
            if (iconURL != null) {
                return new ImageIcon(iconURL);
            } else {
                log.log(Level.WARNING, "The icon was not found: {0}", path);
                return null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error loading icon {0}: ", e.getMessage());
            return null;
        }
    }

    /**
     * Carga un icono y lo redimensiona.
     *
     * @param path Ruta del icono
     * @param width Ancho deseado
     * @param height Alto deseado
     * @return ImageIcon redimensionado o null
     */
    public static ImageIcon loadIcon(String path, int width, int height) {
        ImageIcon icon = loadIcon(path);
        if (icon != null) {
            return resizeIcon(icon, width, height);
        }
        return null;
    }

    /**
     * Redimensiona un ImageIcon.
     *
     * @param icon The icon
     * @param width The width
     * @param height The height
     * @return ImageIcon
     */
    public static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }
}

