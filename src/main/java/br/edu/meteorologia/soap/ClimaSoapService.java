
package br.edu.meteorologia.soap;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ClimaSoapService {

    private static final String URL =
            "http://webservices.oorsprong.org/"
            + "websamples.countryinfo/CountryInfoService.wso";

    private static final String NAMESPACE =
            "http://www.oorsprong.org/websamples.countryinfo";

    private final HttpClient cliente = HttpClient.newHttpClient();

    // Guarda os dois dados que queremos mostrar:
    // capital do pais e XML SOAP original.
    public record ResultadoSoap(String capital, String xml) {
    }

    public ResultadoSoap consultarCapitalComXml(String codigoPais)
            throws Exception {

        if (codigoPais == null ||
                !codigoPais.matches("[a-zA-Z]{2}")) {
            throw new IllegalArgumentException(
                    "Codigo de pais invalido."
            );
        }

        String codigo = codigoPais.toUpperCase();

        String xmlRequisicao = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope
                    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:web="http://www.oorsprong.org/websamples.countryinfo">
                    <soap:Body>
                        <web:CapitalCity>
                            <web:sCountryISOCode>%s</web:sCountryISOCode>
                        </web:CapitalCity>
                    </soap:Body>
                </soap:Envelope>
                """.formatted(codigo);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header(
                    "SOAPAction",
                    "\"" + NAMESPACE + "/CapitalCity\""
                )
                .POST(HttpRequest.BodyPublishers.ofString(
                        xmlRequisicao, StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> resposta = cliente.send(
                requisicao,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        String xmlRecebido = resposta.body();

        System.out.println("\n------ XML SOAP ENVIADO ------");
        System.out.println(xmlRequisicao);

        System.out.println("\n------ XML SOAP RECEBIDO ------");
        System.out.println(xmlRecebido);

        // O XML SOAP pode conter informacoes de erro.
        // Por isso vamos interpretar antes de verificar o HTTP.

        DocumentBuilderFactory fabrica =
                DocumentBuilderFactory.newInstance();

        fabrica.setNamespaceAware(true);

        fabrica.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        fabrica.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        fabrica.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        fabrica.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        fabrica.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, ""
        );

        fabrica.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""
        );

        Document documento = fabrica.newDocumentBuilder()
                .parse(new InputSource(
                        new StringReader(xmlRecebido)
                ));

        NodeList falhas = documento.getElementsByTagNameNS(
                "http://schemas.xmlsoap.org/soap/envelope/",
                "Fault"
        );

        if (falhas.getLength() > 0) {
            throw new RuntimeException(
                    "O servidor retornou um SOAP Fault: "
                    + falhas.item(0).getTextContent().trim()
            );
        }

        if (resposta.statusCode() != 200) {
            throw new RuntimeException(
                    "Erro SOAP. HTTP: " + resposta.statusCode()
            );
        }

        NodeList resultado = documento.getElementsByTagNameNS(
                NAMESPACE,
                "CapitalCityResult"
        );

        if (resultado.getLength() == 0) {
            throw new RuntimeException(
                    "Capital nao encontrada na resposta SOAP."
            );
        }

        String capital = resultado.item(0).getTextContent();

        return new ResultadoSoap(capital, xmlRecebido);
    }

    // Mantem compatibilidade com o Main.java do console.
    public String consultarCapital(String codigoPais)
            throws Exception {

        return consultarCapitalComXml(codigoPais).capital();
    }
}
