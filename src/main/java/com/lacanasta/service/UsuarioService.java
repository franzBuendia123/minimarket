package com.lacanasta.service;

import com.lacanasta.dao.UsuarioDAO;
import com.lacanasta.dao.jdbc.UsuarioDAOJdbc;
import com.lacanasta.model.Usuario;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;
import com.lacanasta.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio para la gestión y autenticación de usuarios.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final ConnectionProvider connectionProvider;

    /**
     * Constructor por defecto que crea las dependencias reales.
     */
    public UsuarioService() {
        this(new UsuarioDAOJdbc(), new DatabaseConnection());
    }

    /**
     * Constructor para inyección de dependencias (pruebas).
     */
    public UsuarioService(UsuarioDAO usuarioDAO, ConnectionProvider connectionProvider) {
        this.usuarioDAO = usuarioDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lista todos los usuarios del sistema.
     */
    public List<Usuario> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return usuarioDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar usuarios", e);
        }
    }

    /**
     * Busca un usuario por su identificador.
     */
    public Usuario findById(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            return usuarioDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el usuario", e);
        }
    }

    /**
     * Guarda un usuario (inserta si es nuevo o actualiza si ya existe).
     */
    public void save(Usuario usuario) {
        try (Connection conn = connectionProvider.getConnection()) {
            if (usuario.getIdUsuario() == 0) {
                if (usuario.getFechaCreacion() == null) {
                    usuario.setFechaCreacion(LocalDateTime.now());
                }
                usuarioDAO.insert(conn, usuario);
            } else {
                usuarioDAO.update(conn, usuario);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar el usuario", e);
        }
    }

    /**
     * Elimina un usuario por su identificador.
     */
    public void delete(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            usuarioDAO.delete(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el usuario", e);
        }
    }

    /**
     * Autentica a un usuario verificando credenciales.
     *
     * <p>Soporta contraseñas hasheadas con BCrypt y texto plano (fase de pruebas).</p>
     *
     * @param username   nombre de usuario.
     * @param contrasena contraseña ingresada.
     * @return el usuario autenticado, o {@code null} si las credenciales son inválidas.
     */
    public Usuario authenticate(String username, String contrasena) {
        try (Connection conn = connectionProvider.getConnection()) {
            Usuario usuario = usuarioDAO.findByUsername(conn, username);
            if (usuario != null && usuario.isEstado()
                    && PasswordUtil.verificar(contrasena, usuario.getContrasena())) {
                return usuario;
            }
            return null;
        } catch (SQLException e) {
            throw new ServiceException("Error en la autenticación", e);
        }
    }
}
