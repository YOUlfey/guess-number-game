package com.github.youlfey.guess.the.number.game.pojo.response.content;

import com.github.youlfey.guess.the.number.game.domain.GameInstance;
import com.github.youlfey.guess.the.number.game.domain.PlayerInstance;
import com.github.youlfey.guess.the.number.game.domain.projection.GameProjectionForView;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class GamesResponseForPlayer extends ContentResponse {
    private final PageResponse<GameProjectionForView> createdGames;
    private final PageResponse<GameProjectionForView> availableGames;
    private final PageResponse<GameProjectionForView> currentGames;

    public GamesResponseForPlayer(PlayerInstance player, Page<GameInstance> createdGames,
                                  Page<GameInstance> availableGames, Page<GameInstance> currentGames) {
        super(player.getId());
        this.createdGames = convertToPageResponse(createdGames);
        this.availableGames = convertToPageResponse(availableGames);
        this.currentGames = convertToPageResponse(currentGames);
    }

    private PageResponse<GameProjectionForView> convertToPageResponse(Page<GameInstance> pageGames) {
        return PageResponse
                .<GameProjectionForView>builder()
                .content(projectToView(pageGames))
                .page(pageGames.getNumber())
                .size(pageGames.getSize())
                .totalElements(pageGames.getTotalElements())
                .totalPages(pageGames.getTotalPages())
                .build();
    }

    private Collection<GameProjectionForView> projectToView(Page<GameInstance> pageGames) {
        return pageGames.getContent().stream().map(GameProjectionForView::new).collect(toList());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PageResponse<T> {
        private Collection<T> content;
        private Integer page;
        private Integer size;
        private Integer totalPages;
        private Long totalElements;
    }
}
