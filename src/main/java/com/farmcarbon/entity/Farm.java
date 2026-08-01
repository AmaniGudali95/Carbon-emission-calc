package com.farmcarbon.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="farms")
@Getter
@Setter
@NoArgsConstructor
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(name="size_hectares", nullable = false)
    private BigDecimal sizeHectares;

    @Column(name = "crop_type")
    private String cropType;

    public Farm(String name, String location, BigDecimal sizeHectares, String cropType){
        this.name=name;
        this.location=location;
        this.sizeHectares=sizeHectares;
        this.cropType=cropType;
    }
}
