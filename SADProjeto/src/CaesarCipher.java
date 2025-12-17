import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe para desencriptação de mensagens cifradas com a Cifra de César
 * Alfabeto utilizado: [A-Z]
 */
public class CaesarCipher {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b(?=.*[AEIOUÁÉÍÓÚaeiouáéíóú])[\\p{L}]{2,}\\b");
    private static final Pattern VOWEL_PATTERN = Pattern.compile("[AEIOUaeiouÁÉÍÓÚáéíóú]");
    private static final Pattern IMPOSSIBLE_PATTERN = Pattern.compile(
            "(?iu)(?:k{2}|w{2}|y{2}|q{2}|j{2}|([\\p{L}])\\1{2,}|[bcdfghjklmnpqrstvwxyz]{4,})");
    private static final String SALT_CHARS = "!#$%&+-<=>@";
    private static final Pattern SALT_PATTERN = Pattern.compile("[" + Pattern.quote(SALT_CHARS) + "]{1,4}");
    private static boolean filtrosAtivos = true;

    public static class ResultadoDesencriptacao {
        private final String mensagem;
        private final String pepper;
        private final String salt;
        private final boolean saltNoInicio;

        public ResultadoDesencriptacao(String mensagem, String pepper, String salt, boolean saltNoInicio) {
            this.mensagem = mensagem;
            this.pepper = pepper;
            this.salt = salt;
            this.saltNoInicio = saltNoInicio;
        }

        public String getMensagem() {
            return mensagem;
        }

        public String getPepper() {
            return pepper;
        }

        public String getSalt() {
            return salt;
        }

        public boolean isSaltNoInicio() {
            return saltNoInicio;
        }
    }

    public static boolean filtrosAtivos() {
        return filtrosAtivos;
    }

    public static void definirFiltrosAtivos(boolean ativos) {
        filtrosAtivos = ativos;
    }

    /**
     * Desencripta uma mensagem usando um deslocamento específico
     * @param text Texto cifrado
     * @param shift Valor do deslocamento (0-25)
     * @return Texto desencriptado com o deslocamento especificado
     */
    public static String desencriptarComDeslocamento(String text, int shift) {
        StringBuilder result = new StringBuilder();

        // Normalizar o shift para estar entre 0-25
        shift = shift % 26;

        // Processar cada caractere
        for (char c : text.toCharArray()) {
            // Verificar se é uma letra
            if (Character.isLetter(c)) {
                // Converter para maiúscula
                char upperChar = Character.toUpperCase(c);

                // Desencriptar: deslocar para trás
                char decrypted = (char) ((upperChar - 'A' - shift + 26) % 26 + 'A');

                result.append(decrypted);
            } else {
                // Manter caracteres não-alfabéticos inalterados
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Desencripta uma mensagem testando todos os 26 deslocamentos possíveis
     * @param text Texto cifrado
     * @return Array com todas as 26 possibilidades de desencriptação
     */
    public static String[] desencriptarTodosDeslocamentos(String text) {
        String[] results = new String[26];

        // Tentar cada deslocamento de 0 a 25
        for (int shift = 0; shift < 26; shift++) {
            results[shift] = desencriptarComDeslocamento(text, shift);
        }

        return results;
    }

    /**
     * Apresenta todas as possibilidades de desencriptação formatadas
     * @param text Texto cifrado original
     */
    public static void apresentarTodasDesencriptacoes(String text) {
        System.out.println("\n========================================");
        System.out.println("TODAS AS POSSIBILIDADES DE DESENCRIPTAÇÃO");
        System.out.println("========================================");
        System.out.println("Mensagem cifrada: " + text);
        if (filtrosAtivos) {
            System.out.println("(apresentando apenas resultados que cumprem os filtros)");
        } else {
            System.out.println("(filtros desativados - todas as hipóteses são listadas)");
        }
        System.out.println("----------------------------------------\n");

        String[] results = desencriptarTodosDeslocamentos(text);
        int exibidos = 0;

        for (int i = 0; i < results.length; i++) {
            String resultado = results[i];
            if (filtrosAtivos && !textoValido(resultado)) {
                continue;
            }

            System.out.printf("Shift %2d: %s\n", i, resultado);
            exibidos++;
        }

        if (filtrosAtivos && exibidos == 0) {
            System.out.println("Nenhum resultado cumpriu os filtros definidos.");
        }

        if (filtrosAtivos) {
            System.out.println("\nTotal mostrado: " + exibidos + " de 26 (após filtros)");
        } else {
            System.out.println("\nTotal mostrado: " + exibidos + " de 26");
        }
        System.out.println("\n========================================\n");
    }

    public static List<ResultadoDesencriptacao> desencriptarHashComParametros(String hashBase64,
                                                                             String alfabetoSubstituicao,
                                                                             int deslocacao,
                                                                             String pepper,
                                                                             String salt,
                                                                             Boolean saltNoInicio) {
        if (hashBase64 == null || hashBase64.isEmpty()) {
            throw new IllegalArgumentException("Hash Base64 inválida");
        }

        if (alfabetoSubstituicao == null || alfabetoSubstituicao.length() != 26) {
            throw new IllegalArgumentException("O alfabeto de substituição deve ter 26 letras");
        }

        String textoCodificado;
        try {
            byte[] dados = Base64.getDecoder().decode(hashBase64);
            textoCodificado = new String(dados, StandardCharsets.ISO_8859_1);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Hash Base64 inválida", e);
        }

        String aposCaesar = desencriptarComDeslocamento(textoCodificado, deslocacao);
        String aposSubstituicao = desfazerSubstituicao(aposCaesar, alfabetoSubstituicao);
        List<String> peppers = determinarPepper(aposSubstituicao, pepper);
        List<ResultadoDesencriptacao> resultados = new ArrayList<>();

        for (String pepperAtual : peppers) {
            String restante = aposSubstituicao.substring(pepperAtual.length());
            for (SaltOption opcao : determinarSalt(restante, salt, saltNoInicio)) {
                resultados.add(new ResultadoDesencriptacao(opcao.mensagem(), pepperAtual, opcao.salt(), opcao.saltNoInicio()));
            }
        }

        if (resultados.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível determinar Pepper/Salt válidos para o texto desencriptado");
        }

        return resultados;
    }

    private static String desfazerSubstituicao(String texto, String alfabetoSubstituicao) {
        int[] inverso = construirMapaInverso(alfabetoSubstituicao);
        StringBuilder sb = new StringBuilder(texto.length());

        for (char c : texto.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                int indice = inverso[c - 'A'];
                if (indice < 0) {
                    throw new IllegalArgumentException("Caractere '" + c + "' não pertence ao alfabeto de substituição");
                }
                sb.append((char) ('A' + indice));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static int[] construirMapaInverso(String alfabetoSubstituicao) {
        int[] inverso = new int[26];
        for (int i = 0; i < inverso.length; i++) {
            inverso[i] = -1;
        }

        for (int i = 0; i < alfabetoSubstituicao.length(); i++) {
            char c = Character.toUpperCase(alfabetoSubstituicao.charAt(i));
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("O alfabeto de substituição só pode conter letras maiúsculas");
            }
            int idx = c - 'A';
            if (inverso[idx] != -1) {
                throw new IllegalArgumentException("Caractere duplicado no alfabeto de substituição: " + c);
            }
            inverso[idx] = i;
        }

        return inverso;
    }

    private record SaltOption(String mensagem, String salt, boolean saltNoInicio) {}

    private static List<String> determinarPepper(String texto, String pepperInformado) {
        List<String> peppers = new ArrayList<>();

        if (pepperInformado != null && !pepperInformado.isEmpty()) {
            if (!pepperInformado.matches("\\d{1,2}")) {
                throw new IllegalArgumentException("Pepper inválido. Use 1 ou 2 dígitos.");
            }
            if (!texto.startsWith(pepperInformado)) {
                throw new IllegalArgumentException("Pepper fornecido não corresponde ao texto desencriptado");
            }
            peppers.add(pepperInformado);
            return peppers;
        }

        int max = Math.min(2, texto.length());
        int len = 0;
        while (len < max && Character.isDigit(texto.charAt(len))) {
            len++;
        }

        if (len == 0) {
            peppers.add("");
        } else {
            for (int i = 1; i <= len; i++) {
                peppers.add(texto.substring(0, i));
            }
        }

        return peppers;
    }

    private static List<SaltOption> determinarSalt(String texto,
                                                   String saltInformado,
                                                   Boolean saltNoInicioInformado) {
        List<SaltOption> salts = new ArrayList<>();

        if (saltInformado != null && !saltInformado.isEmpty()) {
            if (!SALT_PATTERN.matcher(saltInformado).matches()) {
                throw new IllegalArgumentException("Salt inválido. Use apenas caracteres permitidos e máximo 4.");
            }

            if (Boolean.TRUE.equals(saltNoInicioInformado)) {
                if (!texto.startsWith(saltInformado)) {
                    throw new IllegalArgumentException("Salt fornecido não corresponde ao início do texto");
                }
                salts.add(new SaltOption(texto.substring(saltInformado.length()), saltInformado, true));
            } else if (Boolean.FALSE.equals(saltNoInicioInformado)) {
                if (!texto.endsWith(saltInformado)) {
                    throw new IllegalArgumentException("Salt fornecido não corresponde ao fim do texto");
                }
                salts.add(new SaltOption(texto.substring(0, texto.length() - saltInformado.length()), saltInformado, false));
            } else {
                boolean encontrado = false;
                if (texto.startsWith(saltInformado)) {
                    salts.add(new SaltOption(texto.substring(saltInformado.length()), saltInformado, true));
                    encontrado = true;
                }
                if (texto.endsWith(saltInformado)) {
                    salts.add(new SaltOption(texto.substring(0, texto.length() - saltInformado.length()), saltInformado, false));
                    encontrado = true;
                }
                if (!encontrado) {
                    throw new IllegalArgumentException("Salt fornecido não corresponde ao início nem ao fim do texto");
                }
            }

            return salts;
        }

        LinkedHashSet<SaltOption> conjunto = new LinkedHashSet<>();

        String prefixo = extrairSaltInicio(texto);
        if (!prefixo.isEmpty()) {
            conjunto.add(new SaltOption(texto.substring(prefixo.length()), prefixo, true));
        }

        String sufixo = extrairSaltFim(texto);
        if (!sufixo.isEmpty()) {
            conjunto.add(new SaltOption(texto.substring(0, texto.length() - sufixo.length()), sufixo, false));
        }

        if (conjunto.isEmpty()) {
            conjunto.add(new SaltOption(texto, "", false));
        }

        salts.addAll(conjunto);
        return salts;
    }

    private static String extrairSaltInicio(String texto) {
        int limite = Math.min(4, texto.length());
        StringBuilder candidato = new StringBuilder();
        for (int j = 0; j < limite; j++) {
            char c = texto.charAt(j);
            if (SALT_CHARS.indexOf(c) < 0) {
                break;
            }
            candidato.append(c);
        }
        return candidato.toString();
    }

    private static String extrairSaltFim(String texto) {
        int limite = Math.min(4, texto.length());
        StringBuilder candidato = new StringBuilder();
        for (int j = 1; j <= limite; j++) {
            char c = texto.charAt(texto.length() - j);
            if (SALT_CHARS.indexOf(c) < 0) {
                break;
            }
            candidato.insert(0, c);
        }
        return candidato.toString();
    }

    public static String[] desencriptarComSalts(String cipherText,
                                                int shiftSalt1,
                                                int shiftMessage,
                                                int shiftSalt2) {
        // Separar as 3 partes
        String salt1Cifrado = cipherText.substring(0, 3);
        String salt2Cifrado = cipherText.substring(cipherText.length() - 3);
        String mensagemCifrada = cipherText.substring(3, cipherText.length() - 3);

        // Desencriptar cada parte
        String salt1 = desencriptarComDeslocamento(salt1Cifrado, shiftSalt1);
        String mensagem = desencriptarComDeslocamento(mensagemCifrada, shiftMessage);
        String salt2 = desencriptarComDeslocamento(salt2Cifrado, shiftSalt2);

        // Retornar as 3 partes
        return new String[]{salt1, mensagem, salt2};
    }

    public static void desencriptarTodasCombinacoes(String cipherText) {
        // Validação
        if (cipherText.length() < 7) {
            System.out.println("\n[ERRO] A mensagem deve ter pelo menos 7 caracteres!");
            System.out.println("       (3 para Salt1 + 1 para mensagem + 3 para Salt2)");
            return;
        }

        // Cabeçalho
        System.out.println("\n" + "=".repeat(100));
        System.out.println("TODAS AS COMBINAÇÕES DE DESENCRIPTAÇÃO COM SALTS");
        System.out.println("=".repeat(100));
        System.out.println("Mensagem cifrada: " + cipherText);
        System.out.println("Total de combinações: 17,576 (26 x 26 x 26)");
        if (filtrosAtivos) {
            System.out.println("(apresentando apenas combinações cujo texto da mensagem cumpre os filtros)");
        } else {
            System.out.println("(filtros desativados - todas as combinações serão mostradas)");
        }
        System.out.println("-".repeat(100));
        System.out.printf("%-6s | %-18s | %-30s | %-18s\n",
                "  Nº  ", "Salt1 [shift]", "Mensagem [shift]", "Salt2 [shift]");
        System.out.println("-".repeat(100));

        int contador = 0;
        int totalTestado = 0;

        // Loop triplo: testar todos os shifts
        for (int shiftSalt1 = 0; shiftSalt1 < 26; shiftSalt1++) {
            for (int shiftMsg = 0; shiftMsg < 26; shiftMsg++) {
                for (int shiftSalt2 = 0; shiftSalt2 < 26; shiftSalt2++) {
                    totalTestado++;

                    // Desencriptar com esta combinação
                    String[] resultado = desencriptarComSalts(cipherText, shiftSalt1, shiftMsg, shiftSalt2);

                    if (filtrosAtivos && !textoValido(resultado[1])) {
                        continue;
                    }

                    contador++;

                    // Mostrar resultado
                    System.out.printf("%6d | %-3s [shift=%2d] | %-20s [shift=%2d] | %-3s [shift=%2d]\n",
                            contador,
                            resultado[0], shiftSalt1,
                            resultado[1], shiftMsg,
                            resultado[2], shiftSalt2);
                }
            }
        }

        System.out.println("=".repeat(100));
        if (filtrosAtivos) {
            System.out.println("Total analisado: " + totalTestado + " combinações");
            System.out.println("Total mostrado: " + contador + " combinações (após filtros)");
        } else {
            System.out.println("Filtros desativados: " + contador + " combinações foram apresentadas.");
        }
        System.out.println("=".repeat(100));
    }

    private static boolean textoValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        Matcher palavraMatcher = WORD_PATTERN.matcher(texto);
        boolean temPalavraReal = false;
        while (palavraMatcher.find()) {
            String palavra = palavraMatcher.group();
            if (VOWEL_PATTERN.matcher(palavra).find()) {
                temPalavraReal = true;
                break;
            }
        }
        if (!temPalavraReal) {
            return false;
        }

        if (!VOWEL_PATTERN.matcher(texto).find()) {
            return false;
        }

        if (IMPOSSIBLE_PATTERN.matcher(texto).find()) {
            return false;
        }

        return true;
    }

}
