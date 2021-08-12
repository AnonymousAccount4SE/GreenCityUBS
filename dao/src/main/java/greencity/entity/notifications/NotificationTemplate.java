package greencity.entity.notifications;

import greencity.entity.enums.NotificationType;
import greencity.entity.language.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "notification_type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "body", length = 1500)
    private String body;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;
}