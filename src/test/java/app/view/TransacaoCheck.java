package app.view;

import app.controller.TransacaoController;
import app.dao.CategoriaDAO;
import app.dao.TransacaoDAO;
import app.dao.UsuarioDAO;
import app.model.Categoria;
import app.model.Usuario;
import java.awt.Component;
import java.awt.Container;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

/** Executar via scripts/Test-Cadastro.ps1 -CheckClass app.view.TransacaoCheck. */
public class TransacaoCheck {
    private static int verificacoes;

    public static void main(String[] args) throws Exception {
        try (var config = TransacaoCheck.class.getClassLoader().getResourceAsStream("META-INF/persistence.xml")) {
            if (config == null || !new String(config.readAllBytes(), StandardCharsets.UTF_8)
                    .contains("jdbc:h2:mem:cadastro_check;")) {
                throw new IllegalStateException("Use o script de testes com banco em memória.");
            }
        }
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setEmail("teste@example.com");
        usuario.setSenha("senha123");
        new UsuarioDAO().salvar(usuario);
        TransacaoController controller = new TransacaoController();
        TransacaoDAO transacoes = new TransacaoDAO();
        rejeita(() -> controller.registrarTransacao(10, "Teste", "despesa", LocalDate.now(), usuario, null));
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        categoria.setUsuario(usuario);
        rejeita(() -> controller.registrarTransacao(10, "Teste", "despesa", LocalDate.now(), usuario, categoria));
        confere(transacoes.listarPorUsuario(usuario).isEmpty(), "Tentativas inválidas não podem gravar");
        AtomicInteger voltas = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            TelaTransacao painel = new TelaTransacao(usuario, voltas::incrementAndGet);
            confere(!botao(painel, "Salvar").isEnabled(), "Salvar deve ficar desabilitado sem categorias");
            botao(painel, "Cancelar").doClick();
            confere(voltas.get() == 1, "Cancelar deve retornar ao menu");
        });
        new CategoriaDAO().salvar(categoria);
        Usuario outro = new Usuario();
        outro.setId(usuario.getId() + 1);
        rejeita(() -> controller.registrarTransacao(10, "Teste", "despesa", LocalDate.now(), outro, categoria));
        controller.registrarTransacao(10, "Teste", "despesa", LocalDate.now(), usuario, categoria);
        confere(transacoes.listarPorUsuario(usuario).size() == 1, "Transação válida deve ser gravada");
        SwingUtilities.invokeAndWait(() -> {
            TelaTransacao painel = new TelaTransacao(usuario, voltas::incrementAndGet);
            confere(botao(painel, "Salvar").isEnabled(), "Categorias novas devem ser carregadas ao reabrir");
            confere(comboCategorias(painel).getItemCount() == 1, "Categoria deve aparecer na seleção");
            botao(painel, "Cancelar").doClick();
            botao(new TelaCategorias(usuario, voltas::incrementAndGet), "Voltar ao menu").doClick();
            botao(new TelaResumoFinanceiro(usuario, voltas::incrementAndGet), "Voltar ao menu").doClick();
            confere(voltas.get() == 4, "Todos os painéis devem permitir voltar");
        });
        confere(transacoes.listarPorUsuario(usuario).size() == 1, "Cancelar não deve salvar");
        System.out.println("OK: " + verificacoes + " verificações de transação e navegação passaram.");
    }

    private static JButton botao(Container raiz, String texto) {
        for (Component componente : raiz.getComponents()) {
            if (componente instanceof JButton b && texto.equals(b.getText())) return b;
            if (componente instanceof Container c) {
                JButton encontrado = botao(c, texto);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private static JComboBox<?> comboCategorias(Container raiz) {
        for (Component componente : raiz.getComponents()) {
            if (componente instanceof JComboBox<?> c && c.getItemCount() > 0
                    && c.getItemAt(0) instanceof Categoria) return c;
            if (componente instanceof Container c) {
                JComboBox<?> encontrado = comboCategorias(c);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private static void rejeita(Runnable acao) {
        try { acao.run(); }
        catch (IllegalArgumentException esperado) { verificacoes++; return; }
        throw new AssertionError("A transação deveria ter sido rejeitada");
    }

    private static void confere(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
        verificacoes++;
    }
}
