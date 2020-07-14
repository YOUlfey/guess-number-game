package com.github.youlfey.guess.the.number.game.domain;

import com.github.youlfey.guess.the.number.game.pojo.PartyAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Entity(name = "game_records")
public class GameActionRecord {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @NotNull
    private Integer tryNumber;

    private PartyAction action;

    private boolean approved;

    private boolean result;

    private boolean canLie;

    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    @ManyToOne
    @JoinColumn(name = "party")
    private GameParty party;

    @Version
    private Integer version;

    public boolean isNotApproved() {
        return !approved;
    }
}
