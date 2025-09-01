package net.supervision.superviseurapp.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/export")
public class ServiceStatusExportController {

    @PostMapping("/displayed")
    public void exportDisplayedData(@RequestBody ExportRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=watchcat-history.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("History");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = request.getType().equals("machine") ?
                new String[]{"IP Address", "Port", "Status", "Tested At"} :
                new String[]{"Service Name", "IP Address", "Port", "Status", "Version", "Tested At"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Data rows
        int rowNum = 1;
        for (Map<String, Object> record : request.getData()) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            if (request.getType().equals("machine")) {
                row.createCell(col++).setCellValue((String) record.get("ipAddress"));
                row.createCell(col++).setCellValue(String.valueOf(record.get("port")));
                row.createCell(col++).setCellValue((String) record.get("status"));
                row.createCell(col++).setCellValue((String) record.get("testedAt"));
            } else {
                row.createCell(col++).setCellValue((String) record.get("serviceName"));
                row.createCell(col++).setCellValue((String) record.get("ipAddress"));
                row.createCell(col++).setCellValue(String.valueOf(record.get("port")));
                row.createCell(col++).setCellValue((String) record.get("status"));
                row.createCell(col++).setCellValue((String) record.get("version"));
                row.createCell(col++).setCellValue((String) record.get("testedAt"));
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Data
    public static class ExportRequest {
        private String type;
        private List<Map<String, Object>> data;
    }
}