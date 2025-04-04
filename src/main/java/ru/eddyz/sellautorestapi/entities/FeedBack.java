package ru.eddyz.sellautorestapi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FeedBack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double estimation;

    private String text;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "reciver_id", referencedColumnName = "user_id")
    private User receiver;
}
