package com.example.billing.distribution.service;

import com.example.billing.distribution.parser.DistributionInputParser;
import com.example.billing.model.Line;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProportionalDistributionServiceTest {

    private final ProportionalDistributionService service = new ProportionalDistributionService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    @Test
    void testScenario1() throws Exception { runScenarioTest("sc1"); }

    @Test
    void testScenario2() throws Exception { runScenarioTest("sc2"); }

    @Test
    void testScenario3() throws Exception { runScenarioTest("sc3"); }

    @Test
    void testScenario4() throws Exception { runScenarioTest("sc4"); }

    @Test
    void testScenario5() throws Exception { runScenarioTest("sc5"); }

    private void runScenarioTest(String scenarioFolder) throws Exception {
        InputStream inStream = getClass().getResourceAsStream("/" + scenarioFolder + "/in.txt");
        InputStream outStream = getClass().getResourceAsStream("/" + scenarioFolder + "/out.txt");

        assertNotNull(inStream, "in.txt не е намерен в resources/" + scenarioFolder);
        assertNotNull(outStream, "out.txt не е намерен в resources/" + scenarioFolder);

        DistributionInputParser.ParsedInput input = DistributionInputParser.parse(inStream);
        List<String> expectedLines = readLines(outStream);

        List<Line> actualLines = new ArrayList<>();

        for (DistributionInputParser.ReadingPair pair : input.readingPairs()) {
            actualLines.addAll(service.distribute(pair.start(), pair.end(), input.prices()));
        }

        assertEquals(expectedLines.size(), actualLines.size(), "Броят на генерираните редове не съвпада!");

        for (int i = 0; i < expectedLines.size(); i++) {
            String[] expectedParts = expectedLines.get(i).split(",");
            Line actualLine = actualLines.get(i);

            assertEquals(expectedParts[0], actualLine.getStartDateTime().format(formatter), "Грешка в Началната дата на ред " + (i + 1));
            assertEquals(expectedParts[1], actualLine.getEndDateTime().format(formatter), "Грешка в Крайната дата на ред " + (i + 1));

            assertEquals(0, new BigDecimal(expectedParts[2]).compareTo(actualLine.getQuantity()), "Грешка в Количеството на ред " + (i + 1));
            assertEquals(0, new BigDecimal(expectedParts[3]).compareTo(actualLine.getPrice()), "Грешка в Цената на ред " + (i + 1));
        }
    }

    private List<String> readLines(InputStream is) throws Exception {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }
}