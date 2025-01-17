package com.quantimodo.etl;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import java.io.IOException;
import com.almworks.sqlite4java.SQLiteException;

public class SQLiteReader implements Reader {
  static {
    // Turn off most logging.
    java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(java.util.logging.Level.SEVERE); 
    // Load library.
    try { SQLite.loadLibrary(); } catch (final SQLiteException e) {}
  }
  public static final SQLiteReader instance = new SQLiteReader();
  
  private static final String[]   EMPTY_STRING_ARRAY = new String[0];
  private static final Object[][] EMPTY_DATA_MATRIX  = new Object[0][];
  
  // Disable default constructor.
  private SQLiteReader() {}
  
  public DatabaseView getDatabaseView(final CharSequence filename) throws IOException {
    return getDatabaseView(new File(filename.toString()));
  }
  
  public DatabaseView getDatabaseView(final File file) throws IOException {
    final SQLiteConnection database = new SQLiteConnection(file);
    SQLiteStatement statement = null;
    try {
      database.openReadonly();
      
      final String[] tableNames;
      statement = database.prepare("SELECT name FROM sqlite_master WHERE type='table'");
      {
        final List<String> tableNameList = new ArrayList<String>();
        while (statement.step()) tableNameList.add(statement.columnString(0));
        statement.dispose();
        tableNames = tableNameList.toArray(EMPTY_STRING_ARRAY);
      }
      
      final ArrayTable[] tables = new ArrayTable[tableNames.length];
      for (int tableNumber = 0; tableNumber < tableNames.length; tableNumber++) {
        final String tableName = tableNames[tableNumber];
        statement = database.prepare("SELECT * FROM [" + tableName + "]");
        
        final int columnCount = statement.columnCount();
        final String[] columnNames = new String[columnCount];
        for (int columnNumber = 0; columnNumber < columnCount; columnNumber++) {
          columnNames[columnNumber] = statement.getColumnName(columnNumber);
        }
        
        final Object[][] tableData;
        {
          final List<Object[]> tableDataList = new ArrayList<Object[]>();
          int rowNumber = 0;
          while (statement.step()) {
            final Object[] rowData = new Object[columnCount];
            for (int columnNumber = 0; columnNumber < columnCount; columnNumber++) {
              rowData[columnNumber] = statement.columnValue(columnNumber);
            }
            tableDataList.add(rowData);
          }
          tableData = tableDataList.toArray(EMPTY_DATA_MATRIX);
        }
        tables[tableNumber] = new ArrayTable(tableName, columnNames, tableData);
        
        statement.dispose();
      }
      
      return new ArrayDatabaseView(tableNames, tables);
    } catch (final SQLiteException e) {
      throw new IOException(e);
    } finally {
      if (statement != null) statement.dispose();
      database.dispose();
    }
  }
}