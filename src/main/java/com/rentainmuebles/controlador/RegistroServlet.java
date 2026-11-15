package com.rentainmuebles.controlador;

import com.rentainmuebles.datos.UsuarioDAO;
import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * CU-01 Registrarse: crea una cuenta de Cliente o de Propietario.
 * Si el correo ya existe con el otro rol y la contraseña coincide, le agrega el rol nuevo
 * al mismo usuario (una persona puede ser Cliente y Propietario a la vez).
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String volver = request.getContextPath() + "/registro.jsp?error=";

        // 1. Leer el formulario
        String tipo        = request.getParameter("tipo");          // "cliente" o "propietario"
        String nombre      = texto(request.getParameter("nombre"));
        String telefono    = texto(request.getParameter("telefono"));
        String email       = texto(request.getParameter("email")).toLowerCase();
        String contrasena  = request.getParameter("contrasena");
        String tipoPersona = request.getParameter("tipoPersona");
        String numeroCta   = texto(request.getParameter("numeroCta"));
        String tipoCta     = request.getParameter("tipoCta");
        int idCiudad = Validar.entero(request.getParameter("idCiudad"));
        int idBanco  = Validar.entero(request.getParameter("idBanco"));

        // 2. Validar
        boolean tipoValido = "cliente".equals(tipo) || "propietario".equals(tipo);
        boolean datosValidos = !nombre.isEmpty() && email.contains("@")
                && contrasena != null && contrasena.length() >= 4;
        boolean rolValido = "cliente".equals(tipo)
                ? idCiudad > 0
                : ("Natural".equals(tipoPersona) || "Juridica".equals(tipoPersona));
        if (!tipoValido || !datosValidos || !rolValido) {
            response.sendRedirect(volver + "datos");
            return;
        }
        if (!Validar.cuenta(idBanco, numeroCta, tipoCta)) {
            response.sendRedirect(volver + "cuenta");
            return;
        }

        // 3. Guardar
        try {
            UsuarioDAO dao = new UsuarioDAO();
            List<Usuario> cuentas = dao.buscarRolesPorEmail(email);   // roles que ya tiene ese correo

            // 3a. Correo nuevo: se crea el usuario con su rol
            if (cuentas.isEmpty()) {
                dao.registrar(tipo, nombre, telefono, email, contrasena,
                              idCiudad, tipoPersona, idBanco, numeroCta, tipoCta);
                response.sendRedirect(request.getContextPath() + "/login.jsp?registrado=1");
                return;
            }

            // 3b. El correo ya existe: ¿ya tiene el rol que pide?
            for (Usuario c : cuentas) {
                if (c.getRol().equals(tipo)) {
                    response.sendRedirect(volver + "email");          // "El correo ya está en uso"
                    return;
                }
            }

            // 3c. Tiene otro rol: solo se agrega si demuestra que es la misma persona (misma contraseña)
            Usuario existente = cuentas.get(0);
            if (!existente.iniciarSesion(email, contrasena)) {
                response.sendRedirect(volver + "clave");
                return;
            }
            dao.agregarRol(existente.getId(), tipo, idCiudad, tipoPersona, idBanco, numeroCta, tipoCta);
            response.sendRedirect(request.getContextPath() + "/login.jsp?registrado=rol");
        } catch (SQLException e) {
            getServletContext().log("Error al registrar usuario", e);
            // 23505 = código de PostgreSQL para "llave duplicada" (regla UNIQUE):
            // el número de cuenta ya lo tiene registrado otra persona
            if ("23505".equals(e.getSQLState())) {
                response.sendRedirect(volver + "cuentaRepetida");
                return;
            }
            // Se envía el mensaje de PostgreSQL a la pantalla para saber qué regla falló
            String detalle = URLEncoder.encode(String.valueOf(e.getMessage()), StandardCharsets.UTF_8);
            response.sendRedirect(volver + "bd&detalle=" + detalle);
        }
    }

    /** Devuelve el texto sin espacios al inicio y al final ("" si es null). */
    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
