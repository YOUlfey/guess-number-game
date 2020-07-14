package com.github.youlfey.guess.the.number.game.domain;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"updatedDate"})
@ToString
@Entity(name = "contracts")
/*
 * This class resolve following problem:
 * Imagine a situation: user has two tabs in browser
 *                      on first tab user selects one player passing player-simpSessionId in header
 *                      on second tab user selects another player passing another player-simpSessionId in header
 *                      It's allow to play in his created games - it's incorrect behavior
 */
public class ContractPlayerIdWithJSessionId {
    public static final String key = "jSessionId";
    @Id
    @Column(name = "JSESSIONID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "player")
    @NotNull
    private PlayerInstance player;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @Version
    private Integer version;
}
