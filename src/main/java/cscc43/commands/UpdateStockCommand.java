package cscc43.commands;

import java.sql.Date;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

import java.util.Scanner;

@ShellComponent
public class UpdateStockCommand {

    private final JdbcTemplate jdbcTemplate;
    private final Scanner scanner;
    private final RestTemplate restTemplate;
    private final String apiKey = "HC2I4DMMURVHJGIQ"; // Replace with your API key

    public UpdateStockCommand(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.scanner = new Scanner(System.in); // Initialize scanner
        this.restTemplate = new RestTemplate(); // Initialize RestTemplate
    }

    @ShellMethod(key = "update", value = "Update or add a new stock entry.")
    public String updateStock(@ShellOption String symbol, @ShellOption String date) {

        // Fetch data from Alpha Vantage
        Map<String, String> stockData = fetchStockData(symbol, date);

        if (stockData == null) {
            return "Failed to fetch stock data.";
        }

        // Validate and parse inputs
        Date timestamp = parseDate(date);
        Double open = parseDouble(stockData.get("1. open"));
        Double low = parseDouble(stockData.get("3. low"));
        Double high = parseDouble(stockData.get("2. high"));
        Double close = parseDouble(stockData.get("4. close"));
        Long volume = parseLong(stockData.get("5. volume"));

        // Check if all values are valid
        if (timestamp == null || open == null || low == null || high == null || close == null || volume == null) {
            return "Invalid input. Please check the format of your inputs.";
        }

        System.out.println("You are about to update/add the following stock entry:");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Symbol: " + symbol);
        System.out.println("Open: " + open);
        System.out.println("Low: " + low);
        System.out.println("High: " + high);
        System.out.println("Close: " + close);
        System.out.println("Volume: " + volume);
        System.out.println("Press 0 to go back or type 'yes' to proceed: ");

        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("0")) {
            return "Stock entry update cancelled.";
        } else if (confirmation.equalsIgnoreCase("yes")) {
            // SQL query to insert or update stock entry
            String sql = """
                INSERT INTO Stocks (timestamp, symbol, open, low, high, close, volume)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (timestamp, symbol)
                DO UPDATE SET open = EXCLUDED.open,
                              low = EXCLUDED.low,
                              high = EXCLUDED.high,
                              close = EXCLUDED.close,
                              volume = EXCLUDED.volume;
            """;

            jdbcTemplate.update(sql, timestamp, symbol, open, low, high, close, volume);
            return "Stock entry updated successfully.";
        } else {
            return "Invalid input. Stock entry update cancelled.";
        }
    }

    private Map<String, String> fetchStockData(String symbol, String date) {
        String url = "https://www.alphavantage.co/query";
        
        // Build the query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", symbol)
                .queryParam("apikey", apiKey)
                .queryParam("outputsize", "full")
                .queryParam("datatype", "json");
        
        // Send the HTTP GET request
        ResponseEntity<Map> response = restTemplate.getForEntity(builder.toUriString(), Map.class);
        
        // Check if the request was successful
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> timeSeries = (Map<String, Object>) responseBody.get("Time Series (Daily)");
            
            if (timeSeries.containsKey(date)) {
                Map<String, String> stockData = (Map<String, String>) timeSeries.get(date);
                return stockData;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // Helper method to parse Date
    private Date parseDate(String dateStr) {
        try {
            return Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Helper method to parse Double
    private Double parseDouble(String doubleStr) {
        try {
            return Double.valueOf(doubleStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Helper method to parse Long
    private Long parseLong(String longStr) {
        try {
            return Long.valueOf(longStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
