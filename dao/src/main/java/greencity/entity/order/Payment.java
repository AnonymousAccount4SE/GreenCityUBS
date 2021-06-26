package greencity.entity.order;

import javax.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "payment")
@EqualsAndHashCode
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 3, nullable = false)
    private String currency;
    @Column(length = 12, nullable = false)
    private Long amount;
    @Column(length = 50, nullable = false)
    private String orderStatus;
    @Column(length = 50)
    private String responseStatus;
    @Column(length = 16)
    private String senderCellPhone;
    @Column(length = 50)
    private String senderAccount;
    @Column(length = 19)
    private String maskedCard;
    @Column(length = 50)
    private String cardType;
    @Column(length = 4)
    private Integer responseCode;
    @Column(length = 1024)
    private String responseDescription;
    @Column(length = 19)
    private String orderTime;
    @Column(length = 10)
    private String settlementDate;
    @Column(length = 12)
    private Long fee;
    @Column(length = 50)
    private String paymentSystem;
    @Column(length = 254)
    private String senderEmail;
    @Column(length = 19)
    private Long paymentId;
    @ManyToOne
    private Order order;
    @Column
    private String comment;
}
