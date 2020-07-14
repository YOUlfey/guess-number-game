package com.github.youlfey.guess.the.number.game.pojo.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class GameCreateRequest {
    @NotNull
    private String name;
    @NotNull
    private Integer number;

    // Через сколько удалить игру, если никто не подключится, или если никто ходить не будет (в минутах)
    private Long expired;
}
