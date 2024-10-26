package com.karam;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Insert_Materials_Colors_Method {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/metology_store";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Karam";

    public static void main(String[] args) {
        Set<String> uniqueColors = new HashSet<>();
        Set<String> uniqueApplicationMethods = new HashSet<>();
        Set<String> uniqueMaterials = new HashSet<>();

        try (FileInputStream fis = new FileInputStream("data.xlsx");
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell colorCell = row.getCell(12);
                Cell applicMethodCell = row.getCell(17);
                Cell materialCell = row.getCell(13);

                if (colorCell != null) {
                    String color = colorCell.getStringCellValue().trim();
                    if (!color.equals("nan") && !color.isEmpty()) {
                        uniqueColors.add(color);
                    }
                }

                if (applicMethodCell != null) {
                    String applicMethod = applicMethodCell.getStringCellValue().trim();
                    if (!applicMethod.equals("nan") && !applicMethod.isEmpty()) {
                        uniqueApplicationMethods.add(applicMethod);
                    }
                }

                if (materialCell != null) {
                    String material = materialCell.getStringCellValue().trim();
                    if (!material.equals("nan") && !material.isEmpty()) {
                        uniqueMaterials.add(material);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            for (String color : uniqueColors) {
                insertColor(color, connection);
            }

            for (String applicMethod : uniqueApplicationMethods) {
                insertApplicationMethod(applicMethod, connection);
            }

            for (String material : uniqueMaterials) {
                insertMaterial(material, connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertColor(String colorName, Connection connection) throws Exception {
        String sql = "INSERT INTO Colors (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, colorName);
            preparedStatement.executeUpdate();
        }
    }

    private static void insertApplicationMethod(String methodName, Connection connection) throws Exception {
        String sql = "INSERT INTO applicationmetods (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, methodName);
            preparedStatement.executeUpdate();
        }
    }

    private static void insertMaterial(String materialName, Connection connection) throws Exception {
        String sql = "INSERT INTO souvenirmaterials (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, materialName);
            preparedStatement.executeUpdate();
        }
    }
}
