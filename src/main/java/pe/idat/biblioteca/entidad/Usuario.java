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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Usuario de la biblioteca (estudiante, docente o administrador).
 * Relaciones: muchos a muchos con {@link Rol} y uno a muchos con {@link Prestamo}.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombres;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String apellidos;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    @Column(name = "codigo_universitario", nullable = false, unique = true, length = 20)
    private String codigoUniversitario;

    /** Contraseña cifrada con BCrypt (nunca en texto plano). */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Prestamo> prestamos = new ArrayList<>();

    protected Usuario() {
        // JPA
    }

    public Usuario(String nombres, String apellidos, String email, String codigoUniversitario, String password) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.codigoUniversitario = codigoUniversitario;
        this.password = password;
    }

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public boolean tieneRol(NombreRol nombreRol) {
        return roles.stream().anyMatch(r -> r.getNombre() == nombreRol);
    }

    public void asignarRoles(Set<Rol> nuevosRoles) {
        roles.clear();
        roles.addAll(nuevosRoles);
    }

    public void actualizarDatos(String nombres, String apellidos, String email) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
    }

    public void cambiarPassword(String passwordCifrada) { this.password = passwordCifrada; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreCompleto() { return nombres + " " + apellidos; }
    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getCodigoUniversitario() { return codigoUniversitario; }
    public String getPassword() { return password; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public Set<Rol> getRoles() { return roles; }
    public List<Prestamo> getPrestamos() { return prestamos; }

    /** Solo para pruebas unitarias. */
    public void asignarId(Long id) { this.id = id; }
}
