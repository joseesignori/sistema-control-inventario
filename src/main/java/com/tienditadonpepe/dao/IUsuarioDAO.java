package com.tienditadonpepe.dao;

import com.tienditadonpepe.model.Usuario;

public interface IUsuarioDAO extends ICrudDAO<Usuario, Integer> {
    Usuario readByLogin(String usuarioLogin);

    void updateContrasena(int idUsuario, String contrasenaHash);

    int countAdministradoresActivos();
}
