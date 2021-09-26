package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public Player getByID(Long id) {
        Optional<Player> optionalPlayer = playerRepository.findById(id);
        return optionalPlayer.orElse(null);
    }

    @Override
    public void save(Player player) {
        playerRepository.save(player);
    }

    @Override
    public void delete(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public List<Player> getAll(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable).toList();
    }

    @Override
    public Integer getCount(Specification<Player> specification) {
        return Long.valueOf(playerRepository.count(specification)).intValue();
    }
}
