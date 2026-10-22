package app.view;

import app.controller.TransacaoController;
import app.dao.CategoriaDAO;
import app.model.Categoria;
import app.model.Transacao;
import app.model.Usuario;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class TelaHistorico extends JPanel {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final Usuario usuario;
    private final TransacaoController controller = new TransacaoController();
    private final JTextField inicio = new JTextField(10);
    private final JTextField fim = new JTextField(10);
    private final JComboBox<String> tipo = new JComboBox<>(new String[]{"Todos", "Receita", "Despesa"});
    private final JComboBox<Object> categoria = new JComboBox<>();
    private final JLabel resultado = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Data", "Descrição", "Tipo", "Categoria", "Valor"}, 0) {
        @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    };

    TelaHistorico(Usuario usuario, Runnable voltarAoMenu) {
        super(new BorderLayout(10, 10));
        this.usuario = usuario;
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inicio.setName("inicio");
        fim.setName("fim");
        tipo.setName("tipo");
        categoria.setName("categoria");
        categoria.addItem("Todas");
        new CategoriaDAO().listarPorUsuario(usuario).forEach(categoria::addItem);

        JPanel filtros = new JPanel(new GridLayout(2, 4, 8, 8));
        filtros.add(new JLabel("De (dd/mm/aaaa)"));
        filtros.add(new JLabel("Até (dd/mm/aaaa)"));
        filtros.add(new JLabel("Tipo"));
        filtros.add(new JLabel("Categoria"));
        filtros.add(inicio);
        filtros.add(fim);
        filtros.add(tipo);
        filtros.add(categoria);
        JPanel topo = new JPanel(new BorderLayout(5, 5));
        topo.add(new JLabel("Datas em branco consultam todo o período. Os filtros podem ser combinados."), BorderLayout.NORTH);
        topo.add(filtros, BorderLayout.CENTER);
        JButton pesquisar = new JButton("Pesquisar");
        JButton limpar = new JButton("Limpar filtros");
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        acoes.add(pesquisar);
        acoes.add(limpar);
        topo.add(acoes, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        JTable tabela = new JTable(modelo);
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(24);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setColumnHeaderView(tabela.getTableHeader());
        add(rolagem, BorderLayout.CENTER);
        JButton voltar = new JButton("Voltar ao menu");
        voltar.addActionListener(e -> voltarAoMenu.run());
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(resultado, BorderLayout.CENTER);
        rodape.add(voltar, BorderLayout.EAST);
        add(rodape, BorderLayout.SOUTH);
        pesquisar.addActionListener(e -> pesquisar());
        inicio.addActionListener(e -> pesquisar());
        fim.addActionListener(e -> pesquisar());
        limpar.addActionListener(e -> {
            inicio.setText("");
            fim.setText("");
            tipo.setSelectedIndex(0);
            categoria.setSelectedIndex(0);
            pesquisar();
        });
        pesquisar();
    }

    private void pesquisar() {
        try {
            LocalDate dataInicio = lerData(inicio.getText());
            LocalDate dataFim = lerData(fim.getText());
            String tipoSelecionado = tipo.getSelectedIndex() == 0 ? null
                    : tipo.getSelectedItem().toString().toLowerCase(Locale.ROOT);
            Categoria categoriaSelecionada = categoria.getSelectedItem() instanceof Categoria c ? c : null;
            List<Transacao> transacoes = controller.buscarHistorico(usuario, dataInicio, dataFim,
                    tipoSelecionado, categoriaSelecionada);
            modelo.setRowCount(0);
            NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            for (Transacao t : transacoes) {
                modelo.addRow(new Object[]{t.getData() == null ? "—" : DATA.format(t.getData()),
                        t.getDescricao(), "receita".equals(t.getTipo()) ? "Receita" : "Despesa",
                        t.getCategoria() == null ? "Sem categoria" : t.getCategoria().getNome(),
                        moeda.format(t.getValor())});
            }
            resultado.setText(transacoes.isEmpty() ? "Nenhuma transação encontrada."
                    : transacoes.size() + " transação(ões) encontrada(s).");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Informe uma data válida no formato dd/mm/aaaa.",
                    "Confira as datas", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Confira os filtros", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Não foi possível consultar o histórico. Tente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static LocalDate lerData(String texto) {
        return texto.isBlank() ? null : LocalDate.parse(texto.strip(), DATA);
    }
}
