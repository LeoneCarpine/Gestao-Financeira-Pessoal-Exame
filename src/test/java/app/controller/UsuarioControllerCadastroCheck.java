package app.controller;

import app.model.Usuario;

/** Verificação executável com a unidade financeiroPU apontando para H2 em memória. */
public class UsuarioControllerCadastroCheck {
    private static int verificacoes;

    public static void main(String[] args) throws Exception {
        try (var config = UsuarioControllerCadastroCheck.class.getClassLoader()
                .getResourceAsStream("META-INF/persistence.xml")) {
            if (config == null || !new String(config.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                    .contains("jdbc:h2:mem:cadastro_check;")) {
                throw new IllegalStateException("Execute scripts/Test-Cadastro.ps1 para usar o banco isolado de testes.");
            }
        }
        UsuarioController controller = new UsuarioController();
        for (String nome : new String[]{null, "", "   "}) {
            rejeita(() -> controller.cadastrar(nome, "teste@example.com", "senha123"));
        }
        for (String email : new String[]{null, "", "leone", "a@", "a@@exemplo.com", "a b@exemplo.com"}) {
            rejeita(() -> controller.cadastrar("Leone", email, "senha123"));
        }
        for (String senha : new String[]{null, "", "        ", "1234567"}) {
            rejeita(() -> controller.cadastrar("Leone", "teste@example.com", senha));
        }
        // Se qualquer tentativa inválida tivesse gravado, este cadastro seria duplicado.
        controller.cadastrar("  Leone  ", " TESTE@Example.com ", "senha123");
        Usuario usuario = controller.login(" TESTE@example.com ", "senha123");
        confere("Leone".equals(usuario.getNome()), "Nome deve ser aparado");
        confere("teste@example.com".equals(usuario.getEmail()), "E-mail deve ser normalizado");
        rejeita(() -> controller.cadastrar("Outro", "Teste@EXAMPLE.COM", "senha456"));
        rejeita(() -> controller.login("teste@example.com", ""));
        rejeita(() -> controller.login("teste@example.com", "incorreta"));
        rejeita(() -> controller.login("ausente@example.com", "senha123"));
        rejeita(() -> controller.login("leone", ""));
        // A senha não deve ser aparada silenciosamente.
        controller.cadastrar("Maria", "maria@example.com", " senha123 ");
        confere(controller.login("maria@example.com", " senha123 ") != null, "Senha exata deve autenticar");
        rejeita(() -> controller.login("maria@example.com", "senha123"));
        System.out.println("OK: " + verificacoes + " verificações passaram.");
    }

    private static void rejeita(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException esperado) {
            verificacoes++;
            return;
        }
        throw new AssertionError("A operação deveria ter sido rejeitada");
    }

    private static void confere(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
        verificacoes++;
    }
}
