package com.mcly.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 32, unique = true)
    private String phone;

    @Column(nullable = false, length = 32)
    private String level;

    @Column(name = "face_bound", nullable = false)
    private boolean faceBound;

    protected Member() {
    }

    public Member(String name, String phone, String level) {
        this.name = name;
        this.phone = phone;
        this.level = level;
        this.faceBound = false;
    }

    public Long getId() {
        return id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLevel() {
        return level;
    }

    public boolean isFaceBound() {
        return faceBound;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
