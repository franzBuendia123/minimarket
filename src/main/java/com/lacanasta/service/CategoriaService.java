package com.lacanasta.service;

import com.lacanasta.dao.CategoriaDAO;
import com.lacanasta.dao.jdbc.CategoriaDAOJdbc;
import com.lacanasta.model.Categoria;
import com.lacanasta.util.ConnectionProvider;
import com.lacanasta.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio para la gestión de categorías.
 */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final ConnectionProvider connectionProvider;

    public CategoriaService() {
        this(new CategoriaDAOJdbc(), new DatabaseConnection());
    }

    public CategoriaService(CategoriaDAO categoriaDAO, ConnectionProvider connectionProvider) {
        this.categoriaDAO = categoriaDAO;
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lista todas las categorías.
     */
    public List<Categoria> findAll() {
        try (Connection conn = connectionProvider.getConnection()) {
            return categoriaDAO.findAll(conn);
        } catch (SQLException e) {
            throw new ServiceException("Error al listar categorías", e);
        }
    }

    /**
     * Busca una categoría por su identificador.
     */
    public Categoria findById(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            return categoriaDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la categoría", e);
        }
    }

    /**
     * Guarda una categoría (inserta o actualiza).
     */
    public void save(Categoria categoria) {
        try (Connection conn = connectionProvider.getConnection()) {
            if (categoria.getIdCategoria() == 0) {
                categoriaDAO.insert(conn, categoria);
            } else {
                categoriaDAO.update(conn, categoria);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al guardar la categoría", e);
        }
    }

    /**
     * Elimina una categoría por su identificador.
     */
    public void delete(int id) {
        try (Connection conn = connectionProvider.getConnection()) {
            categoriaDAO.delete(conn, id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar la categoría", e);
        }
    }
}
