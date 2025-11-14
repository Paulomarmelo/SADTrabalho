/**
 * Classe para desencriptação de mensagens cifradas com a Cifra de César
 * Alfabeto utilizado: [A-Z]
 */
public class CaesarCipher {

    /**
     * Desencripta uma mensagem usando um deslocamento específico
     * @param text Texto cifrado
     * @param shift Valor do deslocamento (0-25)
     * @return Texto desencriptado com o deslocamento especificado
     */
    public static String decryptWithShift(String text, int shift) {
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
    public static String[] decryptAllShifts(String text) {
        String[] results = new String[26];

        // Tentar cada deslocamento de 0 a 25
        for (int shift = 0; shift < 26; shift++) {
            results[shift] = decryptWithShift(text, shift);
        }

        return results;
    }

    /**
     * Apresenta todas as possibilidades de desencriptação formatadas
     * @param text Texto cifrado original
     */
    public static void displayAllDecryptions(String text) {
        System.out.println("\n========================================");
        System.out.println("TODAS AS POSSIBILIDADES DE DESENCRIPTAÇÃO");
        System.out.println("========================================");
        System.out.println("Mensagem cifrada: " + text);
        System.out.println("----------------------------------------\n");

        String[] results = decryptAllShifts(text);

        for (int i = 0; i < results.length; i++) {
            System.out.printf("Shift %2d: %s\n", i, results[i]);
        }

        System.out.println("\n========================================\n");
    }
}
