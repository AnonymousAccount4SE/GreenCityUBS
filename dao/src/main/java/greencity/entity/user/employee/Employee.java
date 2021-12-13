package greencity.entity.user.employee;

import greencity.entity.enums.EmployeeStatus;
import greencity.entity.order.Order;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@EqualsAndHashCode(exclude = {"employeePosition", "attachedOrders", "employeeOrderPositions", "employee", "orders"})
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 30, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 30, nullable = false)
    private String lastName;

    @Column(name = "phone_number", length = 30, nullable = false, unique = true)
    private String phoneNumber;

    @Column(length = 170, unique = true)
    private String email;

    @Column(name = "image_path")
    private String imagePath;

    @Column(nullable = false, name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;

    @ManyToMany
    @JoinTable(
        name = "employee_position",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "position_id")})
    private Set<Position> employeePosition;

    @ManyToMany
    @JoinTable(
        name = "order_employee",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "order_id")})
    private Set<Order> attachedOrders;

    @ManyToMany
    @JoinTable(
        name = "employee_receiving_station_mapping",
        joinColumns = {@JoinColumn(name = "employee_id")},
        inverseJoinColumns = {@JoinColumn(name = "receiving_station_id")})
    private Set<ReceivingStation> receivingStation;

    @OneToMany(mappedBy = "employee")
    @Cascade(org.hibernate.annotations.CascadeType.DETACH)
    private Set<EmployeeOrderPosition> employeeOrderPositions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "blockedByEmployee")
    private Set<Order> orders;
}
