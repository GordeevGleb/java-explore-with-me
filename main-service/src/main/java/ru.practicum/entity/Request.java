package ru.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.enums.RequestStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@EqualsAndHashCode
@Table(name = "requests")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event", referencedColumnName = "id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester", referencedColumnName = "id")
    private User requester;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
