package org.example.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concepts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConceptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    public String code;

    @Column(nullable = true)
    public String display;

    @Column(columnDefinition = "TEXT")
    public String definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_system_id", nullable = true)
    public CodeSystemEntity codeSystem;

    // Additional properties that might be useful
    @Column
    public String system; // The code system URL this concept belongs to

    @Column
    public Boolean active = true;

}
