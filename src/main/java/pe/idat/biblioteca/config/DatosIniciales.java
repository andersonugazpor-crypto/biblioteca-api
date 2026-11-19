package pe.idat.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.entidad.Categoria;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.entidad.NombreRol;
import pe.idat.biblioteca.entidad.Prestamo;
import pe.idat.biblioteca.entidad.Rol;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.repositorio.CategoriaRepositorio;
import pe.idat.biblioteca.repositorio.LibroRepositorio;
import pe.idat.biblioteca.repositorio.PrestamoRepositorio;
import pe.idat.biblioteca.repositorio.RolRepositorio;
import pe.idat.biblioteca.repositorio.UsuarioRepositorio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Carga datos de ejemplo solo si la base está vacía: roles, usuarios de prueba,
 * categorías, libros y algunos préstamos (uno activo, uno vencido y uno devuelto).
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosIniciales.class);

    private final RolRepositorio roles;
    private final UsuarioRepositorio usuarios;
    private final CategoriaRepositorio categorias;
    private final LibroRepositorio libros;
    private final PrestamoRepositorio prestamos;
    private final PasswordEncoder encoder;
    private final Clock reloj;

    public DatosIniciales(RolRepositorio roles, UsuarioRepositorio usuarios, CategoriaRepositorio categorias,
                          LibroRepositorio libros, PrestamoRepositorio prestamos, PasswordEncoder encoder, Clock reloj) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.libros = libros;
        this.prestamos = prestamos;
        this.encoder = encoder;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol admin = roles.findByNombre(NombreRol.ADMIN)
                .orElseGet(() -> roles.save(new Rol(NombreRol.ADMIN, "Gestiona libros, usuarios y préstamos")));
        Rol usuario = roles.findByNombre(NombreRol.USUARIO)
                .orElseGet(() -> roles.save(new Rol(NombreRol.USUARIO, "Consulta el catálogo y su historial de préstamos")));
        if (usuarios.count() > 0) {
            return;
        }

        Usuario administrador = crearUsuario("Ana", "Torres Vega", "admin@biblioteca.edu.pe", "ADM000001", "Admin123*", Set.of(admin));
        Usuario carlos = crearUsuario("Carlos", "Quispe Mamani", "carlos.quispe@idat.edu.pe", "U20240001", "Usuario123*", Set.of(usuario));
        Usuario maria = crearUsuario("María", "Flores Huamán", "maria.flores@idat.edu.pe", "U20240002", "Usuario123*", Set.of(usuario));
        crearUsuario("Jorge", "Ramos Díaz", "jorge.ramos@idat.edu.pe", "U20240003", "Usuario123*", Set.of(usuario));

        Map<String, Categoria> cat = new HashMap<>();
        for (String[] c : new String[][]{
                {"Programación", "Lenguajes, algoritmos y buenas prácticas"},
                {"Bases de Datos", "Modelado, SQL y NoSQL"},
                {"Arquitectura de Software", "Diseño, patrones y microservicios"},
                {"Redes", "Redes de computadoras y comunicaciones"},
                {"Matemática", "Cálculo, álgebra y estadística"},
                {"Literatura", "Narrativa y poesía"}}) {
            cat.put(c[0], categorias.save(new Categoria(c[0], c[1])));
        }

        Libro cleanCode = libro("9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall", 2008, 3, cat, "Programación", "Arquitectura de Software");
        Libro effective = libro("9780134685991", "Effective Java", "Joshua Bloch", "Addison-Wesley", 2018, 2, cat, "Programación");
        libro("9781617294945", "Spring in Action", "Craig Walls", "Manning", 2018, 2, cat, "Programación", "Arquitectura de Software");
        Libro patrones = libro("9780201633610", "Design Patterns", "Erich Gamma y otros", "Addison-Wesley", 1994, 2, cat, "Arquitectura de Software");
        libro("9781492034025", "Building Microservices", "Sam Newman", "O'Reilly", 2021, 1, cat, "Arquitectura de Software");
        libro("9780073523323", "Database System Concepts", "Abraham Silberschatz", "McGraw-Hill", 2010, 4, cat, "Bases de Datos");
        libro("9781449373320", "Designing Data-Intensive Applications", "Martin Kleppmann", "O'Reilly", 2017, 2, cat, "Bases de Datos", "Arquitectura de Software");
        libro("9780133594140", "Computer Networking: A Top-Down Approach", "James Kurose", "Pearson", 2016, 3, cat, "Redes");
        libro("9780538497817", "Cálculo de una variable", "James Stewart", "Cengage", 2012, 5, cat, "Matemática");
        Libro cienAnios = libro("9780307474728", "Cien años de soledad", "Gabriel García Márquez", "Vintage Español", 2009, 2, cat, "Literatura");
        libro("9788420471839", "La ciudad y los perros", "Mario Vargas Llosa", "Alfaguara", 2012, 2, cat, "Literatura");
        libro("9786124169014", "Los ríos profundos", "José María Arguedas", "Penguin Random House", 2016, 1, cat, "Literatura");

        LocalDate hoy = LocalDate.now(reloj);
        prestar(carlos, cleanCode, hoy.minusDays(3), 14);         // activo y al día
        prestar(maria, effective, hoy.minusDays(20), 7);          // vencido (13 días de retraso)
        Prestamo devuelto = prestar(carlos, patrones, hoy.minusDays(30), 7);
        devuelto.registrarDevolucion(hoy.minusDays(21), new BigDecimal("1.00"), "Devuelto con 2 días de retraso");
        Prestamo devuelto2 = prestar(maria, cienAnios, hoy.minusDays(15), 10);
        devuelto2.registrarDevolucion(hoy.minusDays(6), new BigDecimal("1.00"), null);

        LOG.info("Datos de ejemplo cargados: 4 usuarios, {} categorías, {} libros, 4 préstamos", cat.size(), libros.count());
    }

    private Usuario crearUsuario(String nombres, String apellidos, String email, String codigo, String clave, Set<Rol> rolesUsuario) {
        Usuario u = new Usuario(nombres, apellidos, email, codigo, encoder.encode(clave));
        u.asignarRoles(rolesUsuario);
        return usuarios.save(u);
    }

    private Libro libro(String isbn, String titulo, String autor, String editorial, int anio, int ejemplares,
                        Map<String, Categoria> cat, String... nombresCategoria) {
        Libro l = new Libro(isbn, titulo, autor, editorial, anio, ejemplares);
        Set<Categoria> asignadas = new java.util.HashSet<>();
        for (String n : nombresCategoria) {
            asignadas.add(cat.get(n));
        }
        l.asignarCategorias(asignadas);
        return libros.save(l);
    }

    private Prestamo prestar(Usuario u, Libro l, LocalDate fecha, int dias) {
        l.prestarEjemplar();
        return prestamos.save(new Prestamo(u, l, fecha, dias, null));
    }
}
