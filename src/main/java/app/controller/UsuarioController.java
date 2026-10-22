package app.controller;

import app.dao.*;
import app.model.*;
import java.util.Locale;
import java.util.regex.Pattern;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

public class UsuarioController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario login(String email, String senha) {
        String emailNormalizado = validarEmail(email);
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe a senha.");
        }
        try {
            return usuarioDAO.buscarPorEmailESenha(emailNormalizado, senha);
        } catch (NoResultException | NonUniqueResultException ex) {
            throw new IllegalArgumentException("E-mail ou senha inválidos.");
        }
    }

    public void cadastrar(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe seu nome.");
        }
        String emailNormalizado = validarEmail(email);
        if (senha == null || senha.isBlank() || senha.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres e não pode conter apenas espaços.");
        }
        if (usuarioDAO.existePorEmail(emailNormalizado)) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }
        Usuario u = new Usuario();
        u.setNome(nome.strip());
        u.setEmail(emailNormalizado);
        u.setSenha(senha);
        usuarioDAO.salvar(u);
    }

    private String validarEmail(String email) {
        String normalizado = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido, como nome@exemplo.com.");
        }
        return normalizado;
    }
}
