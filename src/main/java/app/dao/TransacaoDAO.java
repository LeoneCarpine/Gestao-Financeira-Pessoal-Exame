package app.dao;

import app.model.Categoria;
import app.model.Transacao;
import app.model.Usuario;

import java.time.LocalDate;
import java.util.List;

public class TransacaoDAO extends GenericDAO<Transacao> {
    public List<Transacao> listarPorUsuario(Usuario usuario) {
        return em.createQuery("FROM Transacao WHERE usuario = :usuario", Transacao.class)
                .setParameter("usuario", usuario)
                .getResultList();
    }

    public List<Transacao> filtrar(Usuario usuario, LocalDate inicio, LocalDate fim, String tipo, Categoria categoria) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("É necessário estar conectado para consultar o histórico.");
        }
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        if (tipo != null && !tipo.equals("receita") && !tipo.equals("despesa")) {
            throw new IllegalArgumentException("Selecione um tipo de transação válido.");
        }
        StringBuilder jpql = new StringBuilder("SELECT t FROM Transacao t LEFT JOIN FETCH t.categoria WHERE t.usuario = :usuario");
        if (inicio != null) jpql.append(" AND t.data >= :inicio");
        if (fim != null) jpql.append(" AND t.data <= :fim");
        if (tipo != null) jpql.append(" AND t.tipo = :tipo");
        if (categoria != null) jpql.append(" AND t.categoria = :categoria");
        jpql.append(" ORDER BY t.data DESC, t.id DESC");
        var consulta = em.createQuery(jpql.toString(), Transacao.class).setParameter("usuario", usuario);
        if (inicio != null) consulta.setParameter("inicio", inicio);
        if (fim != null) consulta.setParameter("fim", fim);
        if (tipo != null) consulta.setParameter("tipo", tipo);
        if (categoria != null) consulta.setParameter("categoria", categoria);
        return consulta.getResultList();
    }
}
