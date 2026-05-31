package com.turggut.sms.domain.student;

import com.turggut.sms.domain.BaseAuditedEntity;
import com.turggut.sms.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_number", nullable = false, unique = true, length = 32)
    private String studentNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "national_id", length = 32)
    private String nationalId;

    @Column(length = 32)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 120)
    private String department;

    @Column(name = "enrollment_year")
    private Integer enrollmentYear;

    @Column(length = 255)
    private String address;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
