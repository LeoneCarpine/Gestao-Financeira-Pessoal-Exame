package app.view;

import app.model.Usuario;

import javax.swing.*;
import java.awt.*;

class TelaPrincipal extends JFrame {
    private final Usuario usuario;
    private final JPanel menu = new JPanel(new GridLayout(5, 1, 10, 10));

    public TelaPrincipal(Usuario usuario) {
        super("Painel - " + usuario.getNome());
        this.usuario = usuario;

        JButton btnTransacoes = new JButton("Registrar Transação");
        JButton btnResumo = new JButton("Resumo Financeiro");
        JButton btnHistorico = new JButton("Histórico de Transações");
        JButton btnCategorias = new JButton("Categorias");
        JButton btnSair = new JButton("Sair");

        JPanel panel = menu;
        panel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        panel.add(btnTransacoes);
        panel.add(btnHistorico);
        panel.add(btnResumo);
        panel.add(btnCategorias);
        panel.add(btnSair);

        btnTransacoes.addActionListener(e -> mostrarTela(
                new TelaTransacao(usuario, this::mostrarMenu), "Registrar Transação"));
        btnHistorico.addActionListener(e -> mostrarTela(
                new TelaHistorico(usuario, this::mostrarMenu), "Histórico de Transações"));
        btnResumo.addActionListener(e -> mostrarTela(
                new TelaResumoFinanceiro(usuario, this::mostrarMenu), "Resumo Financeiro"));
        btnCategorias.addActionListener(e -> mostrarTela(
                new TelaCategorias(usuario, this::mostrarMenu), "Categorias"));
        btnSair.addActionListener(e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });

        mostrarMenu();
        setMinimumSize(new Dimension(660, 440));
        setSize(780, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void mostrarMenu() {
        mostrarTela(menu, "Menu");
    }

    private void mostrarTela(JPanel tela, String titulo) {
        setTitle("Gestão Financeira - " + titulo);
        setContentPane(tela);
        revalidate();
        repaint();
    }
}

