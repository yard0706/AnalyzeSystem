package ru.vrn.rt.analyzesystem.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tac",
        uniqueConstraints = @UniqueConstraint(columnNames = "tac"))
public class Tac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String tac;
    private String description;

    @Override
    public String toString() {
        return "Tac{" +
                "id=" + id +
                ", tac='" + tac + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTac() {
        return tac;
    }

    public void setTac(String tac) {
        this.tac = tac;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
