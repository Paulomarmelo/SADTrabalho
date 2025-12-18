import java.util.List;
import java.util.Scanner;

public class Menu {
    private Scanner scanner;

    /**
     * Construtor - inicializa o Scanner
     */
    public Menu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Inicia o menu principal do programa
     */
    public void iniciar() {
        int opcao;

        do {
            mostrarMenu();
            opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    executarAlinea1();
                    break;
                case 2:
                    executarAlinea2();
                    break;
                case 3:
                    desencriptarHashComParametros();
                    break;
                case 4:
                    alternarFiltros();
                    break;
                case 0:
                    System.out.println("\nA sair do programa...");
                    System.out.println("Até breve!");
                    break;
                default:
                    System.out.println("\nOpção inválida! Tente novamente.");
            }

        } while (opcao != 0);

        scanner.close();
    }

    /**
     * Apresenta o menu principal
     */
    private void mostrarMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   DESENCRIPTAÇÃO - CIFRA DE CÉSAR     ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("  1 - Alínea 1 (Caesar simples)");
        System.out.println("  2 - Alínea 2 (Caesar com Salts)");
        System.out.println("  3 - Desencriptar hash com parâmetros");
        boolean filtrosAtivos = CaesarCipher.filtrosAtivos();
        System.out.println("  4 - " + (filtrosAtivos ? "Desativar filtros de resultados" : "Ativar filtros de resultados"));
        System.out.println("  0 - Sair");
        System.out.println("─────────────────────────────────────────");
        System.out.print("Escolha uma opção: ");
    }

    /**
     * Lê e valida a opção escolhida pelo utilizador
     * @return Opção escolhida
     */
    private int lerOpcao() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Limpar buffer
            return -1; // Retorna opção inválida
        } finally {
            scanner.nextLine(); // Consumir a quebra de linha
        }
    }

    /**
     * Executa a funcionalidade da Alínea 1
     * Desencripta mensagens usando a Cifra de César (todos os deslocamentos)
     */
    private void executarAlinea1() {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│         ALÍNEA 1 - DESENCRIPTAR        │");
        System.out.println("└────────────────────────────────────────┘");

        System.out.print("Introduza a mensagem cifrada: ");
        String mensagemCifrada = scanner.nextLine();

        if (mensagemCifrada == null || mensagemCifrada.trim().isEmpty()) {
            System.out.println("\nErro: A mensagem não pode estar vazia!");
            return;
        }

        // Desencriptar e mostrar todas as possibilidades
        CaesarCipher.apresentarTodasDesencriptacoes(mensagemCifrada);

        System.out.println("Prima ENTER para voltar ao menu...");
        scanner.nextLine();
    }

    /**
     * Executa a funcionalidade da Alínea 2
     * Desencripta mensagens com dois Salts (um no início e outro no fim)
     */
    private void executarAlinea2() {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│    ALÍNEA 2 - CAESAR COM SALTS        │");
        System.out.println("└────────────────────────────────────────┘");

        System.out.print("Introduza a mensagem cifrada: ");
        String mensagemCifrada = scanner.nextLine();

        if (mensagemCifrada == null || mensagemCifrada.trim().isEmpty()) {
            System.out.println("\nErro: A mensagem não pode estar vazia!");
            return;
        }

        if (mensagemCifrada.length() < 7) {
            System.out.println("\nErro: A mensagem deve ter pelo menos 7 caracteres!");
            System.out.println("      (3 para Salt1 + 1 para mensagem + 3 para Salt2)");
            return;
        }

        // Desencriptar e mostrar todas as combinações
        CaesarCipher.desencriptarTodasCombinacoes(mensagemCifrada);

        System.out.println("Prima ENTER para voltar ao menu...");
        scanner.nextLine();
    }

    /**
     * Desencripta hash com parâmetros fornecidos pelo utilizador
     */
    private void desencriptarHashComParametros() {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│ DESENCRIPTAR HASH COM PARÂMETROS      │");
        System.out.println("└────────────────────────────────────────┘");

        System.out.print("Hash em Base64: ");
        String hash = scanner.nextLine().trim();
        if (hash.isEmpty()) {
            System.out.println("\n[ERRO] A hash não pode estar vazia.");
            return;
        }

        System.out.print("Alfabeto de substituição (26 letras em MAIÚSCULAS): ");
        String alfabeto = scanner.nextLine().trim();
        if (alfabeto.length() != 26) {
            System.out.println("\n[ERRO] O alfabeto de substituição deve ter exatamente 26 letras.");
            return;
        }

        System.out.print("Deslocação de César (0-25): ");
        String deslocamentoStr = scanner.nextLine().trim();
        int deslocamento;
        try {
            deslocamento = Integer.parseInt(deslocamentoStr);
        } catch (NumberFormatException e) {
            System.out.println("\n[ERRO] Deslocação inválida.");
            return;
        }

        System.out.print("Pepper (0-99, máximo 2 dígitos, deixar vazio se desconhecido): ");
        String pepper = scanner.nextLine().trim();
        if (!pepper.isEmpty() && !pepper.matches("\\d{1,2}")) {
            System.out.println("\n[ERRO] O pepper deve ter 1 ou 2 dígitos.");
            return;
        }

        System.out.print("Salt (máx 4 caracteres de {!#$%&+-<=>@}, deixar vazio se desconhecido): ");
        String salt = scanner.nextLine().trim();
        if (!salt.isEmpty() && !salt.matches("[!#$%&+\\-<=>@]{1,4}")) {
            System.out.println("\n[ERRO] Salt inválido. Use apenas caracteres permitidos e máximo 4.");
            return;
        }

        Boolean saltNoInicio = null;
        if (!salt.isEmpty()) {
            System.out.print("Salt está no início? (S/N/ENTER para desconhecido): ");
            String saltPosicao = scanner.nextLine().trim().toUpperCase();
            if (saltPosicao.equals("S")) {
                saltNoInicio = true;
            } else if (saltPosicao.equals("N")) {
                saltNoInicio = false;
            } else if (!saltPosicao.isEmpty()) {
                System.out.println("\n[ERRO] Resposta inválida. Indique 'S', 'N' ou deixe em branco.");
                return;
            }
        }

        try {
            List<CaesarCipher.ResultadoDesencriptacao> resultados = CaesarCipher.desencriptarHashComParametros(
                    hash, alfabeto, deslocamento, pepper, salt, saltNoInicio);

            System.out.println("\nResultados possíveis: ");
            for (int i = 0; i < resultados.size(); i++) {
                CaesarCipher.ResultadoDesencriptacao r = resultados.get(i);
                System.out.printf("%2d) Mensagem: %s | Pepper: %s | Salt: %s (%s)\n",
                        i + 1,
                        r.getMensagem(),
                        r.getPepper().isEmpty() ? "(vazio)" : r.getPepper(),
                        r.getSalt().isEmpty() ? "(vazio)" : r.getSalt(),
                        r.isSaltNoInicio() ? "início" : "fim");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERRO] " + e.getMessage());
        }

        System.out.println("\nPrima ENTER para voltar ao menu...");
        scanner.nextLine();
    }

    private void alternarFiltros() {
        boolean estadoAtual = CaesarCipher.filtrosAtivos();
        CaesarCipher.definirFiltrosAtivos(!estadoAtual);

        System.out.println("\n┌────────────────────────────────────────┐");
        if (estadoAtual) {
            System.out.println("│    Filtros de resultados DESATIVADOS    │");
        } else {
            System.out.println("│     Filtros de resultados ATIVADOS      │");
        }
        System.out.println("└────────────────────────────────────────┘");
        System.out.println("Prima ENTER para continuar...");
        scanner.nextLine();
    }
}
