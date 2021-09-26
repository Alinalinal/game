package com.game.service;

import com.game.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlayerService {
    Player getByID(Long id);

    void save(Player player);

    void delete(Long id);

    List<Player> getAll(Specification<Player> specification, Pageable pageable);

    Integer getCount(Specification<Player> specification);
}
