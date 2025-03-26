package com.example.crud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "pass")
    private String password;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "phone")
    private String phone;

    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private Date dob;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    @Column(name = "verification_code")
    private String verificationCode;

    @Transient
    private String confirmPass;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private Date resetTokenExpiry;

    @Column(name = "token_expiration")
    private Date tokenExpiration;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "deletion_token")
    private String deletionToken;

    @Column(name = "deletion_token_expiry")
    private Date deletionTokenExpiry;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Column(name = "roles")
    private String roles;

    public enum Gender {
        MALE, FEMALE
    }
}
