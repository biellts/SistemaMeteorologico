
package br.edu.meteorologia;

import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.meteorologia.rest.CidadeRestService;
import br.edu.meteorologia.rest.ClimaRestService;
import br.edu.meteorologia.soap.ClimaSoapService;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("     SISTEMA METEOROLOGICO");
        System.out.println("==================================");

        System.out.print("\nDigite o nome da cidade: ");
        String nomeCidade = scanner.nextLine().trim();

        if (nomeCidade.isEmpty()) {
            System.out.println("Informe uma cidade valida.");
            return;
        }

        try {
            // ==========================================
            // ETAPA 1 - BUSCAR CIDADE UTILIZANDO REST
            // ==========================================

            CidadeRestService cidadeService =
                    new CidadeRestService();

            System.out.println("\nBuscando cidade...");

            JsonNode cidade = cidadeService.buscarCidade(nomeCidade);

            String nome = cidade.path("name").asText();
            String estado = cidade.path("admin1").asText("");
            String pais = cidade.path("country").asText("");

            String codigoPais = cidade.path("country_code").asText("");

            double latitude = cidade.path("latitude").asDouble();
            double longitude = cidade.path("longitude").asDouble();

            System.out.println("Cidade encontrada: "
                    + nome + " - " + estado + " - " + pais);

            // ==========================================
            // ETAPA 2 - CONSULTAR CLIMA UTILIZANDO REST
            // ==========================================

            ClimaRestService climaService =
                    new ClimaRestService();

            System.out.println("\nConsultando previsao do tempo...");

            JsonNode resposta = climaService.consultarClima(
                    latitude, longitude
            );

            JsonNode atual = resposta.path("current");

            System.out.println("\n------ CLIMA ATUAL ------");

            System.out.println("Cidade: " + nome);

            System.out.println("Temperatura: "
                    + atual.path("temperature_2m").asDouble()
                    + " °C");

            System.out.println("Sensacao termica: "
                    + atual.path("apparent_temperature").asDouble()
                    + " °C");

            System.out.println("Umidade: "
                    + atual.path("relative_humidity_2m").asInt()
                    + " %");

            System.out.println("Vento: "
                    + atual.path("wind_speed_10m").asDouble()
                    + " km/h");

            System.out.println("Atualizacao: "
                    + atual.path("time").asText());

            // ==========================================
            // ETAPA 3 - PREVISAO DOS PROXIMOS 5 DIAS
            // ==========================================

            JsonNode diario = resposta.path("daily");

            System.out.println("\n------ PREVISAO DE 5 DIAS ------");

            for (int i = 0; i < diario.path("time").size(); i++) {

                String data = diario.path("time")
                        .get(i).asText();

                double maxima = diario.path("temperature_2m_max")
                        .get(i).asDouble();

                double minima = diario.path("temperature_2m_min")
                        .get(i).asDouble();

                int chuva = diario.path("precipitation_probability_max")
                        .get(i).asInt();

                int codigo = diario.path("weather_code")
                        .get(i).asInt();

                System.out.println("\nData: " + data);
                System.out.println("Maxima: " + maxima + " °C");
                System.out.println("Minima: " + minima + " °C");

                System.out.println(
                        "Probabilidade maxima de chuva: "
                        + chuva + " %"
                );

                System.out.println(
                        "Condicao: " + descricaoClima(codigo)
                );

                System.out.println("-------------------------");
            }

            // ==========================================
            // ETAPA 4 - CONSULTAR CAPITAL UTILIZANDO SOAP
            // ==========================================

            System.out.println("\n------ CONSULTA SOAP ------");

            try {

                ClimaSoapService soapService =
                        new ClimaSoapService();

                System.out.println(
                        "Consultando capital de: " + pais
                );

                String capital = soapService.consultarCapital(
                        codigoPais
                );

                System.out.println(
                        "\n------ INFORMACOES DO PAIS ------"
                );

                System.out.println("Pais: " + pais);
                System.out.println("Codigo ISO: " + codigoPais);
                System.out.println("Capital: " + capital);

            } catch (Exception erroSoap) {

                System.out.println(
                        "Falha na consulta SOAP: "
                        + erroSoap.getMessage()
                );
            }

            // ==========================================
            // ETAPA 5 - MOSTRAR JSON RECEBIDO PELO REST
            // ==========================================

            System.out.println("\n------ JSON RECEBIDO ------");
            System.out.println(resposta.toPrettyString());

        } catch (Exception e) {

            System.out.println(
                    "\nErro ao executar sistema: "
                    + e.getMessage()
            );
        }
    }

    // ==========================================
    // CONVERTER CODIGOS METEOROLOGICOS
    // ==========================================

    private static String descricaoClima(int codigo) {

        return switch (codigo) {

            case 0 -> "Ceu limpo";

            case 1, 2, 3 ->
                    "Ceu pouco nublado ou nublado";

            case 45, 48 -> "Neblina";

            case 51, 53, 55 -> "Garoa";

            case 56, 57 -> "Garoa congelante";

            case 61, 63, 65 -> "Chuva";

            case 66, 67 -> "Chuva congelante";

            case 71, 73, 75, 77 -> "Neve";

            case 80, 81, 82 -> "Pancadas de chuva";

            case 85, 86 -> "Pancadas de neve";

            case 95, 96, 99 -> "Trovoadas";

            default -> "Condicao desconhecida";
        };
    }
}
