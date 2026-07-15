package com.travelsimulator.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.travelsimulator.entity.Scenario;
import com.travelsimulator.entity.ScenarioResult;
import com.travelsimulator.entity.TripScenarioSession;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generateComparisonPdf(TripScenarioSession session) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(76, 81, 238));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(15, 23, 42));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font disclaimerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.RED);

            // Document Header
            Paragraph title = new Paragraph("AI What-If Travel Simulator", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Trip Scenario Comparison Report", subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Session Info Box
            Paragraph sessionInfo = new Paragraph();
            sessionInfo.add(new Chunk("Session ID: ", boldBodyFont));
            sessionInfo.add(new Chunk(session.getId().toString() + "\n", bodyFont));
            sessionInfo.add(new Chunk("Created At: ", boldBodyFont));
            sessionInfo.add(new Chunk(session.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n", bodyFont));
            sessionInfo.setSpacingAfter(15);
            document.add(sessionInfo);

            // Add Disclaimer
            Paragraph disclaimer = new Paragraph("*Estimates based on historical data and AI reasoning — always verify with official sources before travel.*", disclaimerFont);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            disclaimer.setSpacingAfter(15);
            document.add(disclaimer);

            // 1. Comparison Table
            document.add(new Paragraph("Scenario Comparison Matrix", headingFont));
            document.add(new Paragraph(" ", bodyFont)); // spacer

            List<Scenario> scenarios = session.getScenarios();
            
            // Create Table: 6 columns
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 1.2f, 1.2f, 1.5f, 1.5f, 1.5f});

            // Table Headers
            String[] headers = {"Scenario Label", "Score", "Cost Est", "Weather", "Safety", "Mode"};
            Color headerBg = new Color(30, 41, 59);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(headerText, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Rows
            for (Scenario scenario : scenarios) {
                ScenarioResult result = scenario.getResult();
                
                table.addCell(new PdfPCell(new Paragraph(scenario.getLabel(), bodyFont)));
                
                String scoreStr = result != null ? String.valueOf(result.getDecisionScore()) : "N/A";
                PdfPCell scoreCell = new PdfPCell(new Paragraph(scoreStr, boldBodyFont));
                scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(scoreCell);

                String costStr = result != null ? "INR " + String.format("%.0f", result.getBudgetProjection() + result.getHiddenExpenses()) : "N/A";
                table.addCell(new PdfPCell(new Paragraph(costStr, bodyFont)));

                String weatherStr = result != null ? result.getWeatherSummary() : "N/A";
                table.addCell(new PdfPCell(new Paragraph(weatherStr, bodyFont)));

                String safetyStr = result != null ? result.getSafetyScore() + "/100" : "N/A";
                table.addCell(new PdfPCell(new Paragraph(safetyStr, bodyFont)));

                table.addCell(new PdfPCell(new Paragraph(scenario.getTravelMode(), bodyFont)));
            }

            document.add(table);
            document.add(new Paragraph(" ", bodyFont)); // spacer
            document.add(new Paragraph(" ", bodyFont)); // spacer

            // 2. Scenario Explanations
            document.add(new Paragraph("AI Simulation Insights & Detailed Forecasts", headingFont));
            document.add(new Paragraph(" ", bodyFont));

            for (Scenario scenario : scenarios) {
                ScenarioResult result = scenario.getResult();
                if (result == null) continue;

                Paragraph scenarioSection = new Paragraph();
                scenarioSection.add(new Chunk("■ " + scenario.getLabel() + " (Score: " + result.getDecisionScore() + "/100)\n", boldBodyFont));
                scenarioSection.add(new Chunk("Destination: ", boldBodyFont));
                scenarioSection.add(new Chunk(scenario.getDestination() + " | ", bodyFont));
                scenarioSection.add(new Chunk("Stated Budget: ", boldBodyFont));
                scenarioSection.add(new Chunk("INR " + scenario.getBudget() + " | ", bodyFont));
                scenarioSection.add(new Chunk("Travel Duration: ", boldBodyFont));
                scenarioSection.add(new Chunk(result.getTravelTimeEstimate() + "\n", bodyFont));
                
                scenarioSection.add(new Chunk("Weather Overview: ", boldBodyFont));
                scenarioSection.add(new Chunk(result.getWeatherSummary() + "\n", bodyFont));

                scenarioSection.add(new Chunk("AI Explanation: ", boldBodyFont));
                scenarioSection.add(new Chunk(result.getAiExplanation() + "\n", bodyFont));

                scenarioSection.setSpacingAfter(15);
                document.add(scenarioSection);
            }

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
