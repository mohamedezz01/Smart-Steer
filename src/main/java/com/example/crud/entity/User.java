package com.example.crud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.Date;

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


    public User() {
    }

    public User(int id, String firstName, String lastName, String email, String password, String username, String phone, Date dob, boolean isEmailVerified, String verificationCode, Gender gender, String resetToken, Date resetTokenExpiry) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.username = username;
        this.phone = phone;
        this.dob = dob;
        this.isEmailVerified = isEmailVerified;
        this.verificationCode = verificationCode;
        this.gender = gender;
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(Date tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Date getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(Date resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }


    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDob() {
        return dob;
    }


    public void setDob(Date dob) {
        this.dob = dob;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public enum Gender {
        MALE, FEMALE
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", dob=" + dob +
                ", isEmailVerified=" + isEmailVerified +
                ", verificationCode='" + verificationCode + '\'' +
                ", gender=" + gender +
                '}';
    }
}
