package net.supervision.superviseurapp.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.supervision.superviseurapp.entities.ServiceStatus;
import net.supervision.superviseurapp.repositories.ServiceStatusRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ServiceStatusExportController {

    private final ServiceStatusRepository serviceStatusRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/ms")
    public void exportMicroservicesToExcel(HttpServletResponse response,
                                           @RequestParam(required = false) String ip,
                                           @RequestParam(required = false) String serviceName,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws IOException {

        // Set response headers for Excel download
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"microservices-report.xlsx\"");

        // Get data based on parameters
        List<ServiceStatus> results = getFilteredResults(serviceName, start, end);

        // Create Excel workbook
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Microservices Report");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(generateReportTitle(serviceName, start, end));
            titleCell.setCellStyle(createTitleStyle(workbook));

            // Merge title cells (across all columns)
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // Create header row
            Row headerRow = sheet.createRow(2);
            String[] headers = {"Service Name", "IP Address", "Port", "Status", "Version", "Tested At"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 3;
            for (ServiceStatus status : results) {
                Row row = sheet.createRow(rowNum++);

                // Service Name
                Cell serviceNameCell = row.createCell(0);
                serviceNameCell.setCellValue(status.getServiceName() != null ? status.getServiceName() : "N/A");
                serviceNameCell.setCellStyle(dataStyle);

                // IP Address
                Cell ipCell = row.createCell(1);
                ipCell.setCellValue(status.getIpAddress());
                ipCell.setCellStyle(dataStyle);

                // Port
                Cell portCell = row.createCell(2);
                portCell.setCellValue(status.getPort());
                portCell.setCellStyle(dataStyle);

                // Status (with color coding)
                Cell statusCell = row.createCell(3);
                statusCell.setCellValue(status.getStatus());
                statusCell.setCellStyle(getStatusStyle(workbook, status.getStatus()));

                // Version
                Cell versionCell = row.createCell(4);
                versionCell.setCellValue(status.getVersion() != null ? status.getVersion() : "N/A");
                versionCell.setCellStyle(dataStyle);

                // Tested At
                Cell dateCell = row.createCell(5);
                dateCell.setCellValue(status.getTestedAt().format(DATE_FORMATTER));
                dateCell.setCellStyle(dateStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to response
            workbook.write(response.getOutputStream());
        }
    }

    private List<ServiceStatus> getFilteredResults(String serviceName, LocalDateTime start, LocalDateTime end) {
        if (serviceName != null && start != null && end != null) {
            return serviceStatusRepository.findByServiceNameAndTestedAtBetween(serviceName, start, end);
        } else if (start != null && end != null) {
            return serviceStatusRepository.findByTestedAtBetween(start, end);
        } else {
            return serviceStatusRepository.findAll(); // fallback
        }
    }

    private String generateReportTitle(String serviceName, LocalDateTime start, LocalDateTime end) {
        StringBuilder title = new StringBuilder("Microservices Status Report");

        if (serviceName != null && start != null && end != null) {
            title.append(" - ").append(serviceName)
                    .append(" (").append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append(" to ").append(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(")");
        } else if (start != null && end != null) {
            title.append(" - From ").append(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .append(" to ").append(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } else {
            title.append(" - All Records (Generated on ")
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(")");
        }

        return title.toString();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle getStatusStyle(Workbook workbook, String status) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);

        switch (status.toUpperCase()) {
            case "UP":
                font.setColor(IndexedColors.DARK_GREEN.getIndex());
                style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "DOWN":
                font.setColor(IndexedColors.DARK_RED.getIndex());
                style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
            case "NO_HEALTH_ENDPOINT":
                font.setColor(IndexedColors.DARK_YELLOW.getIndex());
                style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                break;
        }

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }


    
}