
package br.edu.meteorologia.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClimaRestService {

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode consultarClima(double latitude, double longitude)
            throws Exception {

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,"
                + "apparent_temperature,wind_speed_10m"
                + "&daily=temperature_2m_max,temperature_2m_min,"
                + "precipitation_probability_max,weather_code"
                + "&forecast_days=5"
                + "&timezone=auto";

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> resposta = cliente.send(
                requisicao,
                HttpResponse.BodyHandlers.ofString()
        );

        if (resposta.statusCode() != 200) {
            throw new RuntimeException(
                    "Erro ao consultar API. HTTP: "
                    + resposta.statusCode()
                    + " - " + resposta.body()
            );
        }

        return mapper.readTree(resposta.body());
    }
}
