package com.lacanasta.service;

import com.lacanasta.dao.UsuarioDAO;
import com.lacanasta.model.Rol;
import com.lacanasta.model.Usuario;
import com.lacanasta.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link UsuarioService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    private UsuarioService usuarioService;
    private MockedStatic<DatabaseConnection> dbMock;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioDAO);
        dbMock = mockStatic(DatabaseConnection.class);
        dbMock.when(DatabaseConnection::getConnection).thenReturn(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setUsuario("admin");
        usuario.setContrasena("admin123");
        usuario.setEstado(true);
        usuario.setRol(Rol.ADMIN);
        return usuario;
    }

    @Test
    void authenticateDebeDevolverUsuarioCuandoCredencialesSonValidas() throws SQLException {
        when(usuarioDAO.findByUsername(any(Connection.class), eq("admin"))).thenReturn(usuarioActivo());

        Usuario resultado = usuarioService.authenticate("admin", "admin123");

        assertNotNull(resultado);
        assertEquals("admin", resultado.getUsuario());
    }

    @Test
    void authenticateDebeDevolverNuloCuandoContrasenaEsIncorrecta() throws SQLException {
        when(usuarioDAO.findByUsername(any(Connection.class), eq("admin"))).thenReturn(usuarioActivo());

        assertNull(usuarioService.authenticate("admin", "incorrecta"));
    }

    @Test
    void authenticateDebeDevolverNuloCuandoUsuarioEstaInactivo() throws SQLException {
        Usuario usuario = usuarioActivo();
        usuario.setEstado(false);
        when(usuarioDAO.findByUsername(any(Connection.class), eq("admin"))).thenReturn(usuario);

        assertNull(usuarioService.authenticate("admin", "admin123"));
    }

    @Test
    void authenticateDebeDevolverNuloCuandoUsuarioNoExiste() throws SQLException {
        when(usuarioDAO.findByUsername(any(Connection.class), eq("x"))).thenReturn(null);

        assertNull(usuarioService.authenticate("x", "cualquiera"));
    }
}
