package com.example.billing.service;

import com.example.billing.model.Invoice;
import com.example.billing.model.Line;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.awt.Color;

@Service
public class PdfGenerationService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();
            BaseFont baseFont = BaseFont.createFont("src/main/resources/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont baseFontBold = BaseFont.createFont("src/main/resources/fonts/arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font normalFont = new Font(baseFont, 10);
            Font headerFont = new Font(baseFontBold, 10);
            Font titleFont = new Font(baseFontBold, 24);

            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("Bill From:", headerFont));
            leftCell.addElement(new Paragraph("Methodia Inc.", normalFont));
            leftCell.addElement(new Paragraph("Sofia, 1000", normalFont));
            leftCell.addElement(new Paragraph("billing@methodia.com", normalFont));
            leftCell.addElement(Chunk.NEWLINE);
            leftCell.addElement(new Paragraph("Bill To:", headerFont));
            leftCell.addElement(new Paragraph(invoice.getUser().getName(), normalFont));
            leftCell.addElement(new Paragraph("Ref: " + invoice.getUser().getReference(), normalFont));

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
            String invoiceDate = invoice.getDateTime().format(formatter);
            String dueDate = invoice.getDateTime().plusDays(15).format(formatter);

            String period = "N/A";
            if (!invoice.getLines().isEmpty()) {
                String start = invoice.getLines().get(0).getStartDateTime().format(formatter);
                String end = invoice.getLines().get(invoice.getLines().size() - 1).getEndDateTime().format(formatter);
                period = start + "-" + end;
            }

            rightCell.addElement(new Paragraph("Invoice #: " + invoice.getNumber(), normalFont));
            rightCell.addElement(new Paragraph("Invoice Date: " + invoiceDate, normalFont));
            rightCell.addElement(new Paragraph("Due Date: " + dueDate, normalFont));
            rightCell.addElement(new Paragraph("Invoice Period: " + period, normalFont));

            infoTable.addCell(leftCell);
            infoTable.addCell(rightCell);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 1.5f, 1.5f, 2f});

            String[] headers = {"Product", "Period", "Price", "Quantity", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (Line line : invoice.getLines()) {
                table.addCell(new Phrase(line.getProduct().name(), normalFont));
                String linePeriod = line.getStartDateTime().format(formatter) + "-" + line.getEndDateTime().format(formatter);
                table.addCell(new Phrase(linePeriod, normalFont));
                table.addCell(new Phrase("€ " + line.getPrice().toString(), normalFont));
                table.addCell(new Phrase(line.getQuantity().toString(), normalFont));
                table.addCell(new Phrase("€ " + line.getAmount().toString(), normalFont));
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            BigDecimal subtotal = invoice.getTotalAmount();
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(new Phrase("Subtotal:", headerFont));
            totalTable.addCell(new Phrase("€ " + subtotal, normalFont));
            totalTable.addCell(new Phrase("VAT", headerFont));
            totalTable.addCell(new Phrase("€", normalFont));
            totalTable.addCell(new Phrase("Grand Total:", headerFont));
            totalTable.addCell(new Phrase("€ " + subtotal, headerFont));

            document.add(totalTable);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Възникна грешка при генерирането на PDF: " + e.getMessage());
        }
    }
}