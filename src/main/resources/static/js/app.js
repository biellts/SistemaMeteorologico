const campoCidade = document.getElementById("cidade");
const botaoConsultar = document.getElementById("btnConsultar");
const mensagem = document.getElementById("mensagem");

const temperatura = document.getElementById("temperatura");
const temperaturaDetalhe = document.getElementById("temperaturaDetalhe");
const nomeCidade = document.getElementById("nomeCidade");
const sensacao = document.getElementById("sensacao");
const umidade = document.getElementById("umidade");
const vento = document.getElementById("vento");
const atualizacao = document.getElementById("atualizacao");

const pais = document.getElementById("pais");
const codigoISO = document.getElementById("codigoISO");
const capital = document.getElementById("capital");

const listaPrevisao = document.getElementById("listaPrevisao");
const jsonRest = document.getElementById("jsonRest");
const xmlSoap = document.getElementById("xmlSoap");


// ==========================================
// SIGLAS DOS ESTADOS BRASILEIROS
// ==========================================

const siglasEstados = {
    "acre": "AC",
    "alagoas": "AL",
    "amapa": "AP",
    "amazonas": "AM",
    "bahia": "BA",
    "ceara": "CE",
    "distrito federal": "DF",
    "espirito santo": "ES",
    "goias": "GO",
    "maranhao": "MA",
    "mato grosso": "MT",
    "mato grosso do sul": "MS",
    "minas gerais": "MG",
    "para": "PA",
    "paraiba": "PB",
    "parana": "PR",
    "pernambuco": "PE",
    "piaui": "PI",
    "rio de janeiro": "RJ",
    "rio grande do norte": "RN",
    "rio grande do sul": "RS",
    "rondonia": "RO",
    "roraima": "RR",
    "santa catarina": "SC",
    "sao paulo": "SP",
    "sergipe": "SE",
    "tocantins": "TO"
};


// Remove acentos para facilitar comparações
function normalizarTexto(texto) {
    return String(texto || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}


// ==========================================
// FORMATAR LOCALIZAÇÃO
// ==========================================

function formatarLocalizacao(local) {

    const cidade = local.cidade || "Cidade desconhecida";
    const estado = local.estado || "";
    const paisLocal = local.pais || "";
    const codigo = String(local.codigoPais || "")
        .toUpperCase();

    // Cidades brasileiras: mostrar sigla do estado
    if (codigo === "BR") {

        const estadoNormalizado = normalizarTexto(estado);

        const sigla = siglasEstados[estadoNormalizado]
            || estado;

        return sigla
            ? `${cidade} - ${sigla}`
            : cidade;
    }

    // Cidades estrangeiras: mostrar nome do país
    if (paisLocal) {
        return `${cidade} - ${paisLocal}`;
    }

    return cidade;
}


// ==========================================
// FORMATAÇÃO DE VALORES
// ==========================================

function formatarNumero(valor, casas = 1) {

    if (valor === null || valor === undefined) {
        return "--";
    }

    const numero = Number(valor);

    if (!Number.isFinite(numero)) {
        return "--";
    }

    return numero.toLocaleString("pt-BR", {
        minimumFractionDigits: casas,
        maximumFractionDigits: casas
    });
}


function formatarData(data) {

    if (!data) {
        return "--";
    }

    const partes = data.split("-");

    if (partes.length !== 3) {
        return data;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}


function formatarAtualizacao(dataHora) {

    if (!dataHora) {
        return "--";
    }

    const [data, horario] = dataHora.split("T");

    return `${formatarData(data)} às ${horario || "--"}`;
}


// ==========================================
// DESCRIÇÕES METEOROLÓGICAS
// ==========================================

function descricaoClima(codigo) {

    const descricoes = {
        0: "Céu limpo",
        1: "Predominantemente limpo",
        2: "Parcialmente nublado",
        3: "Nublado",
        45: "Neblina",
        48: "Neblina",
        51: "Garoa leve",
        53: "Garoa moderada",
        55: "Garoa intensa",
        56: "Garoa congelante",
        57: "Garoa congelante",
        61: "Chuva leve",
        63: "Chuva moderada",
        65: "Chuva forte",
        66: "Chuva congelante",
        67: "Chuva congelante",
        71: "Neve leve",
        73: "Neve moderada",
        75: "Neve forte",
        77: "Grãos de neve",
        80: "Pancadas de chuva leves",
        81: "Pancadas de chuva",
        82: "Pancadas de chuva fortes",
        85: "Pancadas de neve",
        86: "Pancadas de neve fortes",
        95: "Trovoadas",
        96: "Trovoadas com granizo",
        99: "Trovoadas com granizo"
    };

    return descricoes[codigo] || "Condição desconhecida";
}


// ==========================================
// ÍCONES METEOROLÓGICOS
// ==========================================

function iconeClima(codigo) {

    if (codigo === 0 || codigo === 1) {
        return "☀️";
    }

    if (codigo === 2) {
        return "🌤️";
    }

    if (codigo === 3) {
        return "☁️";
    }

    if ([45, 48].includes(codigo)) {
        return "🌫️";
    }

    if ([95, 96, 99].includes(codigo)) {
        return "⛈️";
    }

    if ([71, 73, 75, 77, 85, 86].includes(codigo)) {
        return "❄️";
    }

    return "🌧️";
}


// ==========================================
// EXIBIR MENSAGENS
// ==========================================

function mostrarMensagem(texto, erro = false) {

    mensagem.textContent = texto;

    mensagem.style.color = erro
        ? "#a31f33"
        : "#103771";
}


// ==========================================
// EXIBIR CLIMA ATUAL - REST
// ==========================================

function exibirClima(dados) {

    const local = dados.localizacao;
    const atual = dados.atual;

    // Exibe "Montes Claros - MG" ou "Paris - França"
    nomeCidade.textContent = formatarLocalizacao(local);

    temperatura.textContent =
        `${formatarNumero(atual.temperature_2m)} °C`;

    temperaturaDetalhe.textContent =
        `${formatarNumero(atual.temperature_2m)} °C`;

    sensacao.textContent =
        `${formatarNumero(atual.apparent_temperature)} °C`;

    umidade.textContent =
        `${formatarNumero(atual.relative_humidity_2m, 0)} %`;

    vento.textContent =
        `${formatarNumero(atual.wind_speed_10m)} km/h`;

    atualizacao.textContent =
        formatarAtualizacao(atual.time);

    // JSON original recebido da Open-Meteo
    jsonRest.textContent = JSON.stringify(
        dados.jsonRest,
        null,
        2
    );
}


// ==========================================
// EXIBIR PREVISÃO DOS PRÓXIMOS CINCO DIAS
// ==========================================

function exibirPrevisao(previsao) {

    listaPrevisao.replaceChildren();

    const datas = previsao.time || [];

    const maximas =
        previsao.temperature_2m_max || [];

    const minimas =
        previsao.temperature_2m_min || [];

    const chuvas =
        previsao.precipitation_probability_max || [];

    const codigos =
        previsao.weather_code || [];

    const quantidade = Math.min(datas.length, 5);

    for (let i = 0; i < quantidade; i++) {

        const codigo = codigos[i];

        // Criar card do dia
        const card = document.createElement("article");
        card.className = "dia-card";

        // Data
        const titulo = document.createElement("h3");
        titulo.textContent = formatarData(datas[i]);

        // Conteúdo do card
        const conteudo = document.createElement("div");
        conteudo.className = "dia-conteudo";

        // Ícone meteorológico
        const icone = document.createElement("div");
        icone.className = "dia-icone";
        icone.textContent = iconeClima(codigo);

        // Detalhes meteorológicos
        const detalhes = document.createElement("div");
        detalhes.className = "dia-detalhes";

        const maxima = document.createElement("p");
        maxima.textContent =
            `🌡️ Máxima: ${formatarNumero(maximas[i])} °C`;

        maxima.className = "maxima";

        const minima = document.createElement("p");
        minima.textContent =
            `❄️ Mínima: ${formatarNumero(minimas[i])} °C`;

        minima.className = "minima";

        const chuva = document.createElement("p");
        chuva.textContent =
            `🌧️ Chuva: ${formatarNumero(chuvas[i], 0)} %`;

        detalhes.append(maxima, minima, chuva);

        conteudo.append(icone, detalhes);

        // Descrição do clima
        const condicao = document.createElement("p");
        condicao.className = "dia-condicao";
        condicao.textContent =
            `Condição: ${descricaoClima(codigo)}`;

        card.append(titulo, conteudo, condicao);

        listaPrevisao.appendChild(card);
    }

    if (quantidade === 0) {

        listaPrevisao.textContent =
            "Previsão indisponível para esta cidade.";
    }
}


// ==========================================
// EXIBIR INFORMAÇÕES DO PAÍS - SOAP
// ==========================================

function exibirSoap(dados) {

    pais.textContent = dados.pais || "--";

    codigoISO.textContent =
        dados.codigoISO || "--";

    if (dados.sucesso) {

        capital.textContent =
            dados.capital || "--";

        xmlSoap.textContent =
            dados.xmlSoap ||
            "XML não informado pelo servidor.";

    } else {

        capital.textContent = "Indisponível";

        xmlSoap.textContent =
            dados.erro ||
            "Não foi possível consultar o Web Service SOAP.";
    }
}


// ==========================================
// CONSULTAR CIDADE
// ==========================================

async function consultarCidade() {

    const cidade = campoCidade.value.trim();

    if (!cidade) {

        mostrarMensagem(
            "Digite o nome de uma cidade para consultar.",
            true
        );

        campoCidade.focus();
        return;
    }

    botaoConsultar.disabled = true;
    botaoConsultar.textContent = "Consultando...";

    mostrarMensagem(
        "Consultando informações meteorológicas e dados do país..."
    );

    try {

        const url =
            `/api/clima?cidade=${encodeURIComponent(cidade)}`;

        const resposta = await fetch(url, {
            headers: {
                "Accept": "application/json"
            }
        });

        const dados = await resposta.json();

        if (!resposta.ok) {

            throw new Error(
                dados.erro ||
                "Não foi possível consultar a cidade."
            );
        }

        // Atualizar REST
        exibirClima(dados);
        exibirPrevisao(dados.previsao);

        // Atualizar SOAP
        exibirSoap(dados.soap);

        if (dados.soap.sucesso) {

            mostrarMensagem(
                "Consulta REST e SOAP realizada com sucesso!"
            );

        } else {

            mostrarMensagem(
                "Dados meteorológicos atualizados. " +
                "O serviço SOAP está indisponível.",
                true
            );
        }

    } catch (erro) {

        mostrarMensagem(
            "Erro na consulta: " + erro.message,
            true
        );

    } finally {

        botaoConsultar.disabled = false;

        botaoConsultar.textContent =
            "☁ Consultar";
    }
}


// ==========================================
// EVENTOS DO SISTEMA
// ==========================================

// Consultar ao clicar no botão
botaoConsultar.addEventListener(
    "click",
    consultarCidade
);

// Consultar ao pressionar Enter
campoCidade.addEventListener(
    "keydown",
    (evento) => {

        if (evento.key === "Enter") {
            consultarCidade();
        }
    }
);


// ==========================================
// INICIALIZAÇÃO
// ==========================================

// Não faz nenhuma consulta automaticamente.
// O usuário escolhe a cidade e clica em Consultar.
