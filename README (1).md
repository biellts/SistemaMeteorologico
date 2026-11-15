# Sistema Meteorológico — Integração REST e SOAP

Projeto acadêmico da disciplina **Sistemas Distribuídos**, desenvolvido em Java com Spring Boot. A aplicação reúne dois Web Services públicos em uma única interface web, em português, para consultar informações meteorológicas de uma cidade e a capital do país correspondente.

## Objetivo

Demonstrar o consumo e a integração de duas tecnologias de Web Services:

- **REST (JSON):** localização de cidades, clima atual e previsão para cinco dias, por meio da Open-Meteo.
- **SOAP (XML/WSDL):** consulta da capital do país, por meio da operação `CapitalCity` do CountryInfoService.

O usuário informa uma cidade no navegador. O backend localiza a cidade, consulta a previsão meteorológica e obtém a capital do país. A interface mostra as informações e os textos das respostas JSON e XML recebidas.

## Tecnologias

- Java (compilação configurada para Java 21; desenvolvimento e testes com JDK 27)
- Maven 3.9.16
- Spring Boot 4.1.2
- Java `HttpClient`
- Jackson (`JsonNode` e `ObjectMapper`) para interpretar JSON
- Java XML DOM (`DocumentBuilderFactory`) para interpretar XML SOAP
- HTML, CSS e JavaScript
- VS Code

## Pré-requisitos

- JDK 21 ou posterior compatível com a configuração do projeto; os testes foram realizados com JDK 27.
- Maven instalado e disponível no PATH.
- Internet para consultar as APIs públicas.
- Porta TCP **8081** livre no computador (configurada em `src/main/resources/application.properties`).

Verificar as instalações:

```powershell
java -version
mvn -version
```

## Como executar

Abra o terminal do VS Code na pasta do projeto. No computador usado no desenvolvimento, o caminho foi:

```powershell
cd "D:\Arquivos 20-05-2022\Desktop\sistema-meteorologico"
mvn clean compile
mvn spring-boot:run
```

Após o Spring Boot iniciar, acesse:

**http://localhost:8081**

Digite o nome de uma cidade e clique em **Consultar** (ou pressione Enter). Nenhuma cidade é pesquisada automaticamente ao abrir a página.

Para encerrar a aplicação, pressione `Ctrl + C` no terminal. A execução do projeto não exige banco de dados nem servidor de aplicações externo.

> Se a porta 8081 estiver ocupada, feche a instância anterior do sistema ou altere `server.port` em `src/main/resources/application.properties`. O Apache HTTP Server ocupava a porta 8080 na máquina de desenvolvimento.

## Funcionamento da integração

1. O JavaScript em `app.js` faz uma requisição ao endpoint local `GET /api/clima?cidade=<nome>`.
2. `CidadeRestService.java` utiliza a **Geocoding API** da Open-Meteo para buscar a localização e obter latitude, longitude e código ISO do país.
3. `ClimaRestService.java` utiliza a **Forecast API** da Open-Meteo para receber o clima atual e a previsão de cinco dias em JSON.
4. `ClimaSoapService.java` monta uma mensagem SOAP/XML com a operação `CapitalCity` e envia uma requisição HTTP POST ao CountryInfoService, passando o código ISO do país.
5. `ClimaController.java` integra os dados e os entrega à interface em uma resposta JSON. O JavaScript atualiza os cards de clima, previsão e dados do país.

## Estrutura principal

```text
sistema-meteorologico/
├── pom.xml
├── countryinfo.wsdl
├── README.md
└── src/main/
    ├── java/br/edu/meteorologia/
    │   ├── Main.java
    │   ├── SistemaMeteorologicoApplication.java
    │   ├── controller/
    │   │   └── ClimaController.java
    │   ├── rest/
    │   │   ├── CidadeRestService.java
    │   │   └── ClimaRestService.java
    │   └── soap/
    │       └── ClimaSoapService.java
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── css/style.css
            ├── js/app.js
            └── img/
```

- `Main.java`: execução alternativa pelo console, mantida durante o desenvolvimento.
- `SistemaMeteorologicoApplication.java`: inicializa a aplicação web Spring Boot.
- `ClimaController.java`: disponibiliza o endpoint `/api/clima` e integra REST e SOAP.
- `CidadeRestService.java`: resolve o nome da cidade para uma localização geográfica.
- `ClimaRestService.java`: faz a consulta meteorológica REST.
- `ClimaSoapService.java`: constrói o envelope SOAP, recebe e interpreta XML.
- `index.html`, `style.css`, `app.js`: interface em português, estilo desktop e atualizações sem recarregar a página.

## Serviços e documentações oficiais

### REST — Open-Meteo

- Documentação da Forecast API: https://open-meteo.com/en/docs
- Documentação da Geocoding API: https://open-meteo.com/en/docs/geocoding-api
- Endpoint de geolocalização: https://geocoding-api.open-meteo.com/v1/search
- Endpoint de clima: https://api.open-meteo.com/v1/forecast

As respostas da Open-Meteo são utilizadas em formato JSON. Os campos solicitados incluem `temperature_2m`, `apparent_temperature`, `relative_humidity_2m`, `wind_speed_10m`, `temperature_2m_max`, `temperature_2m_min`, `precipitation_probability_max` e `weather_code`.

### SOAP — CountryInfoService

- Página do serviço: http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso
- **Link direto do WSDL utilizado:** http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL
- Operação utilizada: `CapitalCity`.
- Entrada: `sCountryISOCode`, como `BR`.
- Saída: `CapitalCityResult`, como `Brasilia`.

O arquivo `countryinfo.wsdl` foi baixado para a raiz do projeto durante os testes. A chamada SOAP foi realizada por HTTP, pois o acesso ao endpoint HTTPS não funcionou na máquina de desenvolvimento. Como HTTP não criptografa o tráfego, esse serviço foi empregado somente para a demonstração acadêmica com informações públicas, sem envio de dados sensíveis.

## Exemplo real de JSON recebido (REST)

Trecho extraído da resposta real da Open-Meteo na consulta de **Bocaiúva — MG**, em **02/11/2026 às 11h30**. O JSON completo também pode ser inspecionado no painel REST da aplicação.

```json
{
  "latitude": -17.11775,
  "longitude": -43.832855,
  "timezone": "America/Sao_Paulo",
  "current": {
    "time": "2026-11-02T11:30",
    "interval": 900,
    "temperature_2m": 29.8,
    "relative_humidity_2m": 53,
    "apparent_temperature": 34.6,
    "wind_speed_10m": 7.7
  },
  "daily": {
    "time": ["2026-11-02", "2026-11-03", "2026-11-04", "2026-11-05", "2026-11-06"],
    "temperature_2m_max": [30.9, 31.1, 32.8, 34.5, 34.1],
    "temperature_2m_min": [20.5, 20.1, 20.1, 20.9, 20.4],
    "precipitation_probability_max": [82, 63, 11, 25, 47],
    "weather_code": [53, 81, 51, 51, 95]
  }
}
```

> O exemplo acima foi reduzido aos campos principais para facilitar a leitura; as respostas completas incluem também `generationtime_ms`, `utc_offset_seconds`, `elevation` e as unidades de medida.

## Exemplo real de XML recebido (SOAP)

Resposta recebida ao executar a operação `CapitalCity` para o código ISO **BR**:

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <m:CapitalCityResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
      <m:CapitalCityResult>Brasilia</m:CapitalCityResult>
    </m:CapitalCityResponse>
  </soap:Body>
</soap:Envelope>
```

## Diferenças REST × SOAP observadas

| Aspecto | REST — Open-Meteo | SOAP — CountryInfoService |
|---|---|---|
| Estilo/protocolo | REST: estilo arquitetural | SOAP: protocolo de mensagens |
| Método HTTP usado no projeto | GET | POST |
| Formato da resposta usado no projeto | JSON | XML em envelope SOAP |
| Descrição do serviço | Documentação dos endpoints | Contrato WSDL |
| Utilização | Dados meteorológicos | Capital do país |

## Dificuldades e soluções

- **WSDL via HTTPS:** a conexão HTTPS falhou; foi utilizado o endpoint HTTP disponível.
- **Porta 8080 ocupada:** o Apache HTTP Server estava usando a porta, então o projeto foi configurado para a 8081.
- **Porta 8081 ocupada em outra execução:** foi necessário encerrar o servidor antigo antes de reiniciar.
- **Cidades com nomes iguais:** uma busca por Nova York retornou Nova Iorque (Maranhão); o código passou a tratar o caso específico com `New York` e filtro `countryCode=US`. Uma seleção geral de localidades homônimas pode ser acrescentada futuramente.
- **Formatação da localização:** cidades brasileiras são exibidas com sigla de UF; cidades de outros países são exibidas com o país.
- **Classe planejada e não utilizada:** `DadosClimaticos.java` foi removida, pois os dados são manipulados diretamente como objetos JSON.

## Limitações conhecidas

- As informações dependem da disponibilidade e das respostas das duas APIs externas.
- A API de geocoding pode retornar cidades homônimas; o tratamento atual não substitui uma seleção geral de localizações.
- A consulta SOAP utiliza HTTP sem criptografia; não deve ser usada para transmitir dados sensíveis.
- A interface foi desenvolvida para uso em computador, não priorizando telas pequenas.

## Verificação técnica

O endpoint consumido pelo navegador é:

```text
GET http://localhost:8081/api/clima?cidade=Bocaiuva
```

A interface permite fazer a consulta sem digitar o endpoint. A resposta integrada contém `localizacao`, `atual`, `previsao`, `jsonRest` e `soap`, incluindo `xmlSoap` quando a consulta SOAP é bem-sucedida.

## Referências complementares do estudo

- Artigo sobre REST e SOAP (InfoQ Brasil): https://www.infoq.com/br/articles/rest-soap-when-to-use-each/
- Documentação Open-Meteo: https://open-meteo.com/en/docs
- Documentação Open-Meteo Geocoding: https://open-meteo.com/en/docs/geocoding-api
- WSDL CountryInfoService: http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL
