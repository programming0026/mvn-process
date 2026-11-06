package com.juniverse.process.gui;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import oshi.software.os.OSProcess;

/**
 *
 * @author Java Universe
 */
public class ProcessTableModel extends AbstractTableModel {

    private static final String[] COLS = {
        "Process", "PID", "Memory", "Cpu", "User", "Base Priority", "Number of Threads", "Path"
    };

    private transient List<OSProcess> data;

    /**
     * Crea el modelo con la lista de procesos.
     * @param data procesos a mostrar
     */
    public ProcessTableModel(List<OSProcess> data) {
        this.data = data;
    }

    /**
     * Reemplaza los procesos y notifica a la tabla.
     * @param data nueva lista
     */
    public void setData(List<OSProcess> data) {
        this.data = data;
        fireTableDataChanged();
    }

    /** Cantidad de procesos. */
    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    /** Cantidad de columnas. */
    @Override
    public int getColumnCount() {
        return COLS.length;
    }

    /** Nombre de una columna. */
    @Override
    public String getColumnName(int column) {
        return COLS[column];
    }

    /**
     * Valor de una celda: número (PID, bytes de memoria, % de CPU) o texto.
     * @param row fila del modelo
     * @param col columna
     */
    @Override
    public Object getValueAt(int row, int col) {
        OSProcess p = data.get(row);

        return switch (col) {
            case 0 ->
                p.getName();
            case 1 ->
                p.getProcessID();
            case 2 ->
                p.getResidentSetSize();
            case 3 ->
                p.getProcessCpuLoadCumulative() * 100;
            case 4 ->
                p.getUser();
            case 5 ->
                p.getPriority();
            case 6 ->
                p.getThreadCount();
            case 7 ->
                p.getPath();
            default ->
                "";
        };
    }

    /**
     * Devuelve el proceso de una fila del modelo.
     *
     * @param modelRow índice en el modelo (no en la vista: con la tabla ordenada difieren)
     * @return el proceso
     */
    public OSProcess getProcessAt(int modelRow) {
        return data.get(modelRow);
    }

    /**
     * Valor de una celda con el formato que ve el usuario (PID en hexadecimal, memoria en MB, CPU en %).
     *
     * @param row fila del modelo
     * @param col columna
     * @return texto de la celda
     */
    public String getFormattedValueAt(int row, int col) {
        return format(col, getValueAt(row, col));
    }

    /**
     * Da formato de presentación a un valor de la columna indicada.
     *
     * @param col columna
     * @param value valor devuelto por {@link #getValueAt(int, int)}
     * @return texto formateado
     */
    public static String format(int col, Object value) {
        if (value == null) {
            return "";
        }
        return switch (col) {
            case 1 ->
                Integer.toHexString((Integer) value).toUpperCase();
            case 2 ->
                String.format("%.2f MB", (Long) value / (1024.0 * 1024.0));
            case 3 ->
                String.format("%.2f %%", (Double) value);
            default ->
                value.toString();
        };
    }

    /**
     * Tipo de cada columna: PID y prioridad enteros, memoria long, CPU double y el resto texto
     * (así la tabla los ordena numéricamente).
     * @param col columna
     */
    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 1, 5, 6 ->
                Integer.class;
            case 2 ->
                Long.class;
            case 3 ->
                Double.class;
            default ->
                String.class;
        };
    }
}
