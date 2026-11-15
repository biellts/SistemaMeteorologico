package br.edu.meteorologia.controller;

import br.edu.meteorologia.rest.CidadeRestService;
import br.edu.meteorologia.rest.ClimaRestService;
import br.edu.meteorologia.soap.ClimaSoapService;
import br.edu.meteorologia.soap.ClimaSoapService.ResultadoSoap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClimaController {

    private final CidadeRestService cidadeService =
            new CidadeRestService();

    private final ClimaRestService climaService =
            new ClimaRestService();

    private final ClimaSoapService soapService =
            new ClimaSoapService();

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(
            value = "/api/clima",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> consultarClima(
            @RequestParam String cidade) {

        if (cidade == null || cidade.isBlank()) {
            return criarErro(
                    400, "Informe o nome de uma cidade."
            );
        }

        try {

            // ETAPA 1: LOCALIZAR CIDADE VIA REST

            JsonNode local = cidadeService.buscarCidade(
                    cidade.trim()
            );

            String nome = local.path("name").asText();
            String estado = local.path("admin1").asText("");
            String pais = local.path("country").asText("");
            String codigoPais = local.path("country_code").asText("");

            double latitude = local.path("latitude").asDouble();
            double longitude = local.path("longitude").asDouble();

            // ETAPA 2: CONSULTAR CLIMA VIA REST

            JsonNode clima = climaService.consultarClima(
                    latitude, longitude
            );

            ObjectNode resultado = mapper.createObjectNode();

            ObjectNode localizacao =
                    resultado.putObject("localizacao");

            localizacao.put("cidade", nome);
            localizacao.put("estado", estado);
            localizacao.put("pais", pais);
            localizacao.put("codigoPais", codigoPais);
            localizacao.put("latitude", latitude);
            localizacao.put("longitude", longitude);

            resultado.set("atual", clima.path("current"));
            resultado.set("previsao", clima.path("daily"));

            // JSON original da Open-Meteo
            resultado.set("jsonRest", clima);

            // ETAPA 3: CONSULTAR CAPITAL VIA SOAP

            ObjectNode dadosSoap = resultado.putObject("soap");

            dadosSoap.put("pais", pais);
            dadosSoap.put("codigoISO", codigoPais);

            try {
                ResultadoSoap respostaSoap =
                        soapService.consultarCapitalComXml(
                                codigoPais
                        );

                dadosSoap.put(
                        "capital",
                        respostaSoap.capital()
                );

                dadosSoap.put(
                        "xmlSoap",
                        respostaSoap.xml()
                );

                dadosSoap.put("sucesso", true);

            } catch (Exception erroSoap) {

                dadosSoap.put("capital", "");
                dadosSoap.put("xmlSoap", "");
                dadosSoap.put("sucesso", false);

                dadosSoap.put(
                        "erro",
                        "Servico SOAP indisponivel: "
                                + erroSoap.getMessage()
                );
            }

            // ETAPA 4: DEVOLVER TUDO PARA O NAVEGADOR

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        mapper.writeValueAsString(resultado)
                    );

        } catch (Exception e) {

            return criarErro(
                    502,
                    "Nao foi possivel consultar a cidade: "
                            + e.getMessage()
            );
        }
    }

    private ResponseEntity<String> criarErro(
            int status, String mensagem) {

        try {
            ObjectNode json = mapper.createObjectNode();
            json.put("erro", mensagem);

            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(json));

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("{\"erro\":\"Erro interno.\"");
        }
    }
}
