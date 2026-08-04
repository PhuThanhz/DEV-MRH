package vn.system.app.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    /**
     * Builds an Excel workbook for Task Summary Report and returns byte array.
     */
    public static byte[] createExcelReport(
            String sheetName,
            List<String> headers,
            List<List<Object>> rows) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Báo cáo");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Data Cell Style
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Row 0: Headers
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIndex = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(20);
                for (int colIndex = 0; colIndex < rowData.size(); colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellStyle(cellStyle);

                    Object val = rowData.get(colIndex);
                    if (val == null) {
                        cell.setCellValue("");
                    } else if (val instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                    } else if (val instanceof Boolean b) {
                        cell.setCellValue(b ? "Đúng hạn" : "Trễ hạn");
                    } else if (val instanceof Instant inst) {
                        cell.setCellValue(DATE_FORMATTER.format(inst));
                    } else {
                        cell.setCellValue(val.toString());
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1000, 3500));
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
