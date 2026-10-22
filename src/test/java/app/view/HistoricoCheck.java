package app.view;

import app.dao.*;
import app.model.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

/** Executar scripts/Test-Cadastro.ps1 -CheckClass app.view.HistoricoCheck. */
public class HistoricoCheck {
    private static int verificacoes;

    public static void main(String[] args) throws Exception {
        try (var config = HistoricoCheck.class.getClassLoader().getResourceAsStream("META-INF/persistence.xml")) {
            if (config == null || !new String(config.readAllBytes(), StandardCharsets.UTF_8)
                    .contains("jdbc:h2:mem:cadastro_check;")) throw new IllegalStateException("Use o banco isolado de testes.");
        }
        UsuarioDAO usuarios = new UsuarioDAO();
        Usuario u = usuario(usuarios, "Leone");
        Usuario outro = usuario(usuarios, "Outro");
        CategoriaDAO categorias = new CategoriaDAO();
        Categoria a = categoria(categorias, u, "Alimentação");
        Categoria b = categoria(categorias, u, "Salário");
        Categoria alheia = categoria(categorias, outro, "Privada");
        TransacaoDAO dao = new TransacaoDAO();
        transacao(dao, u, a, "2026-01-01", "despesa", "Mercado");
        transacao(dao, u, b, "2026-01-15", "receita", "Pagamento");
        transacao(dao, u, a, "2026-01-31", "despesa", "Almoço");
        transacao(dao, u, null, "2026-02-01", "despesa", "Registro antigo");
        transacao(dao, outro, alheia, "2026-01-15", "despesa", "Privada");
        LocalDate inicio = LocalDate.parse("2026-01-01");
        LocalDate fim = LocalDate.parse("2026-01-31");
        var todas = dao.filtrar(u, null, null, null, null);
        confere(todas.size() == 4, "Isolamento por usuário e inclusão de registros sem categoria");
        confere("Registro antigo".equals(todas.get(0).getDescricao()), "Ordem mais recente primeiro");
        confere(dao.filtrar(u, inicio, fim, null, null).size() == 3, "Datas inclusivas");
        confere(dao.filtrar(u, fim, null, null, null).size() == 2, "Somente data inicial");
        confere(dao.filtrar(u, null, inicio, null, null).size() == 1, "Somente data final");
        confere(dao.filtrar(u, inicio, inicio, null, null).size() == 1, "Pesquisa por um único dia");
        confere(dao.filtrar(u, null, null, "despesa", null).size() == 3, "Filtro por tipo");
        confere(dao.filtrar(u, null, null, null, a).size() == 2, "Filtro por categoria");
        confere(dao.filtrar(u, inicio, fim, "receita", b).size() == 1, "Todos os filtros combinados");
        confere(dao.filtrar(u, inicio, fim, "receita", a).isEmpty(), "Sem resultados");
        confere(dao.filtrar(u, null, null, null, alheia).isEmpty(), "Categoria de outro usuário não expõe dados");
        rejeita(() -> dao.filtrar(u, fim, inicio, null, null));
        rejeita(() -> dao.filtrar(null, null, null, null, null));
        rejeita(() -> dao.filtrar(u, null, null, "invalido", null));
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger voltas = new AtomicInteger();
            TelaHistorico painel = new TelaHistorico(u, voltas::incrementAndGet);
            JTable tabela = achar(painel, JTable.class, null);
            confere(tabela.getRowCount() == 4, "Histórico carregado ao abrir");
            confere("Sem categoria".equals(tabela.getValueAt(0, 3)), "Registro antigo deve ser legível");
            confere(!tabela.isCellEditable(0, 0), "Histórico não deve editar dados acidentalmente");
            achar(painel, JTextField.class, "inicio").setText("01/01/2026");
            achar(painel, JTextField.class, "fim").setText("31/01/2026");
            achar(painel, JComboBox.class, "tipo").setSelectedItem("Receita");
            JComboBox<?> combo = achar(painel, JComboBox.class, "categoria");
            confere(combo.getItemCount() == 3, "Somente categorias do usuário");
            for (int i = 1; i < combo.getItemCount(); i++) {
                if (((Categoria)combo.getItemAt(i)).getId().equals(b.getId())) combo.setSelectedIndex(i);
            }
            botao(painel, "Pesquisar").doClick();
            confere(tabela.getRowCount() == 1 && "Pagamento".equals(tabela.getValueAt(0, 1)), "Pesquisa da interface");
            botao(painel, "Limpar filtros").doClick();
            confere(tabela.getRowCount() == 4 && achar(painel, JTextField.class, "inicio").getText().isEmpty(), "Limpar restaura histórico");
            botao(painel, "Voltar ao menu").doClick();
            confere(voltas.get() == 1, "Retorno ao menu");
            if (args.length > 0) {
                painel.setSize(760, 460);
                layout(painel);
                var imagem = new java.awt.image.BufferedImage(760, 460, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = imagem.createGraphics();
                painel.printAll(g);
                g.dispose();
                try { javax.imageio.ImageIO.write(imagem, "png", new java.io.File(args[0])); }
                catch (java.io.IOException ex) { throw new RuntimeException(ex); }
            }
        });
        System.out.println("OK: " + verificacoes + " verificações do histórico passaram.");
    }

    private static void layout(Container c) { c.doLayout(); for (Component f : c.getComponents()) if (f instanceof Container child) layout(child); }
    private static Usuario usuario(UsuarioDAO dao, String nome) {
        Usuario u = new Usuario(); u.setNome(nome); u.setEmail(nome + "@example.com"); u.setSenha("senha123"); dao.salvar(u); return u;
    }
    private static Categoria categoria(CategoriaDAO dao, Usuario u, String nome) {
        Categoria c = new Categoria(); c.setNome(nome); c.setUsuario(u); dao.salvar(c); return c;
    }
    private static void transacao(TransacaoDAO dao, Usuario u, Categoria c, String data, String tipo, String descricao) {
        Transacao t = new Transacao(); t.setUsuario(u); t.setCategoria(c); t.setData(LocalDate.parse(data));
        t.setTipo(tipo); t.setDescricao(descricao); t.setValor(100.50); dao.salvar(t);
    }
    private static <T extends Component> T achar(Container raiz, Class<T> classe, String nome) {
        for (Component c : raiz.getComponents()) {
            if (classe.isInstance(c) && (nome == null || nome.equals(c.getName()))) return classe.cast(c);
            if (c instanceof Container child) { T achado = achar(child, classe, nome); if (achado != null) return achado; }
        }
        return null;
    }
    private static JButton botao(Container raiz, String texto) {
        for (Component c : raiz.getComponents()) {
            if (c instanceof JButton b && texto.equals(b.getText())) return b;
            if (c instanceof Container child) { JButton b = botao(child, texto); if (b != null) return b; }
        }
        return null;
    }
    private static void rejeita(Runnable acao) {
        try { acao.run(); } catch (IllegalArgumentException esperado) { verificacoes++; return; }
        throw new AssertionError("Consulta inválida deveria falhar");
    }
    private static void confere(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem); verificacoes++;
    }
}
