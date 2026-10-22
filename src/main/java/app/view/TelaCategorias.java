package app.view;


import app.dao.CategoriaDAO;
import app.model.Categoria;
import app.model.Usuario;

import javax.swing.*;
import java.awt.*;


class TelaCategorias extends JPanel {
    private final Usuario usuario;
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final DefaultListModel<Categoria> modeloLista = new DefaultListModel<>();
    private final JList<Categoria> listaCategorias = new JList<>(modeloLista);

    public TelaCategorias(Usuario usuario, Runnable voltarAoMenu) {
        super(new BorderLayout(10, 10));
        this.usuario = usuario;

        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnVoltar = new JButton("Voltar ao menu");
        btnVoltar.addActionListener(e -> voltarAoMenu.run());

        JPanel botoes = new JPanel();
        botoes.add(btnAdicionar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnVoltar);

        add(new JScrollPane(listaCategorias), BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        carregarCategorias();

        btnAdicionar.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog("Nova categoria:");
            if (nome != null && !nome.isEmpty()) {
                Categoria c = new Categoria();
                c.setNome(nome);
                c.setUsuario(usuario);
                categoriaDAO.salvar(c);
                carregarCategorias();
            }
        });

        btnEditar.addActionListener(e -> {
            Categoria selecionada = listaCategorias.getSelectedValue();
            if (selecionada != null) {
                String novoNome = JOptionPane.showInputDialog("Editar categoria:", selecionada.getNome());
                if (novoNome != null && !novoNome.isEmpty()) {
                    selecionada.setNome(novoNome);
                    categoriaDAO.atualizar(selecionada);
                    carregarCategorias();
                }
            }
        });

        btnExcluir.addActionListener(e -> {
            Categoria selecionada = listaCategorias.getSelectedValue();
            if (selecionada != null && JOptionPane.showConfirmDialog(this, "Deseja excluir esta categoria?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                categoriaDAO.remover(selecionada);
                carregarCategorias();
            }
        });

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    private void carregarCategorias() {
        modeloLista.clear();
        categoriaDAO.listarPorUsuario(usuario).forEach(modeloLista::addElement);
    }
}
