package app.controller;

import app.dao.*;
import app.model.*;
import java.time.LocalDate;
import java.util.List;

public class TransacaoController {
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();

    public List<Transacao> buscarHistorico(Usuario usuario, LocalDate inicio, LocalDate fim,
                                          String tipo, Categoria categoria) {
        return transacaoDAO.filtrar(usuario, inicio, fim, tipo, categoria);
    }

    public void registrarTransacao(double valor, String descricao, String tipo, LocalDate data, Usuario usuario, Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new IllegalArgumentException("Selecione uma categoria cadastrada antes de salvar a transação.");
        }
        if (usuario == null || usuario.getId() == null || categoria.getUsuario() == null
                || !usuario.getId().equals(categoria.getUsuario().getId())) {
            throw new IllegalArgumentException("A categoria deve pertencer ao usuário conectado.");
        }
        Transacao t = new Transacao();
        t.setValor(valor);
        t.setDescricao(descricao);
        t.setTipo(tipo);
        t.setData(data);
        t.setUsuario(usuario);
        t.setCategoria(categoria);
        transacaoDAO.salvar(t);
    }
}
