package com.juniverse.process.gui;

import com.juniverse.process.util.IconUtil;
import com.juniverse.process.util.MessageUtil;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ajuar
 */
public class ProcessFrame extends JFrame {
    private static final Logger log
            = Logger.getLogger(ProcessFrame.class.getName());

    private final transient OperatingSystem os;
    private final ProcessTableModel model;

    private final JTable table;

    private Timer updateTimer;

    private static final String TITLE = "process";

    private static final String TITLE_ERROR = "Error";

    /** Crea la ventana con la barra de herramientas y la tabla de procesos, y activa la actualización cada 5 segundos. */
    public ProcessFrame() {

        os = new SystemInfo().getOperatingSystem();
        model = new ProcessTableModel(fetchProcesses());

        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("Refresh", IconUtil.loadIcon("/icons/refresh.png", 32, 32));
        JButton killButton = new JButton("End Process", IconUtil.loadIcon("/icons/end.png", 32, 32));
        JButton exportButton = new JButton("Export", IconUtil.loadIcon("/icons/export.png", 32, 32));
        JButton aboutButton = new JButton("About", IconUtil.loadIcon("/icons/about.png", 32, 32));

        toolBar.add(refreshButton);
        toolBar.add(killButton);
        toolBar.add(exportButton);
        toolBar.add(aboutButton);

        refreshButton.addActionListener(e -> loadProcesses());
        killButton.addActionListener(e -> killSelectedProcess());
        exportButton.addActionListener(e -> exportProcesses());
        aboutButton.addActionListener(e -> about());

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        for (int col = 1; col <= 3; col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(new FormattedCellRenderer(col));
        }

        startAutoUpdate();

        setTitle(TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setSize(800, 600);
        ImageIcon icon = IconUtil.loadIcon("/icons/main.png", 36, 36);
        if (icon != null) {
            setIconImage(icon.getImage());
        }

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopAutoUpdate();
            }
        });

    }

    /** Guarda la lista de procesos en un archivo de texto elegido por el usuario. */
    private void exportProcesses() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save as");

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(file)) {
                for (int i = 0; i < model.getRowCount(); i++) {

                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.printf("%s: %s%n",
                                model.getColumnName(j),
                                model.getFormattedValueAt(i, j));
                    }

                    writer.println();
                }
                MessageUtil.showInfo("Export completed.", TITLE);
            } catch (Exception ex) {
                MessageUtil.showError("Export error: " + ex.getMessage(), TITLE_ERROR);
            }
        }
    }

    /** Recarga la lista de procesos. */
    private void loadProcesses() {
        refreshAsync();
    }

    /** Termina el proceso seleccionado, previa confirmación. */
    private void killSelectedProcess() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            MessageUtil.showError("Select a process to finish.", TITLE_ERROR);
            return;
        }
        // El índice de la vista no coincide con el del modelo cuando la tabla está ordenada
        OSProcess process = model.getProcessAt(table.convertRowIndexToModel(selectedRow));
        int confirm = JOptionPane.showConfirmDialog(this,
                "End process " + process.getName() + " (PID " + process.getProcessID() + ")?",
                TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }


        boolean result = killProcess(process);
        if (result) {
            MessageUtil.showInfo("Process " + process.getName() + " completed.", TITLE);
            loadProcesses();
        } else {
            MessageUtil.showError("The process could not be completed.", TITLE_ERROR);
        }
    }

    /** Consulta al sistema operativo los procesos válidos, ordenados por nombre. */
    private List<OSProcess> fetchProcesses() {
        return os.getProcesses(OperatingSystem.ProcessFiltering.VALID_PROCESS,
                OperatingSystem.ProcessSorting.NAME_ASC, 0);
    }

    /**
     * Pide al sistema terminar el proceso (de forma normal si es posible; si no, forzada).
     * @param process proceso a terminar
     * @return {@code true} si el sistema aceptó la petición
     */
    private boolean killProcess(OSProcess process) {
        boolean result = false;
        Optional<ProcessHandle> optProcess = ProcessHandle.of(process.getProcessID());
        if (optProcess.isPresent()) {
            ProcessHandle ph = optProcess.get();
            log.log(Level.INFO, "Trying to end process: {0}", process.getName());

            if (ph.supportsNormalTermination()) {
                result = ph.destroy();
            } else {
                result = ph.destroyForcibly();
            }

            ph.onExit().thenRun(()
                    -> log.log(Level.INFO, "Proceso {0} terminado", process.getName())
            );
        }

        return result;
    }

    /** Inicia la actualización automática cada 5 segundos. */
    private void startAutoUpdate() {
        if (updateTimer != null && updateTimer.isRunning()) {
            return;
        }

        updateTimer = new Timer(5000, e -> updateCpuAndMemory());
        updateTimer.start();
    }

    /** Detiene la actualización automática. */
    private void stopAutoUpdate() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }

    /** Actualiza la tabla con los valores actuales de CPU y memoria. */
    private void updateCpuAndMemory() {
        refreshAsync();
    }

    /**
     * Consulta los procesos en un hilo de fondo y actualiza la tabla en el hilo de Swing,
     * conservando la selección (por PID, no por posición, porque el orden puede cambiar).
     */
    private void refreshAsync() {
        int selectedRow = table.getSelectedRow();
        final int selectedPid = selectedRow >= 0
                ? model.getProcessAt(table.convertRowIndexToModel(selectedRow)).getProcessID() : -1;
        new SwingWorker<List<OSProcess>, Void>() {
            @Override
            protected List<OSProcess> doInBackground() {
                return fetchProcesses();
            }

            @Override
            protected void done() {
                try {
                    model.setData(get());
                    for (int i = 0; selectedPid >= 0 && i < model.getRowCount(); i++) {
                        if (model.getProcessAt(i).getProcessID() == selectedPid) {
                            int viewRow = table.convertRowIndexToView(i);
                            table.setRowSelectionInterval(viewRow, viewRow);
                            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    log.log(Level.WARNING, "Could not refresh the processes: {0}", ex.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Muestra PID (hexadecimal), memoria (MB) y CPU (%) con formato, sin perder el orden numérico de la columna.
     */
    private static final class FormattedCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private final int column;

        FormattedCellRenderer(int column) {
            this.column = column;
            setHorizontalAlignment(RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            setText(ProcessTableModel.format(column, value));
        }
    }

    /**
     * Muestra el cuadro «Acerca de».
     */
    private void about() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.setVisible(true);
    }
}
