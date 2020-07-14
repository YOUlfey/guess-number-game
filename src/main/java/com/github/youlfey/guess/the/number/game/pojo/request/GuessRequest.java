package com.github.youlfey.guess.the.number.game.pojo.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GuessRequest {
    @NotNull
    private Integer number;

    private UUID gameId;
}
