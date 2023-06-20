import dto.MeasurementDTO;
import dto.MeasurementResponse;


import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Client {
    public static void main(String[] args) {
//        String sensorName = "Another Sensor";
//        registerSensor(sensorName);
//        for (int i = 0; i < 1000; i++)
//            sendMeasurement(sensorName);
        createChart();
    }

    private static void registerSensor(String sensorName) {
        String url = "http://localhost:8080/sensor/registration";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("name", sensorName);
        sendRequest(url, jsonData);
    }

    private static void sendMeasurement(String sensorName) {
        String url = "http://localhost:8080/measurements/add";
        Map<String, Object> jsonData = new HashMap<>();
        Random random = new Random();
        jsonData.put("value", String.valueOf(random.nextInt(200) - 100));
        jsonData.put("raining", String.valueOf(random.nextBoolean()));
        jsonData.put("sensor", Map.of("name", sensorName));
        sendRequest(url, jsonData);
    }

    private static void sendRequest(String url, Map<String, Object> jsonData) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonData, httpHeaders);
        try {
            restTemplate.postForObject(url, request, String.class);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
        }
    }

    private static void createChart() {
        String url = "http://localhost:8080/measurements";
        RestTemplate restTemplate = new RestTemplate();
        MeasurementResponse jsonResponse = restTemplate.getForObject(url, MeasurementResponse.class);
        List<Double> measurementsList;
        if (jsonResponse == null || jsonResponse.getMeasurementDTOList() == null)
            measurementsList = Collections.emptyList();
        else measurementsList = jsonResponse.getMeasurementDTOList()
                .stream().map(MeasurementDTO::getValue).collect(Collectors.toList());
        double[] xData = IntStream.range(0, measurementsList.size()).asDoubleStream().toArray();
        double[] yData = measurementsList.stream().mapToDouble(x -> x).toArray();
        // Create Chart
        XYChart chart = QuickChart.getChart("Temperatures", "X", "Y", "y(x)", xData, yData);

        // Show it
        new SwingWrapper(chart).displayChart();
    }
}
