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
        System.out.println("  1 - Alínea 1");
        System.out.println("  2 - Alínea 2");
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
        CaesarCipher.displayAllDecryptions(mensagemCifrada);

        System.out.println("Prima ENTER para voltar ao menu...");
        scanner.nextLine();
    }

    /**
     * Executa a funcionalidade da Alínea 2
     * (Ainda não implementada)
     */
    private void executarAlinea2() {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│    ALÍNEA 2 - DESENCRIPTAR COM SALTS   │");
        System.out.println("└────────────────────────────────────────┘");

        System.out.println("\nEstrutura: [Salt1: 3 letras] + [Mensagem] + [Salt2: 3 letras]");
        System.out.println("Cada parte foi cifrada com um deslocamento diferente.\n");

        System.out.print("Introduza a mensagem cifrada: ");
        String mensagemCifrada = scanner.nextLine();

        // Validações
        if (mensagemCifrada == null || mensagemCifrada.trim().isEmpty()) {
            System.out.println("\n[ERRO] A mensagem não pode estar vazia!");
            System.out.println("\nPrima ENTER para voltar ao menu...");
            scanner.nextLine();
            return;
        }

        if (mensagemCifrada.length() < 7) {
            System.out.println("\n[ERRO] A mensagem deve ter pelo menos 7 caracteres!");
            System.out.println("        (3 para Salt1 + 1 para mensagem + 3 para Salt2)");
            System.out.println("\nPrima ENTER para voltar ao menu...");
            scanner.nextLine();
            return;
        }

        // Desencriptar todas as combinações
        CaesarCipher.decryptAllCombinations(mensagemCifrada);

        System.out.println("\nPrima ENTER para voltar ao menu...");
        scanner.nextLine();
    }
}
