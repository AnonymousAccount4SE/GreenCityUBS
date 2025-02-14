package greencity.entity.order;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
@EqualsAndHashCode(exclude = {"order"})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime eventDate;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "author", nullable = false)
    private String authorName;

    @ManyToOne
    private Order order;
}