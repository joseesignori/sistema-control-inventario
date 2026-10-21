package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO extends DaoBase implements IUsuarioDAO {

    private static Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_completo"), rs.getString("usuario_login"),
                rs.getString("contrasena_hash"), rs.getString("rol"), rs.getString("estado"));
    }

    @Override
    public void create(Usuario u) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO usuario (nombre_completo, usuario_login, contrasena_hash, rol, estado) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNombre_completo());
                ps.setString(2, u.getUsuario_login());
                ps.setString(3, u.getContrasena_hash());
                ps.setString(4, u.getRol());
                ps.setString(5, u.getEstado());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) u.setId_usuario(k.getInt(1));
                }
            }
            return null;
        });
    }

    @Override
    public Usuario read(Integer id) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM usuario WHERE id_usuario = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public Usuario readByLogin(String login) {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM usuario WHERE usuario_login = ?")) {
                ps.setString(1, login);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
    }

    @Override
    public List<Usuario> readAll() {
        return ejecutar(c -> {
            List<Usuario> lista = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM usuario ORDER BY rol, nombre_completo");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            return lista;
        });
    }

    @Override
    public void update(Usuario u) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE usuario SET nombre_completo = ?, usuario_login = ?, rol = ?, estado = ? WHERE id_usuario = ?")) {
                ps.setString(1, u.getNombre_completo());
                ps.setString(2, u.getUsuario_login());
                ps.setString(3, u.getRol());
                ps.setString(4, u.getEstado());
                ps.setInt(5, u.getId_usuario());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void updateContrasena(int idUsuario, String contrasenaHash) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE usuario SET contrasena_hash = ? WHERE id_usuario = ?")) {
                ps.setString(1, contrasenaHash);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public int countAdministradoresActivos() {
        return ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM usuario WHERE rol = 'Administrador' AND estado = 'Activo'");
                 ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
    }

    @Override
    public void delete(Integer id) {
        ejecutar(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM usuario WHERE id_usuario = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return null;
        });
    }
}
