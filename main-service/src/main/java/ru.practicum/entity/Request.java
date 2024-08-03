package ru.practicum.entity;

import lombok.*;
import ru.practicum.enums.RequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
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
