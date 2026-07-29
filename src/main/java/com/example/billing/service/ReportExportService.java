package com.example.billing.service;

import com.example.billing.dto.ReportSummaryDto;
import com.example.billing.model.ErrorLog;
import com.example.billing.model.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportExportService {

    private final ReportService reportService;

    public ReportExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] exportToPdf(String reportType, int month, int year) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Report: " + reportType + " (" + month + "/" + year + ")", titleFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = null;

            switch (reportType.toUpperCase()) {
                case "SUMMARY":
                    table = new PdfPTable(4);
                    table.addCell("Billing Period");
                    table.addCell("Successful Records");
                    table.addCell("Failed Records");
                    table.addCell("Status");

                    ReportSummaryDto summary = reportService.getSummary(month, year);
                    table.addCell(summary.billingPeriod());
                    table.addCell(String.valueOf(summary.successfulRecords()));
                    table.addCell(String.valueOf(summary.failedRecords()));
                    table.addCell(summary.status());
                    break;

                case "FAILED":
                    table = new PdfPTable(4);
                    table.addCell("Time");
                    table.addCell("Customer ID");
                    table.addCell("Severity");
                    table.addCell("Description");

                    List<ErrorLog> failed = reportService.getFailedRecords(month, year);
                    for (ErrorLog error : failed) {
                        table.addCell(error.getTimestamp().toString());
                        table.addCell(error.getCustomerId() != null ? error.getCustomerId() : "N/A");
                        table.addCell(error.getSeverity().name());
                        table.addCell(error.getDescription());
                    }
                    break;

                case "SUCCESSFUL":
                    table = new PdfPTable(4);
                    table.addCell("Invoice Number");
                    table.addCell("Customer Ref");
                    table.addCell("Date");
                    table.addCell("Total Amount");

                    List<Invoice> successful = reportService.getSuccessfulRecords(month, year);
                    for (Invoice inv : successful) {
                        table.addCell(inv.getNumber());
                        table.addCell(inv.getUser().getReference());
                        table.addCell(inv.getDateTime().toLocalDate().toString());
                        table.addCell(inv.getTotalAmount().toString());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Невалиден тип отчет: " + reportType);
            }

            table.setWidthPercentage(100);
            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Грешка при генериране на PDF: " + e.getMessage());
        }
    }

    public byte[] exportToExcel(String reportType, int month, int year) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(reportType);
            Row headerRow = sheet.createRow(0);
            int rowNum = 1;

            switch (reportType.toUpperCase()) {
                case "SUMMARY":
                    headerRow.createCell(0).setCellValue("Billing Period");
                    headerRow.createCell(1).setCellValue("Successful Records");
                    headerRow.createCell(2).setCellValue("Failed Records");
                    headerRow.createCell(3).setCellValue("Status");

                    ReportSummaryDto summary = reportService.getSummary(month, year);
                    Row dataRow = sheet.createRow(rowNum);
                    dataRow.createCell(0).setCellValue(summary.billingPeriod());
                    dataRow.createCell(1).setCellValue(summary.successfulRecords());
                    dataRow.createCell(2).setCellValue(summary.failedRecords());
                    dataRow.createCell(3).setCellValue(summary.status());
                    break;

                case "FAILED":
                    headerRow.createCell(0).setCellValue("Time");
                    headerRow.createCell(1).setCellValue("Customer ID");
                    headerRow.createCell(2).setCellValue("Severity");
                    headerRow.createCell(3).setCellValue("Description");

                    List<ErrorLog> failed = reportService.getFailedRecords(month, year);
                    for (ErrorLog error : failed) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(error.getTimestamp().toString());
                        row.createCell(1).setCellValue(error.getCustomerId() != null ? error.getCustomerId() : "N/A");
                        row.createCell(2).setCellValue(error.getSeverity().name());
                        row.createCell(3).setCellValue(error.getDescription());
                    }
                    break;

                case "SUCCESSFUL":
                    headerRow.createCell(0).setCellValue("Invoice Number");
                    headerRow.createCell(1).setCellValue("Customer Ref");
                    headerRow.createCell(2).setCellValue("Date");
                    headerRow.createCell(3).setCellValue("Total Amount");

                    List<Invoice> successful = reportService.getSuccessfulRecords(month, year);
                    for (Invoice inv : successful) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(inv.getNumber());
                        row.createCell(1).setCellValue(inv.getUser().getReference());
                        row.createCell(2).setCellValue(inv.getDateTime().toLocalDate().toString());
                        row.createCell(3).setCellValue(inv.getTotalAmount().doubleValue());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Невалиден тип отчет: " + reportType);
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Грешка при генериране на Excel: " + e.getMessage());
        }
    }
}