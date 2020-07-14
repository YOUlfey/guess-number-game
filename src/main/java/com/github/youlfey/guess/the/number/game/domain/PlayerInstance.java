package com.github.youlfey.guess.the.number.game.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "players")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@EqualsAndHashCode(exclude = {"version", "lastUpdate"})
@ToString
public class PlayerInstance {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @UpdateTimestamp
    private LocalDateTime lastUpdate;
    @Version
    private Integer version;
}
