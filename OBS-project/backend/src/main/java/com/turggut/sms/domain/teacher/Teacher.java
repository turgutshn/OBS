package com.turggut.sms.domain.teacher;

import com.turggut.sms.domain.BaseAuditedEntity;
import com.turggut.sms.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseAuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_number", nullable = false, unique = true, length = 32)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(length = 40)
    private String title;

    @Column(length = 120)
    private String department;

    @Column(length = 32)
    private String phone;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
