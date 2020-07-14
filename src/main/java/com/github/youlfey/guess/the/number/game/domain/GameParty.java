package com.github.youlfey.guess.the.number.game.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "game_parties")
public class GameParty {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "player")
    private PlayerInstance player;

    @OneToOne
    @JoinColumn(name = "game")
    private GameInstance game;

    @Version
    private Integer version;
}
