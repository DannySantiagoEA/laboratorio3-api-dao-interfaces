package co.edu.univalle.ir.dao;

import co.edu.univalle.ir.conexion.ConexionDB;
import co.edu.univalle.ir.entity.int_proceso;
import co.edu.univalle.ir.entity.int_usuarios;
import co.edu.univalle.ir.entity.int_usuarios_procesos;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase de prueba autoejecutable para la Tarea #10: pruebas unitarias y de caja
 * negra sobre las consultas filtradas de usuarios
 * (<code>int_usuariosDAO</code>) y la asignacion de horarios
 * (<code>int_usuarios_procesosDAO</code>).
 * <p>
 * Caja negra: los casos salen de lo que promete la interfaz de cada DAO (su
 * Javadoc): clases de equivalencia (existe, no existe, vacio, null) y valores
 * limite (clave de 30 y de 31 caracteres, ids 0, -1 y 999999), sin mirar como
 * esta escrito el codigo. Unitaria: cada metodo se comprueba por separado.
 * </p>
 * <p>
 * La prueba crea sus propios datos (dos perfiles de usuario, dos procesos y
 * cuatro usuarios, todos con un nombre o correo que los identifica) y los borra
 * al terminar. Solo exige que el proceso 1 exista (punto 3.b), porque de el
 * toma el tipo de proceso. Nunca modifica ni borra el proceso 1.
 * </p>
 *
 * @author Santiago Gutierrez Reina
 * @version $Revision: 1.0 $ 2026/11/18
 */
public class PruebaCajaNegraUsuariosHorariosDAO {

    /**
     * Id que no existe en ninguna tabla.
     */
    private static final int ID_INEXISTENTE = 999999;

    /**
     * Nombre con el que se reconocen los procesos temporales de esta prueba.
     */
    private static final String NOMBRE_TEMP = "Proceso temporal prueba #10";

    /**
     * Nombre con el que se reconocen los perfiles temporales de esta prueba.
     */
    private static final String NOMBRE_TIPO = "Tipo temporal prueba #10";

    /**
     * Dominio de correo que llevan todos los usuarios de esta prueba. Con el se
     * encuentran y se borran al final, pase lo que pase.
     */
    private static final String DOMINIO = "@prueba10.local";

    private static final String EMAIL_A = "ana.xq10" + DOMINIO;
    private static final String EMAIL_B = "beto.xq10" + DOMINIO;
    private static final String EMAIL_C = "carla.yv10" + DOMINIO;
    private static final String EMAIL_D = "dario.xq10" + DOMINIO;
    private static final String EMAIL_E = "limite.clave" + DOMINIO;
    private static final String EMAIL_X = "invalido.xq10" + DOMINIO;

    private static int aciertos = 0;
    private static int fallos = 0;

    /**
     * Registra el resultado de una comprobacion y lo imprime.
     *
     * @param descripcion Texto de lo que se comprueba.
     * @param condicion true si la comprobacion se cumple.
     */
    static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aciertos++;
            System.out.println("    [PASA]  " + descripcion);
        } else {
            fallos++;
            System.out.println("    [FALLA] " + descripcion);
        }
    }

    /**
     * Imprime un hallazgo que no cuenta como acierto ni como fallo.
     *
     * @param descripcion Lo que se probo.
     * @param resultado Lo que hizo el DAO.
     */
    static void observar(String descripcion, String resultado) {
        System.out.println("    [OBS]   " + descripcion + ": " + resultado);
    }

    // =========================================================================
    // AYUDAS PARA COMPARAR LISTAS
    // =========================================================================
    private static Set<Integer> conjunto(int... valores) {
        Set<Integer> s = new TreeSet<>();
        for (int v : valores) {
            s.add(v);
        }
        return s;
    }

    private static Set<Integer> idsDe(List<int_usuarios> lista) {
        Set<Integer> s = new TreeSet<>();
        if (lista != null) {
            for (int_usuarios u : lista) {
                s.add(u.getId());
            }
        }
        return s;
    }

    /**
     * true si la lista tiene exactamente a esos usuarios (ni mas ni menos).
     */
    private static boolean son(List<int_usuarios> lista, int... esperados) {
        return lista != null && idsDe(lista).equals(conjunto(esperados));
    }

    /**
     * true si la lista incluye a esos usuarios (puede tener otros).
     */
    private static boolean incluye(List<int_usuarios> lista, int... esperados) {
        return lista != null && idsDe(lista).containsAll(conjunto(esperados));
    }

    private static boolean sinUsuarios(List<int_usuarios> lista) {
        return lista != null && lista.isEmpty();
    }

    private static boolean rechazaAlta(int_usuariosDAO dao, String nombres, String apellidos,
            String email, String clave, int tipo) {
        try {
            dao.insertarUsuario(nombres, apellidos, email, clave, tipo);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean rechazaEdicion(int_usuariosDAO dao, int_usuarios usuario) {
        try {
            dao.editarUsuario(usuario);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static boolean rechazaClave(int_usuariosDAO dao, int id, String clave) {
        try {
            dao.cambiarClave(id, clave);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    // =========================================================================
    // PRUEBAS DE int_usuariosDAO
    // =========================================================================
    /**
     * Ejecuta las pruebas de alta, consultas filtradas, login, entradas
     * invalidas, edicion y borrado de int_usuariosDAO. Deja en la base a los
     * usuarios A, B y C para que los use la prueba de horarios; el usuario D lo
     * crea y lo borra ella misma.
     *
     * @param dao DAO de la tabla int_usuarios.
     * @param tipoA Id de un perfil (int_usuarios_tipo) que existe.
     * @param tipoB Id de otro perfil que existe.
     * @return Los ids de los usuarios A, B y C, o null si no se pudieron crear.
     */
    static int[] probarUsuarios(int_usuariosDAO dao, int tipoA, int tipoB) {
        System.out.println("\n[1] ALTA de usuarios (insertarUsuario)");
        int_usuarios ua = new int_usuarios("Ana Xq10", "Lopez Zr10", EMAIL_A, "claveA1", tipoA);
        int_usuarios uc = new int_usuarios("Carla Yv10", "Rojas Wk10", EMAIL_C, "claveC3", tipoB);
        verificar("con la entidad (usuario A, perfil A): inserta 1 fila", dao.insertarUsuario(ua) == 1);
        verificar("el DAO le asigna un id a la entidad (" + ua.getId() + ")", ua.getId() > 0);
        verificar("con campos sueltos (usuario B, perfil A): inserta 1 fila",
                dao.insertarUsuario("Beto Xq10", "Lopez Wk10", EMAIL_B, "claveB2", tipoA) == 1);
        verificar("con la entidad (usuario C, perfil B): inserta 1 fila", dao.insertarUsuario(uc) == 1);
        int_usuarios ub = dao.seleccionarPorEmail(EMAIL_B);
        if (ua.getId() <= 0 || ub == null || uc.getId() <= 0) {
            System.out.println("    Se detiene: sin los tres usuarios de prueba no se puede seguir.");
            return null;
        }
        int a = ua.getId();
        int b = ub.getId();
        int c = uc.getId();
        String clave30 = "k".repeat(30);
        verificar("clave de 30 caracteres (el limite): se acepta",
                dao.insertarUsuario("Limite", "Clave30", EMAIL_E, clave30, tipoA) == 1);
        int_usuarios limite = dao.seleccionarPorEmail(EMAIL_E);
        verificar("la clave de 30 caracteres se guarda completa",
                limite != null && clave30.equals(limite.getClave()));
        if (limite != null) {
            dao.borrarUsuario(limite.getId());
        }

        System.out.println("\n[2] CONSULTAS FILTRADAS (id, perfil, nombres, apellidos, email, combinadas)");
        int_usuarios leidoA = dao.seleccionarPorId(a);
        verificar("por id (A): trae todos sus datos",
                leidoA != null && "Ana Xq10".equals(leidoA.getNombres())
                && "Lopez Zr10".equals(leidoA.getApellidos())
                && EMAIL_A.equals(leidoA.getEmail())
                && "claveA1".equals(leidoA.getClave())
                && leidoA.getInt_usuarios_tipo_id() == tipoA);
        verificar("por id inexistente (0, -1 y 999999): devuelve null",
                dao.seleccionarPorId(0) == null && dao.seleccionarPorId(-1) == null
                && dao.seleccionarPorId(ID_INEXISTENTE) == null);
        verificar("por perfil A: salen A y B, y no C", son(dao.seleccionarPorTipo(tipoA), a, b));
        verificar("por perfil B: sale solo C", son(dao.seleccionarPorTipo(tipoB), c));
        verificar("por perfil inexistente (0, -1 y 999999): listas vacias, no null",
                sinUsuarios(dao.seleccionarPorTipo(0)) && sinUsuarios(dao.seleccionarPorTipo(-1))
                && sinUsuarios(dao.seleccionarPorTipo(ID_INEXISTENTE)));
        verificar("por nombres \"Xq10\": salen A y B", son(dao.seleccionarPorNombres("Xq10"), a, b));
        verificar("por nombres, coincidencia parcial \"Xq1\": salen A y B",
                son(dao.seleccionarPorNombres("Xq1"), a, b));
        verificar("por nombres, en minusculas \"xq10\": salen A y B (no distingue mayusculas)",
                son(dao.seleccionarPorNombres("xq10"), a, b));
        verificar("por nombres \"Yv10\": sale solo C", son(dao.seleccionarPorNombres("Yv10"), c));
        verificar("por nombres sin coincidencias: lista vacia, no null",
                sinUsuarios(dao.seleccionarPorNombres("Zzz99NoExiste")));
        int totalUsuarios = dao.seleccionarTodos().size();
        verificar("por nombres null, \"\" y \"   \": no filtra (devuelve a todos)",
                dao.seleccionarPorNombres(null).size() == totalUsuarios
                && dao.seleccionarPorNombres("").size() == totalUsuarios
                && dao.seleccionarPorNombres("   ").size() == totalUsuarios);
        verificar("por apellidos \"Wk10\": salen B y C", son(dao.seleccionarPorApellidos("Wk10"), b, c));
        verificar("por apellidos \"Zr10\": sale solo A", son(dao.seleccionarPorApellidos("Zr10"), a));
        verificar("por apellidos con dos palabras \"Lopez Wk10\": sale solo B",
                son(dao.seleccionarPorApellidos("Lopez Wk10"), b));
        verificar("por apellidos sin coincidencias: lista vacia, no null",
                sinUsuarios(dao.seleccionarPorApellidos("Zzz99NoExiste")));
        int_usuarios porEmail = dao.seleccionarPorEmail(EMAIL_A);
        verificar("por email exacto (A): devuelve a A", porEmail != null && porEmail.getId() == a);
        verificar("por email que no existe: devuelve null", dao.seleccionarPorEmail("nadie" + DOMINIO) == null);
        verificar("por email incompleto \"ana.xq10\": devuelve null (el email es exacto)",
                dao.seleccionarPorEmail("ana.xq10") == null);
        verificar("por email null, \"\" y \"   \": devuelve null",
                dao.seleccionarPorEmail(null) == null && dao.seleccionarPorEmail("") == null
                && dao.seleccionarPorEmail("   ") == null);
        observar("email escrito en mayusculas",
                dao.seleccionarPorEmail(EMAIL_A.toUpperCase()) != null
                ? "lo encuentra (no distingue mayusculas)" : "no lo encuentra (distingue mayusculas)");
        verificar("combinada perfil A + nombres \"Xq10\": salen A y B",
                son(dao.seleccionarPorCampos(null, tipoA, "Xq10", null, null), a, b));
        verificar("combinada perfil A + apellidos \"Wk10\": sale solo B (AND)",
                son(dao.seleccionarPorCampos(null, tipoA, null, "Wk10", null), b));
        verificar("combinada perfil B + apellidos \"Wk10\": sale solo C",
                son(dao.seleccionarPorCampos(null, tipoB, null, "Wk10", null), c));
        verificar("combinada perfil A + apellidos \"Rojas\" (Rojas es de C): lista vacia",
                sinUsuarios(dao.seleccionarPorCampos(null, tipoA, null, "Rojas", null)));
        verificar("combinada id de A + perfil A: sale solo A",
                son(dao.seleccionarPorCampos(a, tipoA, null, null, null), a));
        verificar("combinada id de A + perfil B (se contradicen): lista vacia",
                sinUsuarios(dao.seleccionarPorCampos(a, tipoB, null, null, null)));
        verificar("combinada nombres \"Xq10\" + apellidos \"Zr10\": sale solo A",
                son(dao.seleccionarPorCampos(null, null, "Xq10", "Zr10", null), a));
        verificar("combinada por parte del email \"yv10@\": sale solo C",
                son(dao.seleccionarPorCampos(null, null, null, null, "yv10@"), c));
        verificar("combinada con textos vacios: no filtra (devuelve a todos)",
                dao.seleccionarPorCampos(null, null, "", "   ", "").size() == totalUsuarios);
        List<int_usuarios> todos = dao.seleccionarPorCampos(null, null, null, null, null);
        verificar("combinada con todo en null: devuelve a todos, incluidos A, B y C",
                todos.size() == totalUsuarios && incluye(todos, a, b, c));

        System.out.println("\n[3] LOGIN (validarCredenciales)");
        int_usuarios login = dao.validarCredenciales(EMAIL_A, "claveA1");
        verificar("email y clave correctos (A): devuelve a A", login != null && login.getId() == a);
        verificar("clave incorrecta: devuelve null", dao.validarCredenciales(EMAIL_A, "otra") == null);
        verificar("clave con otra mayuscula \"CLAVEA1\": devuelve null (la clave distingue mayusculas)",
                dao.validarCredenciales(EMAIL_A, "CLAVEA1") == null);
        verificar("email de A con la clave de B: devuelve null",
                dao.validarCredenciales(EMAIL_A, "claveB2") == null);
        verificar("email que no existe: devuelve null",
                dao.validarCredenciales("nadie" + DOMINIO, "claveA1") == null);
        verificar("email null o vacio: devuelve null",
                dao.validarCredenciales(null, "claveA1") == null && dao.validarCredenciales("", "claveA1") == null);
        verificar("clave null o vacia: devuelve null",
                dao.validarCredenciales(EMAIL_A, null) == null && dao.validarCredenciales(EMAIL_A, "") == null);

        System.out.println("\n[4] ENTRADAS INVALIDAS"
                + " (el error de llave foranea que sale en la consola es el esperado)");
        int antes = dao.seleccionarTodos().size();
        verificar("nombres vacio: lanza IllegalArgumentException",
                rechazaAlta(dao, "", "Apellido", EMAIL_X, "clave", tipoA));
        verificar("nombres null: lanza IllegalArgumentException",
                rechazaAlta(dao, null, "Apellido", EMAIL_X, "clave", tipoA));
        verificar("apellidos en blanco: lanza IllegalArgumentException",
                rechazaAlta(dao, "Nombre", "   ", EMAIL_X, "clave", tipoA));
        verificar("clave vacia: lanza IllegalArgumentException",
                rechazaAlta(dao, "Nombre", "Apellido", EMAIL_X, "", tipoA));
        verificar("clave null: lanza IllegalArgumentException",
                rechazaAlta(dao, "Nombre", "Apellido", EMAIL_X, null, tipoA));
        verificar("clave de 31 caracteres (uno sobre el limite): lanza IllegalArgumentException",
                rechazaAlta(dao, "Nombre", "Apellido", EMAIL_X, "k".repeat(31), tipoA));
        verificar("perfil inexistente: devuelve 0 filas (llave foranea)",
                dao.insertarUsuario("Nombre", "Apellido", EMAIL_X, "clave", ID_INEXISTENTE) == 0);
        verificar("insertarUsuario(null) devuelve 0", dao.insertarUsuario((int_usuarios) null) == 0);
        verificar("los rechazos no dejaron filas de mas", dao.seleccionarTodos().size() == antes);
        int filasDuplicado = dao.insertarUsuario("Duplicado", "Email Repetido", EMAIL_A, "claveX9", tipoA);
        observar("dos usuarios con el mismo email",
                filasDuplicado == 1 ? "el DAO los ACEPTA (la tabla no tiene UNIQUE en email)"
                        : "el DAO lo rechaza");
        // Se recorre una COPIA: el DAO guarda en memoria la misma lista que devuelve
        // y borrarUsuario la modifica, asi que borrar mientras se recorre la
        // original lanza ConcurrentModificationException.
        for (int_usuarios repetido : new ArrayList<>(
                dao.seleccionarPorCampos(null, tipoA, "Duplicado", null, null))) {
            dao.borrarUsuario(repetido.getId());
        }

        System.out.println("\n[5] EDITAR Y BORRAR (con un usuario D que se crea y se borra aqui)");
        int_usuarios d = new int_usuarios("Dario Xq10", "Mora Zr10", EMAIL_D, "claveD4", tipoA);
        verificar("alta del usuario D: inserta 1 fila y asigna id", dao.insertarUsuario(d) == 1 && d.getId() > 0);
        if (d.getId() <= 0) {
            System.out.println("    Se detiene: sin el usuario D no se puede probar editar ni borrar.");
            return new int[]{a, b, c};
        }
        int idD = d.getId();
        d.setNombres("Dario Editado");
        d.setApellidos("Mora Editado");
        d.setInt_usuarios_tipo_id(tipoB);
        verificar("editarUsuario devuelve 1 fila", dao.editarUsuario(d) == 1);
        int_usuarios editado = dao.seleccionarPorId(idD);
        verificar("los datos editados se leen igual y el email no cambia",
                editado != null && "Dario Editado".equals(editado.getNombres())
                && "Mora Editado".equals(editado.getApellidos())
                && editado.getInt_usuarios_tipo_id() == tipoB && EMAIL_D.equals(editado.getEmail()));
        verificar("las consultas ven el cambio: por perfil B ahora salen C y D",
                son(dao.seleccionarPorTipo(tipoB), c, idD));
        verificar("las consultas ven el cambio: por perfil A ya no sale D",
                son(dao.seleccionarPorTipo(tipoA), a, b));
        verificar("por nombres \"Dario Editado\": aparece D", incluye(dao.seleccionarPorNombres("Dario Editado"), idD));
        verificar("editar con nombres vacio: lanza IllegalArgumentException",
                rechazaEdicion(dao, new int_usuarios(idD, tipoB, "", "Mora", EMAIL_D, "claveD4")));
        verificar("editar un id que no existe: devuelve 0 filas",
                dao.editarUsuario(new int_usuarios(ID_INEXISTENTE, tipoB, "Nadie", "Nadie", EMAIL_X, "clave")) == 0);
        verificar("editarUsuario(null) devuelve 0", dao.editarUsuario(null) == 0);
        verificar("editar con un perfil inexistente: devuelve 0 filas",
                dao.editarUsuario(new int_usuarios(idD, ID_INEXISTENTE, "Dario Editado", "Mora Editado",
                        EMAIL_D, "claveD4")) == 0);
        verificar("tras los rechazos, D conserva su perfil y sus nombres",
                dao.seleccionarPorId(idD) != null
                && dao.seleccionarPorId(idD).getInt_usuarios_tipo_id() == tipoB
                && "Dario Editado".equals(dao.seleccionarPorId(idD).getNombres()));
        verificar("cambiarClave devuelve 1 fila", dao.cambiarClave(idD, "NuevaClave5") == 1);
        verificar("con la clave anterior ya no entra", dao.validarCredenciales(EMAIL_D, "claveD4") == null);
        int_usuarios entra = dao.validarCredenciales(EMAIL_D, "NuevaClave5");
        verificar("con la clave nueva si entra", entra != null && entra.getId() == idD);
        verificar("cambiarClave con clave vacia: lanza IllegalArgumentException", rechazaClave(dao, idD, ""));
        verificar("cambiarClave con 31 caracteres: lanza IllegalArgumentException",
                rechazaClave(dao, idD, "k".repeat(31)));
        verificar("cambiarClave de un id que no existe: devuelve 0 filas",
                dao.cambiarClave(ID_INEXISTENTE, "abc") == 0);
        verificar("tras los rechazos, la clave de D sigue siendo la nueva",
                dao.validarCredenciales(EMAIL_D, "NuevaClave5") != null);
        verificar("borrarUsuario(D) devuelve 1 fila", dao.borrarUsuario(idD) == 1);
        verificar("por id, D ya no se encuentra", dao.seleccionarPorId(idD) == null);
        verificar("por perfil B, D ya no sale (queda C)", son(dao.seleccionarPorTipo(tipoB), c));
        verificar("borrar a D otra vez devuelve 0 filas", dao.borrarUsuario(idD) == 0);
        verificar("borrar un id que no existe devuelve 0 filas", dao.borrarUsuario(ID_INEXISTENTE) == 0);
        verificar("A, B y C siguen intactos",
                son(dao.seleccionarPorTipo(tipoA), a, b) && son(dao.seleccionarPorTipo(tipoB), c));
        return new int[]{a, b, c};
    }

    // =========================================================================
    // PRUEBAS DE int_usuarios_procesosDAO
    // =========================================================================
    private static boolean vacia(List<int_usuarios_procesos> lista) {
        return lista != null && lista.isEmpty();
    }

    private static boolean contiene(List<int_usuarios_procesos> lista, int usuario, int proceso) {
        for (int_usuarios_procesos p : lista) {
            if (p.getInt_usuarios_id() == usuario && p.getInt_proceso_id() == proceso) {
                return true;
            }
        }
        return false;
    }

    private static boolean tieneUsuario(List<int_usuarios_procesos> lista, int usuario) {
        for (int_usuarios_procesos p : lista) {
            if (p.getInt_usuarios_id() == usuario) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compara fechas y horas como texto, para no depender de la zona horaria.
     */
    private static boolean igual(int_usuarios_procesos p, String fi, String ff, String hi, String hf) {
        return p != null
                && fi.equals(String.valueOf(p.getFecha_inicio()))
                && ff.equals(String.valueOf(p.getFecha_fin()))
                && hi.equals(String.valueOf(p.getHora_inicio()))
                && hf.equals(String.valueOf(p.getHora_fin()));
    }

    /**
     * Ejecuta las pruebas de asignacion de horarios (alta, consultas filtradas,
     * entradas invalidas, edicion y borrado) y el borrado en cascada al
     * eliminar un usuario.
     *
     * @param dao DAO de la tabla int_usuarios_procesos.
     * @param usuarios DAO de int_usuarios (para borrar un usuario al final).
     * @param a Id del proceso A.
     * @param b Id del proceso B.
     * @param u Ids de los usuarios A, B y C, en ese orden.
     */
    static void probarHorarios(int_usuarios_procesosDAO dao, int_usuariosDAO usuarios,
            int a, int b, int[] u) {
        int ua = u[0];
        int ub = u[1];
        int uc = u[2];

        System.out.println("\n[6] ASIGNAR horarios (insertarProgramacion)");
        verificar("usuario A -> proceso A: inserta 1 fila",
                dao.insertarProgramacion(ua, a, Date.valueOf("2026-11-20"), Date.valueOf("2026-11-27"),
                        Time.valueOf("08:00:00"), Time.valueOf("10:00:00")) == 1);
        verificar("usuario A -> proceso B: inserta 1 fila",
                dao.insertarProgramacion(ua, b, Date.valueOf("2026-12-01"), Date.valueOf("2026-12-05"),
                        Time.valueOf("15:00:00"), Time.valueOf("16:00:00")) == 1);
        verificar("usuario B -> proceso A, pasando la entidad: inserta 1 fila",
                dao.insertarProgramacion(new int_usuarios_procesos(ub, a,
                        Date.valueOf("2026-11-21"), Date.valueOf("2026-11-28"),
                        Time.valueOf("10:00:00"), Time.valueOf("12:00:00"))) == 1);

        System.out.println("\n[7] CONSULTAS FILTRADAS DE HORARIOS (por usuario, por proceso, por ambos)");
        List<int_usuarios_procesos> deA = dao.seleccionarPorUsuario(ua);
        verificar("por usuario A: 2 asignaciones (proceso A y proceso B)",
                deA.size() == 2 && contiene(deA, ua, a) && contiene(deA, ua, b));
        List<int_usuarios_procesos> deB = dao.seleccionarPorUsuario(ub);
        verificar("por usuario B: 1 asignacion (proceso A)", deB.size() == 1 && contiene(deB, ub, a));
        verificar("por usuario sin asignaciones (C): lista vacia, no null", vacia(dao.seleccionarPorUsuario(uc)));
        verificar("por usuario inexistente (0, -1 y 999999): listas vacias",
                vacia(dao.seleccionarPorUsuario(0)) && vacia(dao.seleccionarPorUsuario(-1))
                && vacia(dao.seleccionarPorUsuario(ID_INEXISTENTE)));
        List<int_usuarios_procesos> enA = dao.seleccionarPorProceso(a);
        verificar("por proceso A: aparecen los usuarios A y B, y no el C",
                enA.size() == 2 && tieneUsuario(enA, ua) && tieneUsuario(enA, ub) && !tieneUsuario(enA, uc));
        List<int_usuarios_procesos> enB = dao.seleccionarPorProceso(b);
        verificar("por proceso B: solo el usuario A", enB.size() == 1 && tieneUsuario(enB, ua));
        verificar("por proceso inexistente (0 y 999999): listas vacias",
                vacia(dao.seleccionarPorProceso(0)) && vacia(dao.seleccionarPorProceso(ID_INEXISTENTE)));
        verificar("por usuario y proceso (A, A): trae las fechas y horas guardadas",
                igual(dao.seleccionarPorUsuarioYProceso(ua, a),
                        "2026-11-20", "2026-11-27", "08:00:00", "10:00:00"));
        verificar("usuario sin esa asignacion (C, A): devuelve null",
                dao.seleccionarPorUsuarioYProceso(uc, a) == null);
        verificar("usuario asignado a otro proceso (B, B): devuelve null",
                dao.seleccionarPorUsuarioYProceso(ub, b) == null);
        verificar("proceso inexistente (A, 999999): devuelve null",
                dao.seleccionarPorUsuarioYProceso(ua, ID_INEXISTENTE) == null);
        List<int_usuarios_procesos> todas = dao.seleccionarTodos();
        verificar("seleccionarTodos incluye las 3 asignaciones de prueba",
                contiene(todas, ua, a) && contiene(todas, ua, b) && contiene(todas, ub, a));

        System.out.println("\n[8] ENTRADAS INVALIDAS EN HORARIOS"
                + " (los errores SQL que salen en la consola en este bloque son los esperados)");
        verificar("asignacion repetida (misma llave usuario + proceso): devuelve 0",
                dao.insertarProgramacion(ua, a, Date.valueOf("2027-01-10"), Date.valueOf("2027-01-12"),
                        Time.valueOf("08:00:00"), Time.valueOf("09:00:00")) == 0);
        verificar("usuario inexistente: devuelve 0 (llave foranea)",
                dao.insertarProgramacion(ID_INEXISTENTE, a, Date.valueOf("2027-01-10"), Date.valueOf("2027-01-12"),
                        Time.valueOf("08:00:00"), Time.valueOf("09:00:00")) == 0);
        verificar("proceso inexistente: devuelve 0 (llave foranea)",
                dao.insertarProgramacion(uc, ID_INEXISTENTE, Date.valueOf("2027-01-10"), Date.valueOf("2027-01-12"),
                        Time.valueOf("08:00:00"), Time.valueOf("09:00:00")) == 0);
        verificar("insertarProgramacion(null) devuelve 0",
                dao.insertarProgramacion((int_usuarios_procesos) null) == 0);
        verificar("los rechazos no dejaron filas de mas: el usuario A sigue con 2",
                dao.seleccionarPorUsuario(ua).size() == 2);
        int filasInvertido = dao.insertarProgramacion(uc, a, Date.valueOf("2026-12-10"), Date.valueOf("2026-12-01"),
                Time.valueOf("16:00:00"), Time.valueOf("08:00:00"));
        observar("fecha_fin y hora_fin anteriores a las de inicio",
                filasInvertido == 1 ? "el DAO las ACEPTA, no valida el rango" : "el DAO las rechaza");
        dao.borrarProgramacion(uc, a);
        int filasCruce = dao.insertarProgramacion(uc, a, Date.valueOf("2026-11-20"), Date.valueOf("2026-11-27"),
                Time.valueOf("08:00:00"), Time.valueOf("10:00:00"));
        observar("otro usuario con el mismo horario exacto en el mismo proceso",
                filasCruce == 1 ? "el DAO lo ACEPTA, no valida cruces de horario" : "el DAO lo rechaza");
        dao.borrarProgramacion(uc, a);

        System.out.println("\n[9] EDITAR horarios (editarProgramacion)");
        verificar("editar (A, A) devuelve 1 fila",
                dao.editarProgramacion(ua, a, Date.valueOf("2026-11-22"), Date.valueOf("2026-11-30"),
                        Time.valueOf("14:00:00"), Time.valueOf("17:30:00")) == 1);
        verificar("los nuevos horarios de (A, A) se leen igual",
                igual(dao.seleccionarPorUsuarioYProceso(ua, a),
                        "2026-11-22", "2026-11-30", "14:00:00", "17:30:00"));
        verificar("editar (A, A) no cambia la asignacion (A, B)",
                igual(dao.seleccionarPorUsuarioYProceso(ua, b),
                        "2026-12-01", "2026-12-05", "15:00:00", "16:00:00"));
        int_usuarios_procesos deLaBase = dao.seleccionarPorUsuarioYProceso(ub, a);
        if (deLaBase != null) {
            deLaBase.setHora_fin(Time.valueOf("13:00:00"));
            verificar("editar (B, A) pasando la entidad devuelve 1 fila", dao.editarProgramacion(deLaBase) == 1);
            verificar("la nueva hora_fin de (B, A) se lee igual",
                    igual(dao.seleccionarPorUsuarioYProceso(ub, a),
                            "2026-11-21", "2026-11-28", "10:00:00", "13:00:00"));
        } else {
            verificar("la asignacion (B, A) existe antes de editarla", false);
        }
        verificar("editar una asignacion que no existe (C, A) devuelve 0",
                dao.editarProgramacion(uc, a, Date.valueOf("2026-11-22"), Date.valueOf("2026-11-30"),
                        Time.valueOf("14:00:00"), Time.valueOf("17:30:00")) == 0);
        verificar("editar no crea la asignacion que no existia",
                dao.seleccionarPorUsuarioYProceso(uc, a) == null);

        System.out.println("\n[10] BORRAR horarios (borrarProgramacion, borrarPorUsuario)");
        verificar("borrar (A, B) devuelve 1 fila", dao.borrarProgramacion(ua, b) == 1);
        verificar("la asignacion (A, B) ya no se encuentra",
                dao.seleccionarPorUsuarioYProceso(ua, b) == null);
        verificar("el usuario A conserva su otra asignacion (A, A)",
                dao.seleccionarPorUsuario(ua).size() == 1);
        verificar("borrar (A, B) otra vez devuelve 0", dao.borrarProgramacion(ua, b) == 0);
        verificar("borrar una asignacion que nunca existio (C, A) devuelve 0",
                dao.borrarProgramacion(uc, a) == 0);
        verificar("borrarPorUsuario(B) elimina su 1 asignacion", dao.borrarPorUsuario(ub) == 1);
        verificar("borrarPorUsuario(C), sin asignaciones, devuelve 0", dao.borrarPorUsuario(uc) == 0);
        verificar("borrarPorUsuario(A) elimina la que le queda", dao.borrarPorUsuario(ua) == 1);
        verificar("no queda ninguna asignacion de los usuarios de prueba",
                vacia(dao.seleccionarPorUsuario(ua)) && vacia(dao.seleccionarPorUsuario(ub))
                && vacia(dao.seleccionarPorUsuario(uc)));

        System.out.println("\n[11] BORRAR UN USUARIO se lleva sus horarios (ON DELETE CASCADE)");
        verificar("asignar al usuario C al proceso B: inserta 1 fila",
                dao.insertarProgramacion(uc, b, Date.valueOf("2026-12-15"), Date.valueOf("2026-12-16"),
                        Time.valueOf("09:00:00"), Time.valueOf("10:00:00")) == 1);
        verificar("borrarUsuario(C) devuelve 1 fila", usuarios.borrarUsuario(uc) == 1);
        verificar("el horario de C desaparece junto con el usuario",
                vacia(dao.seleccionarPorUsuario(uc)) && dao.seleccionarPorUsuarioYProceso(uc, b) == null);
        verificar("el proceso B no queda con una asignacion huerfana de C",
                !tieneUsuario(dao.seleccionarPorProceso(b), uc));
    }

    // =========================================================================
    // PREPARACION Y LIMPIEZA
    // =========================================================================
    /**
     * Crea un perfil de usuario temporal. int_usuarios_tipo todavia no tiene
     * clase DAO de implementacion, por eso se hace con JDBC directo.
     */
    private static int crearTipo(Connection c, String letra) throws SQLException {
        String sql = "INSERT INTO int_usuarios_tipo (nombre, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, NOMBRE_TIPO + " " + letra);
            ps.setString(2, "Perfil temporal de la prueba #10");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /**
     * Borra todo lo que crea esta prueba (usuarios, perfiles y procesos
     * temporales; el ON DELETE CASCADE se lleva sus horarios) y deja los
     * contadores de id donde estaban.
     */
    static void limpiar(ConexionDB conexion, int_usuariosDAO usuarios, int_procesoDAO procesos) {
        // Se recorren COPIAS de las listas: los DAO modifican la lista que devolvieron
        // al borrar, y borrar mientras se recorre la original lanza
        // ConcurrentModificationException.
        for (int_usuarios u : new ArrayList<>(
                usuarios.seleccionarPorCampos(null, null, null, null, DOMINIO))) {
            usuarios.borrarUsuario(u.getId());
        }
        try (Statement st = conexion.getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM int_usuarios_tipo WHERE nombre LIKE '" + NOMBRE_TIPO + "%'");
        } catch (SQLException e) {
            System.out.println("    (no se pudieron borrar los perfiles de prueba: " + e.getMessage() + ")");
        }
        for (int_proceso p : new ArrayList<>(procesos.seleccionarTodos())) {
            if (p.getId() != 1 && p.getNombre() != null && p.getNombre().startsWith(NOMBRE_TEMP)) {
                procesos.borrarProceso(p.getId());
            }
        }
        try (Statement st = conexion.getConnection().createStatement()) {
            st.executeUpdate("ALTER TABLE int_usuarios AUTO_INCREMENT = 1");
            st.executeUpdate("ALTER TABLE int_usuarios_tipo AUTO_INCREMENT = 1");
            st.executeUpdate("ALTER TABLE int_proceso AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            System.out.println("    (no se pudo reiniciar AUTO_INCREMENT: " + e.getMessage() + ")");
        }
    }

    /**
     * Punto de entrada de la prueba.
     *
     * @param args Argumentos de linea de comandos (no se usan).
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  PRUEBA TAREA #10: CONSULTAS FILTRADAS Y HORARIOS (CAJA NEGRA)   ");
        System.out.println("==================================================================");
        ConexionDB conexion = null;
        try {
            conexion = new ConexionDB("db_config.txt");
            int_procesoDAO procesoDAO = new int_procesoDAOImp(conexion);
            int_usuariosDAO usuariosDAO = new int_usuariosDAOImp(conexion);
            int_usuarios_procesosDAO horariosDAO = new int_usuarios_procesosDAOImp(conexion);
            int_proceso base = procesoDAO.seleccionarPorId(1);
            if (base == null) {
                fallos++;
                System.out.println("[ERROR] No existe el proceso 1: ingresalo primero (punto 3.b).");
            } else {
                limpiar(conexion, usuariosDAO, procesoDAO); // por si una corrida anterior se corto
                try {
                    int tipoA = crearTipo(conexion.getConnection(), "A");
                    int tipoB = crearTipo(conexion.getConnection(), "B");
                    int_proceso procA = new int_proceso(base.getInt_proceso_tipo_id(), NOMBRE_TEMP + " A",
                            "Primer proceso para probar los horarios", 100.0f, "", "");
                    int_proceso procB = new int_proceso(base.getInt_proceso_tipo_id(), NOMBRE_TEMP + " B",
                            "Segundo proceso para probar los horarios", 100.0f, "", "");
                    if (tipoA <= 0 || tipoB <= 0
                            || procesoDAO.insertarProceso(procA) != 1 || procesoDAO.insertarProceso(procB) != 1) {
                        fallos++;
                        System.out.println("[ERROR] No se pudieron crear los datos de prueba.");
                    } else {
                        int[] ids = probarUsuarios(usuariosDAO, tipoA, tipoB);
                        if (ids != null) {
                            probarHorarios(horariosDAO, usuariosDAO, procA.getId(), procB.getId(), ids);
                        }
                    }
                } finally {
                    limpiar(conexion, usuariosDAO, procesoDAO);
                }
            }
        } catch (Exception e) {
            fallos++;
            System.err.println("[ERROR] No se pudo ejecutar la prueba: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conexion != null) {
                try {
                    conexion.cerrarConexion();
                } catch (SQLException e) {
                    System.err.println("[AVISO] No se pudo cerrar la conexion: " + e.getMessage());
                }
            }
        }
        System.out.println("\n==================================================================");
        System.out.println("  RESULTADO: " + aciertos + " pasan, " + fallos + " fallan");
        System.out.println("==================================================================");
    }
}
