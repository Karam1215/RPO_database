package com.karam;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InsertSouvenirs {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/metology_store";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Karam";

    // Column indexes
    private static final int ID_COLUMN = 0;
    private static final int URL_COLUMN = 1;
    private static final int SHORT_NAME_COLUMN = 3;
    private static final int NAME_COLUMN = 4;
    private static final int DESCRIPTION_COLUMN = 7;
    private static final int ID_CATEGORY_COLUMN = 8;
    private static final int PRICE_COLUMN = 6;
    private static final int RATING_COLUMN = 9;
    private static final int DEALER_PRICE_COLUMN = 10;
    private static final int WEIGHT_COLUMN = 11;
    private static final int SIZE_COLUMN = 14;
    private static final int ID_MATERIAL_COLUMN = 13;
    private static final int Q_TYPICS_COLUMN = 15;
    private static final int PICS_SIZE_COLUMN = 16;
    private static final int ID_APPLIC_METHOD_COLUMN = 17;
    private static final int ID_COLOR_COLUMN = 12;
    private static final int ALL_CATEGORY_COLUMN = 18;

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            insertSouvenirs(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertSouvenirs(Connection connection) throws Exception {
        Map<String, Integer> materialIdMap = getMaterialIdMap(connection);
        Map<String, Integer> applicationMethodIdMap = getApplicationMethodIdMap(connection);
        Map<String, Integer> colorIdMap = getColorIdMap(connection);
        int invalidCount = 0;

        try (FileInputStream fis = new FileInputStream("data.xlsx");
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;
            String insertSouvenirSQL = "INSERT INTO Souvenirs (id, url, shortname, name, description, rating, " +
                    "idcategory, idcolor, size, idmaterial, weight, qtypics, picssize, idapplicmetod, " +
                    "dealerprice, price, AllCategories) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSouvenirSQL)) {
                for (Row row : sheet) {
                    if (isFirstRow) {
                        isFirstRow = false;
                        continue;
                    }

                    long id = getCellValueAsLong(row.getCell(ID_COLUMN));
                    String url = getCellValue(row.getCell(URL_COLUMN));
                    String shortName = getCellValue(row.getCell(SHORT_NAME_COLUMN));
                    String name = getCellValue(row.getCell(NAME_COLUMN));
                    String description = getCellValue(row.getCell(DESCRIPTION_COLUMN));
                    int idCategory = getCellValueAsInt(row.getCell(ID_CATEGORY_COLUMN));
                    double price = getCellValueAsDouble(row.getCell(PRICE_COLUMN));
                    int rating = getCellValueAsInt(row.getCell(RATING_COLUMN));
                    double dealerPrice = getCellValueAsDouble(row.getCell(DEALER_PRICE_COLUMN));
                    double weight = getCellValueAsDouble(row.getCell(WEIGHT_COLUMN));
                    String size = getCellValue(row.getCell(SIZE_COLUMN));
                    int idMaterial = materialIdMap.getOrDefault(getCellValue(row.getCell(ID_MATERIAL_COLUMN)), -1);
                    String qTypics = getCellValue(row.getCell(Q_TYPICS_COLUMN));
                    String picsSize = getCellValue(row.getCell(PICS_SIZE_COLUMN));
                    int idApplicMetod = applicationMethodIdMap.getOrDefault(getCellValue(row.getCell(ID_APPLIC_METHOD_COLUMN)), -1);
                    int idColor = colorIdMap.getOrDefault(getCellValue(row.getCell(ID_COLOR_COLUMN)), -1);
                    String allCategory = getCellValue(row.getCell(ALL_CATEGORY_COLUMN));
                    if (idColor == -1) {
                        System.out.println("Skipping Souvenir due to invalid color for: " + name + " " + idColor +" " + id);
                        continue;
                    }
                    if (isValidSouvenir(id, url, name, description, idCategory, idMaterial, idApplicMetod)) {
                        preparedStatement.setLong(1, id);
                        preparedStatement.setString(2, url);
                        preparedStatement.setString(3, shortName);
                        preparedStatement.setString(4, name);
                        preparedStatement.setString(5, description);
                        preparedStatement.setInt(6, rating);
                        preparedStatement.setInt(7, idCategory);
                        preparedStatement.setInt(8, idColor);
                        preparedStatement.setString(9, size);
                        preparedStatement.setInt(10, idMaterial);
                        preparedStatement.setDouble(11, weight);
                        preparedStatement.setString(12, qTypics);
                        preparedStatement.setString(13, picsSize);
                        preparedStatement.setInt(14, idApplicMetod);
                        preparedStatement.setDouble(15, dealerPrice);
                        preparedStatement.setDouble(16, price);
                        preparedStatement.setString(17, allCategory);
                        preparedStatement.addBatch();
                    } else {
                        System.out.println("Missing required data for Souvenir: "  + " " + id + " " + shortName + " " + name + " " + description + " " + idCategory + " " + idMaterial + " " + idApplicMetod + " " + idColor);
                        invalidCount++;
                    }
                }
                preparedStatement.executeBatch();
                System.out.println("Total souvenirs skipped due to missing required data: " + invalidCount);

            }
        }
    }

    private static Map<String, Integer> getMaterialIdMap(Connection connection) throws Exception {
        return getIdMap(connection, "SELECT id, name FROM souvenirmaterials");
    }

    private static Map<String, Integer> getApplicationMethodIdMap(Connection connection) throws Exception {
        return getIdMap(connection, "SELECT id, name FROM ApplicationMetods");
    }

    private static Map<String, Integer> getColorIdMap(Connection connection) throws Exception {
        return getIdMap(connection, "SELECT id, name FROM Colors");
    }

    private static Map<String, Integer> getIdMap(Connection connection, String sql) throws Exception {
        Map<String, Integer> idMap = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                idMap.put(rs.getString("name"), rs.getInt("id"));
            }
        }
        return idMap;
    }

    private static boolean isValidSouvenir(long id, String url, String name, String description, int idCategory, int idMaterial, int idApplicMetod) {
        return id > 0 && url != null && !url.trim().isEmpty() &&
                name != null && !name.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                idCategory != 0 && idMaterial != -1 && idApplicMetod != -1;
    }

    private static String getCellValue(Cell cell) {
        return cell != null ? cell.getStringCellValue() : null;
    }

    private static int getCellValueAsInt(Cell cell) {
        if (cell != null) {
            try {
                switch (cell.getCellType()) {
                    case NUMERIC:
                        return (int) cell.getNumericCellValue();
                    case STRING:
                        return Integer.parseInt(cell.getStringCellValue());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer value: " + (cell.getStringCellValue() != null ? cell.getStringCellValue() : "null"));
            }
        }
        return 0;
    }

    private static long getCellValueAsLong(Cell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    try {
                        return Long.parseLong(cell.getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid long value: " + cell.getStringCellValue());
                    }
            }
        }
        return 0;
    }

    private static double getCellValueAsDouble(Cell cell) {
        if (cell != null) {
            try {
                switch (cell.getCellType()) {
                    case NUMERIC:
                        return cell.getNumericCellValue();
                    case STRING:
                        String value = cell.getStringCellValue();
                        if (value.equalsIgnoreCase("nan")) {
                            return 0.0;
                        }
                        return Double.parseDouble(value);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid double value: " + (cell.getStringCellValue() != null ? cell.getStringCellValue() : "null"));
            }
        }
        return 0.0;
    }
}
