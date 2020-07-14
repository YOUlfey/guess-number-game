package com.github.youlfey.guess.the.number.game.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "games")
public class GameInstance {
    public static final long defaultExpired = 10L;
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String name;
    @NotNull
    private Integer number;
    @ManyToOne
    @JoinColumn(name = "owner")
    private PlayerInstance owner;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    @NotNull
    private GameState state;
    @Version
    private Integer version;

    // Через сколько удалить игру, если никто не подключится, или если никто ходить не будет (в минутах)
    private Long expired;
}
