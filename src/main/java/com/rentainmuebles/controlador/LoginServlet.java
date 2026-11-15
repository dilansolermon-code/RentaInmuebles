package com.rentainmuebles.controlador;

import com.rentainmuebles.datos.UsuarioDAO;
import com.rentainmuebles.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Recibe el formulario de login.jsp.
 * Busca TODOS los roles del correo y le pide a cada objeto que verifique la contraseña
 * (Usuario.iniciarSesion, como en el diagrama de clases).
 * Si la persona tiene un solo rol entra directo; si tiene varios, elige con cuál ingresar.
 */
@WebServlet("/login")   // dirección: /RentaInmuebles/login
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");                 // para leer bien tildes y ñ
        String email = request.getParameter("email");
        String contrasena = request.getParameter("contrasena");
        String inicio = request.getContextPath();              // /RentaInmuebles

        try {
            // 1. Roles del correo cuya contraseña coincide
            List<Usuario> roles = new ArrayList<>();
            for (Usuario u : new UsuarioDAO().buscarRolesPorEmail(email)) {
                if (u.iniciarSesion(email, contrasena)) {
                    roles.add(u);
                }
            }
            if (roles.isEmpty()) {
                response.sendRedirect(inicio + "/login.jsp?error=datos");
                return;
            }

            // 2. Se guardan en la sesión todos sus roles (para poder cambiar de rol después)
            HttpSession sesion = request.getSession();
            sesion.setAttribute("roles", roles);

            // 3. Un solo rol: entra directo. Varios: pantalla para elegir
            if (roles.size() == 1) {
                entrarComo(roles.get(0), request, response);
            } else {
                sesion.removeAttribute("usuario");             // aún no ha elegido
                response.sendRedirect(inicio + "/elegir-rol.jsp");
            }
        } catch (SQLException e) {
            getServletContext().log("Error al iniciar sesión", e);   // queda en la ventana Output
            response.sendRedirect(inicio + "/login.jsp?error=bd");
        }
    }

    /** Deja al usuario "logueado" con el rol dado y lo envía a su pantalla principal. */
    static void entrarComo(Usuario u, HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("usuario", u);
        String inicio = request.getContextPath();
        switch (u.getRol()) {
            case "admin":       response.sendRedirect(inicio + "/admin/inmuebles.jsp"); break;
            case "propietario": response.sendRedirect(inicio + "/propietario/inmuebles.jsp"); break;
            default:            response.sendRedirect(inicio + "/cliente/buscar.jsp");
        }
    }
}
