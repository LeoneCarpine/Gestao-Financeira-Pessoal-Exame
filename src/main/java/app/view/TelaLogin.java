package app.view;

import app.controller.UsuarioController;
import app.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaLogin extends JFrame {
    private JTextField campoEmail;
    private JPasswordField campoSenha;
    private JButton btnLogin;
    private JButton btnCadastrar;
    private UsuarioController usuarioController;

    public TelaLogin() {
        super("Login - Gestão Financeira");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        usuarioController = new UsuarioController();

        campoEmail = new JTextField(20);
        campoSenha = new JPasswordField(20);
        btnLogin = new JButton("Entrar");
        btnCadastrar = new JButton("Cadastrar");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Email:"));
        panel.add(campoEmail);
        panel.add(new JLabel("Senha:"));
        panel.add(campoSenha);
        panel.add(btnLogin);
        panel.add(btnCadastrar);

        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = campoEmail.getText();
                String senha = new String(campoSenha.getPassword());

                try {
                    Usuario usuario = usuarioController.login(email, senha);
                    String nome = usuario.getNome();
                    JOptionPane.showMessageDialog(TelaLogin.this, "Bem-vindo, "
                            + (nome == null || nome.isBlank() ? usuario.getEmail() : nome));
                    new TelaPrincipal(usuario).setVisible(true);
                    dispose();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(TelaLogin.this, ex.getMessage(), "Confira os dados", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TelaLogin.this, "Não foi possível entrar. Tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCadastrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = campoEmail.getText();
                String senha = new String(campoSenha.getPassword());
                String nome = JOptionPane.showInputDialog(TelaLogin.this, "Digite seu nome:");
                if (nome == null) {
                    return;
                }

                try {
                    usuarioController.cadastrar(nome, email, senha);
                    JOptionPane.showMessageDialog(TelaLogin.this, "Usuário cadastrado com sucesso!");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(TelaLogin.this, ex.getMessage(), "Confira os dados", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TelaLogin.this, "Não foi possível cadastrar. Tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}
