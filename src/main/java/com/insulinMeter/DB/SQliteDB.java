package com.insulinMeter.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.insulinMeter.Patient.InsulinDataModel;

/**
 * 
 * @author bhk code to create a sqlitedb
 */
public class SQliteDB {

	public enum QueryTyp {
		createInsulin_glucoseDataTable, createInectionTimingTable, insertIntoTable, updateTable, delete, query
	}

	public enum TableNames {
		InsulinData, InjectionTimings,
	}

	private static SQliteDB singletonInstance;

	private SQliteDB() {

	}

	public static SQliteDB getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new SQliteDB();
		}
		return singletonInstance;
	}

	public ArrayList<InsulinDataModel> exceuteQuery(HashMap<String, String> queryWithParms, QueryTyp operation,
			TableNames table, String whereClaus) {
		Connection c = null;
		Statement stmt = null;
		ResultSet result = null;
		ArrayList<InsulinDataModel> data = new ArrayList<>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:sample.db");
			// System.out.println("Opened database successfully");

			// System.out.println("Opene database successfully");
			HashMap<String, String> hashdelte = new HashMap<>();
			hashdelte.put("Meal", "changed");
			hashdelte.put("Time", "changed");
			hashdelte.put("Quantity", "changed");
			String sql = getQuery(operation, queryWithParms, table, whereClaus);
			stmt = c.createStatement();
			try {
				result = stmt.executeQuery(sql);
				// System.out.println(result.getFetchSize()+"---------------"+result.next());
				while (result.next()) {
					// System.out.println(result.);
					InsulinDataModel model = new InsulinDataModel();
					model.setGLucoseINjected(result.getDouble("GLucoseINjected"));
					model.setInsulinInjected(result.getDouble("InsulinInjected"));
					model.setTimestamp(result.getLong("SYSDATE"));
					model.setSugarLevel(result.getDouble("GLUCAGONLEVEL"));
					data.add(model);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		return data;
	}

	public InsulinDataModel getLastGlucoseInjectedTime() {
		HashMap<String, String> hashdelte = new HashMap<>();
		hashdelte.put("SYSDATE", ((Long) System.currentTimeMillis()).toString());
		hashdelte.put("GLUCAGONLEVEL", "");
		hashdelte.put("InsulinInjected", "");
		hashdelte.put("GLucoseINjected", "0");
		ResultSet result = null;
		InsulinDataModel glucogonInjetced = null;
		String query = getQuery(QueryTyp.query, hashdelte, TableNames.InsulinData,
				"GLucoseINjected > 0 and SYSDATE >" + (System.currentTimeMillis() - 20 * 60 * 1000));
		ArrayList<InsulinDataModel> listOfLastGlucogonInjections = new ArrayList<>();
		try {
			Class.forName("org.sqlite.JDBC");
			Connection c = DriverManager.getConnection("jdbc:sqlite:sample.db");
			// System.out.println("Opened database successfully");
			Statement stmt = c.createStatement();
			try {
				result = stmt.executeQuery(query);
				// System.out.println(result.getFetchSize()+"---------------"+result.next());
				// result.afterLast();
				while (result.next()) {
					InsulinDataModel data = new InsulinDataModel();
					data.setTimestamp(result.getLong("SYSDATE"));
					listOfLastGlucogonInjections.add(data);

				}
				if (listOfLastGlucogonInjections.size() > 0) {
					// result.previous();
					glucogonInjetced = listOfLastGlucogonInjections.get(listOfLastGlucogonInjections.size() - 1);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		return glucogonInjetced;

	}

	public void createTable(TableNames table) {
		Connection c = null;
		Statement stmt = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:sample.db");
			String sql = getQuery(QueryTyp.createInsulin_glucoseDataTable, null, table, null);
			stmt = c.createStatement();
			stmt.executeUpdate(sql);

			stmt.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/***
	 * 
	 * @param query
	 *            type of query to perform
	 * @param columnNames
	 *            names of the columns
	 * @param tableName
	 * @param whereclause
	 * @return
	 */
	static String getQuery(QueryTyp query, HashMap<String, String> columnNames, TableNames tableName,
			String whereclause) {
		// TODO create delete
		// all the insert queries are handled BY modifying the case statements
		// and enum .only modify if you have to create
		// a new table

		// builders for mutable strings
		StringBuilder sqlStatmentBuilder = new StringBuilder();
		StringBuilder columns = new StringBuilder();
		StringBuilder value = new StringBuilder();
		switch (query) {
		case createInectionTimingTable:
			return "Create Table if not exists " + tableName
					+ "(ROWID  INTEGER PRIMARY KEY AUTOINCREMENT , Meal String, Time  String, Quantity NUMERIC)";

		case createInsulin_glucoseDataTable:
			return "Create Table if not exists " + TableNames.InsulinData
					+ "( ROWID  INTEGER PRIMARY KEY  AUTOINCREMENT," + " SYSDATE  DOUBLE,"
					+ " GLUCAGONLEVEL DOUBLE, InsulinInjected DOUBLE , GLucoseINjected DOUBLE)";

		// insert queries dynamically
		case insertIntoTable:
			// clearing the builder from any junks
			sqlStatmentBuilder.setLength(0);
			columns.setLength(0);
			value.setLength(0);
			if (columnNames.isEmpty() || columnNames == null) {
				return null;
			}
			sqlStatmentBuilder.append("INSERT INTO " + tableName + " (");
			for (String columnName : columnNames.keySet()) {
				columns.append(columnName + ",");
				value.append("'" + columnNames.get(columnName) + "', ");

			}
			// deletin the last comma
			columns.deleteCharAt(columns.lastIndexOf(","));
			value.deleteCharAt(value.lastIndexOf(","));
			columns.append(")");
			value.append(")");
			sqlStatmentBuilder.append(columns.toString() + " Values (" + value.toString());
			String completedSqlInsertStatement = sqlStatmentBuilder.toString();

			return completedSqlInsertStatement;// "INSERT INTO
												// InjectionTimings(Quantity )
												// values ('1' )";
		// break;
		case updateTable:
			// clearing the builder from any junks
			sqlStatmentBuilder.setLength(0);
			columns.setLength(0);
			value.setLength(0);
			// checking the where condition is not null
			if (whereclause == null || whereclause.length() == 0) {
				System.err.println("plz update where clause");
				return null;
			}

			sqlStatmentBuilder.append("UPDATE  " + TableNames.InjectionTimings + " SET ");
			for (String columnName : columnNames.keySet()) {
				sqlStatmentBuilder.append(columnName + " = '" + columnNames.get(columnName) + "',");

			}
			// deleting the last comma
			sqlStatmentBuilder.deleteCharAt(sqlStatmentBuilder.lastIndexOf(","));

			sqlStatmentBuilder.append(" WHERE " + whereclause);
			String completedSqlUpdateStatement = sqlStatmentBuilder.toString();

			return completedSqlUpdateStatement;
		case delete:
			// checking the where condition is not null
			if (whereclause == null || whereclause.length() == 0) {
				System.err.println("plz update where clause");
				return null;
			}
			return "DELETE FROM " + tableName + " WHERE " + whereclause;
		case query:
			if (whereclause == null || whereclause.length() == 0) {
				// System.err.println("plz update where clause");
				// return null;
			}
			sqlStatmentBuilder.append("SELECT ");
			if (columnNames.keySet().size() < 1) {
				sqlStatmentBuilder.append("*");
			} else {
				for (String columnName : columnNames.keySet()) {
					sqlStatmentBuilder.append(columnName + ",");

				}
			}
			// deletin the last comma
			sqlStatmentBuilder.deleteCharAt(sqlStatmentBuilder.lastIndexOf(","));
			sqlStatmentBuilder.append(" FROM ");
			sqlStatmentBuilder.append(tableName);
			if (whereclause != null) {
				sqlStatmentBuilder.append(" WHERE " + whereclause);
			}

			return sqlStatmentBuilder.toString();
		default:
			break;
		}
		return null;
	}

}
