package app.dao;
import app.model.*;

public class UsuarioDAO extends GenericDAO<Usuario> {
    public boolean existePorEmail(String email) {
        return em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE LOWER(TRIM(u.email)) = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    public Usuario buscarPorEmailESenha(String email, String senha) {
        return em.createQuery("FROM Usuario WHERE LOWER(TRIM(email)) = :email AND senha = :senha", Usuario.class)
                .setParameter("email", email)
                .setParameter("senha", senha)
                .getSingleResult();
    }
}
