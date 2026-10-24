package com.tienditadonpepe.service;

import com.tienditadonpepe.config.ConexionBD;
import com.tienditadonpepe.dao.IProveedorDAO;
import com.tienditadonpepe.dao.ProveedorDAO;
import com.tienditadonpepe.model.Proveedor;
import com.tienditadonpepe.util.DatosException;
import com.tienditadonpepe.util.NegocioException;
import com.tienditadonpepe.util.Validaciones;

import java.util.List;

/** Gestión de proveedores de atún enlatado. */
public class ProveedorService extends ServicioBase {

    private final IProveedorDAO dao = new ProveedorDAO();

    public List<Proveedor> listar() {
        sesion.exigir();
        try {
            return dao.readAll();
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public List<Proveedor> listarActivos() {
        sesion.exigir();
        try {
            return dao.readActivos();
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public String siguienteCodigo() {
        try {
            return dao.siguienteCodigo();
        } catch (DatosException e) {
            throw traducir(e, null, null);
        }
    }

    public Proveedor registrar(String razonSocial, String ruc, String telefono, String correo, String direccion) {
        sesion.exigirAdministrador();
        Proveedor p = new Proveedor();
        p.setRazon_social(Validaciones.requerido(razonSocial, "la razón social", 100));
        p.setRuc(Validaciones.ruc(ruc));
        p.setTelefono(Validaciones.telefonoOpcional(telefono));
        p.setCorreo(Validaciones.correoOpcional(correo));
        p.setDireccion(Validaciones.opcional(direccion, "la dirección", 150));
        p.setEstado(Proveedor.ESTADO_ACTIVO);
        try {
            return ConexionBD.getInstance().enTransaccion(() -> {
                p.setId_proveedor(dao.siguienteCodigo());
                dao.create(p);
                return p;
            });
        } catch (DatosException e) {
            throw traducir(e, "Ya existe un proveedor registrado con el RUC " + p.getRuc() + ".", null);
        }
    }

    public void actualizar(String idProveedor, String razonSocial, String ruc, String telefono, String correo,
                           String direccion, String estado) {
        sesion.exigirAdministrador();
        try {
            Proveedor p = dao.read(idProveedor);
            if (p == null) throw new NegocioException("El proveedor ya no existe.");
            String nuevoEstado = Proveedor.ESTADO_INACTIVO.equals(estado) ? Proveedor.ESTADO_INACTIVO : Proveedor.ESTADO_ACTIVO;
            if (Proveedor.ESTADO_INACTIVO.equals(nuevoEstado) && p.estaActivo()) {
                int n = dao.contarProductos(idProveedor);
                if (n > 0) {
                    throw new NegocioException("No se puede inactivar al proveedor: abastece " + n
                            + (n == 1 ? " producto activo." : " productos activos.") + " Cambia primero el proveedor de esos productos.");
                }
            }
            p.setRazon_social(Validaciones.requerido(razonSocial, "la razón social", 100));
            p.setRuc(Validaciones.ruc(ruc));
            p.setTelefono(Validaciones.telefonoOpcional(telefono));
            p.setCorreo(Validaciones.correoOpcional(correo));
            p.setDireccion(Validaciones.opcional(direccion, "la dirección", 150));
            p.setEstado(nuevoEstado);
            dao.update(p);
        } catch (DatosException e) {
            throw traducir(e, "Ya existe otro proveedor con ese RUC.", null);
        }
    }

    public void eliminar(String idProveedor) {
        sesion.exigirAdministrador();
        try {
            dao.delete(idProveedor);
        } catch (DatosException e) {
            throw traducir(e, null, "No se puede eliminar al proveedor porque tiene productos o ingresos asociados. Puedes inactivarlo.");
        }
    }
}
