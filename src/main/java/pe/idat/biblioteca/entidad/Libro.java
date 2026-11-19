package pe.idat.biblioteca.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Libro del catálogo. Controla sus propios ejemplares disponibles.
 * Relaciones: muchos a muchos con {@link Categoria} y uno a muchos con {@link Prestamo}.
 * {@code @Version} activa el bloqueo optimista: si dos préstamos simultáneos
 * modifican el mismo libro, el segundo falla en lugar de dejar el stock inconsistente.
 */
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 10, max = 17)
    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String autor;

    @Size(max = 100)
    @Column(length = 100)
    private String editorial;

    @Min(1450)
    @Max(2100)
    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Min(0)
    @Column(name = "ejemplares_totales", nullable = false)
    private int ejemplaresTotales;

    @Min(0)
    @Column(name = "ejemplares_disponibles", nullable = false)
    private int ejemplaresDisponibles;

    /** Borrado lógico: un libro con historial de préstamos no se elimina físicamente. */
    @Column(nullable = false)
    private boolean activo = true;

    @Version
    private Long version;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "libro_categorias",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    private List<Prestamo> prestamos = new ArrayList<>();

    protected Libro() {
        // JPA
    }

    public Libro(String isbn, String titulo, String autor, String editorial, Integer anioPublicacion, int ejemplares) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.ejemplaresTotales = ejemplares;
        this.ejemplaresDisponibles = ejemplares;
    }

    public void prestarEjemplar() {
        if (!activo) {
            throw new ReglaNegocioException("El libro \"" + titulo + "\" está dado de baja");
        }
        if (ejemplaresDisponibles <= 0) {
            throw new ReglaNegocioException("No hay ejemplares disponibles de \"" + titulo + "\"");
        }
        ejemplaresDisponibles--;
    }

    public void devolverEjemplar() {
        if (ejemplaresDisponibles >= ejemplaresTotales) {
            throw new ReglaNegocioException("Todos los ejemplares de \"" + titulo + "\" ya están en biblioteca");
        }
        ejemplaresDisponibles++;
    }

    public int getEjemplaresPrestados() {
        return ejemplaresTotales - ejemplaresDisponibles;
    }

    /** Cambia el total de ejemplares sin permitir que quede por debajo de los prestados. */
    public void actualizarEjemplaresTotales(int nuevoTotal) {
        int prestados = getEjemplaresPrestados();
        if (nuevoTotal < prestados) {
            throw new ReglaNegocioException("No se puede reducir a " + nuevoTotal
                    + " ejemplares: hay " + prestados + " prestados");
        }
        this.ejemplaresTotales = nuevoTotal;
        this.ejemplaresDisponibles = nuevoTotal - prestados;
    }

    public void actualizarDatos(String isbn, String titulo, String autor, String editorial, Integer anioPublicacion) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
    }

    public void asignarCategorias(Set<Categoria> nuevas) {
        categorias.clear();
        categorias.addAll(nuevas);
    }

    public void darDeBaja() {
        if (getEjemplaresPrestados() > 0) {
            throw new ReglaNegocioException("No se puede dar de baja \"" + titulo + "\": tiene ejemplares prestados");
        }
        this.activo = false;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEditorial() { return editorial; }
    public Integer getAnioPublicacion() { return anioPublicacion; }
    public int getEjemplaresTotales() { return ejemplaresTotales; }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public boolean isActivo() { return activo; }
    public Long getVersion() { return version; }
    public Set<Categoria> getCategorias() { return categorias; }
    public List<Prestamo> getPrestamos() { return prestamos; }

    /** Solo para pruebas unitarias. */
    public void asignarId(Long id) { this.id = id; }
}
