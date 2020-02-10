package View;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the model containing data displayed in the JTable.
 */

public class ProgramTable extends AbstractTableModel {

    //list of table data
    private List<TableData> tableData;

    private String[] columnNames = {"Channel",
            "Start time",
            "End time",
            "Status"};


    /**
     * Constructor
     */
    public ProgramTable() {
        tableData = new ArrayList<>();

    }

    /**
     *
     * @return number of rows.
     */
    @Override
    public int getRowCount() {
        return tableData.size();
    }

    /**
     *
     * @return number of columns
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     *
     * @param column index of column
     * @return column name for a specific column
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     *
     * @param row row index
     * @param column column index
     * @return false to make each cell non-editable
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     *
     * @param rowIndex index of row
     * @param columnIndex index of column
     * @return value at a specific row and column index in the table.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        TableData data = tableData.get(rowIndex);

        switch (columnIndex) {

            case 0:
                return data.getTitle();

            case 1:
                return data.getStartTime();

            case 2:
                return data.getEndTime();

            case 3:
                return data.getStatus();

        }

        return null;
    }

    /**
     * Used to determine the default renderer/ editor for each cell in jtable
     * @param columnIndex column index
     * @return column class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    /**
     * This method adds table data to the table.
     * @param td table data object
     */
    public void addTableData(TableData td) {

        tableData.add(td);
        this.fireTableRowsInserted(tableData.size(), tableData.size());
    }

    /**
     * Removes a row from the table.
     * @param rowIndex the index of the row
     */
    public void removeRow(int rowIndex) {

        tableData.remove(rowIndex);
        this.fireTableRowsDeleted(tableData.size(), tableData.size());
    }
}
