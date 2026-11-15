
package br.edu.meteorologia.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CidadeRestService {

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode buscarCidade(String nomeCidade) throws Exception {

        String nome = URLEncoder.encode(
                nomeCidade.trim(), StandardCharsets.UTF_8
        );

        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + nome
                + "&count=1"
                + "&language=pt"
                + "&format=json";

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
                    "Erro ao buscar cidade. HTTP: "
                    + resposta.statusCode()
            );
        }

        JsonNode json = mapper.readTree(resposta.body());
        JsonNode resultados = json.path("results");

        if (!resultados.isArray() || resultados.isEmpty()) {
            throw new RuntimeException(
                    "Cidade nao encontrada: " + nomeCidade
            );
        }

        return resultados.get(0);
    }
}
