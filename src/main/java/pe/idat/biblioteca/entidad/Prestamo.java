package pe.idat.biblioteca.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Préstamo de un libro a un usuario. Relación muchos a uno con {@link Usuario} y con {@link Libro}.
 * Conoce sus fechas, por lo que calcula los días de retraso y la multa.
 */
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @NotNull
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal multa = BigDecimal.ZERO;

    @Size(max = 250)
    @Column(length = 250)
    private String observaciones;

    protected Prestamo() {
        // JPA
    }

    public Prestamo(Usuario usuario, Libro libro, LocalDate fechaPrestamo, int diasPrestamo, String observaciones) {
        this.usuario = usuario;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaPrestamo.plusDays(diasPrestamo);
        this.observaciones = observaciones;
    }

    public boolean estaVencido(LocalDate hoy) {
        return estado == EstadoPrestamo.ACTIVO && hoy.isAfter(fechaVencimiento);
    }

    public long diasRetraso(LocalDate referencia) {
        LocalDate fin = fechaDevolucion != null ? fechaDevolucion : referencia;
        return Math.max(0, ChronoUnit.DAYS.between(fechaVencimiento, fin));
    }

    /** Registra la devolución, calcula la multa y devuelve el ejemplar al libro. */
    public void registrarDevolucion(LocalDate fecha, BigDecimal multaPorDia, String observaciones) {
        if (estado == EstadoPrestamo.DEVUELTO) {
            throw new ReglaNegocioException("El préstamo " + id + " ya fue devuelto el " + fechaDevolucion);
        }
        if (fecha.isBefore(fechaPrestamo)) {
            throw new ReglaNegocioException("La fecha de devolución no puede ser anterior al préstamo");
        }
        this.fechaDevolucion = fecha;
        this.estado = EstadoPrestamo.DEVUELTO;
        this.multa = multaPorDia.multiply(BigDecimal.valueOf(diasRetraso(fecha))).setScale(2, RoundingMode.HALF_UP);
        if (observaciones != null && !observaciones.isBlank()) {
            this.observaciones = observaciones;
        }
        libro.devolverEjemplar();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Libro getLibro() { return libro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public EstadoPrestamo getEstado() { return estado; }
    public BigDecimal getMulta() { return multa; }
    public String getObservaciones() { return observaciones; }

    /** Solo para pruebas unitarias. */
    public void asignarId(Long id) { this.id = id; }
}
