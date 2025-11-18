import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe para desencriptação de mensagens cifradas com a Cifra de César
 * Alfabeto utilizado: [A-Z]
 */
public class CaesarCipher {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b(?=.*[AEIOUÁÉÍÓÚaeiouáéíóú])[\\p{L}]{2,}\\b");
    private static final Pattern VOWEL_PATTERN = Pattern.compile("[AEIOUaeiouÁÉÍÓÚáéíóú]");
    private static final Pattern IMPOSSIBLE_PATTERN = Pattern.compile("(?i)([qw])\\1+|([\\p{L}])\\2{2,}");

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
        System.out.println("(apresentando apenas resultados que cumprem os filtros)");
        System.out.println("----------------------------------------\n");

        String[] results = desencriptarTodosDeslocamentos(text);
        int exibidos = 0;

        for (int i = 0; i < results.length; i++) {
            String resultado = results[i];
            if (!textoValido(resultado)) {
                continue;
            }

            System.out.printf("Shift %2d: %s\n", i, resultado);
            exibidos++;
        }

        if (exibidos == 0) {
            System.out.println("Nenhum resultado cumpriu os filtros definidos.");
        }

        System.out.println("\nTotal mostrado: " + exibidos + " de 26 (após filtros)");
        System.out.println("\n========================================\n");
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
        System.out.println("(apresentando apenas combinações cujo texto da mensagem cumpre os filtros)");
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

                    if (!textoValido(resultado[1])) {
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
        System.out.println("Total analisado: " + totalTestado + " combinações");
        System.out.println("Total mostrado: " + contador + " combinações (após filtros)");
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
