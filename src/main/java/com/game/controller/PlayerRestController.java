package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerRestController {

    @Autowired
    private PlayerService playerService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Player>> getPlayerList(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String title,
                                                      @RequestParam(required = false) Race race,
                                                      @RequestParam(required = false) Profession profession,
                                                      @RequestParam(required = false) Long after,
                                                      @RequestParam(required = false) Long before,
                                                      @RequestParam(required = false) Boolean banned,
                                                      @RequestParam(required = false) Integer minExperience,
                                                      @RequestParam(required = false) Integer maxExperience,
                                                      @RequestParam(required = false) Integer minLevel,
                                                      @RequestParam(required = false) Integer maxLevel,
                                                      @RequestParam(required = false) PlayerOrder order,
                                                      @RequestParam(required = false) Integer pageNumber,
                                                      @RequestParam(required = false) Integer pageSize) {

        Specification<Player> specification = formSpecification(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel);

        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 3;
        }
        if (order == null) {
            order = PlayerOrder.ID;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.name().toLowerCase()));
        List<Player> playerList = this.playerService.getAll(specification, pageable);

        return new ResponseEntity<>(playerList, HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPlayerCount(@RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) Race race,
                                                  @RequestParam(required = false) Profession profession,
                                                  @RequestParam(required = false) Long after,
                                                  @RequestParam(required = false) Long before,
                                                  @RequestParam(required = false) Boolean banned,
                                                  @RequestParam(required = false) Integer minExperience,
                                                  @RequestParam(required = false) Integer maxExperience,
                                                  @RequestParam(required = false) Integer minLevel,
                                                  @RequestParam(required = false) Integer maxLevel) {

        Specification<Player> specification = formSpecification(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel);
        Integer playerCount = this.playerService.getCount(specification);

        return new ResponseEntity<>(playerCount, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long playerId) {
        if (!isIDPassValidation(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Player player = this.playerService.getByID(playerId);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") Long playerId) {
        if (!isIDPassValidation(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Player player = this.playerService.getByID(playerId);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.playerService.delete(playerId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> createPlayer(@RequestBody(required = false) Player player) {
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String name = player.getName();
        String title = player.getTitle();
        Race race = player.getRace();
        Profession profession = player.getProfession();
        Date birthday = player.getBirthday();
        Integer experience = player.getExperience();

        if (!isNamePassValidation(name) || !isTitlePassValidation(title) || race == null || profession == null
                || !isDatePassValidation(birthday) || !isExperiencePassValidation(experience)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (player.getBanned() == null) {
            player.setBanned(false);
        }

        Integer level = calcLevelOfPlayer(experience);
        player.setLevel(level);
        player.setUntilNextLevel(calcExperienceNeededToReachNextLevel(experience, level));
        this.playerService.save(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long playerId,
                                               @RequestBody(required = false) Player updatedPlayer) {

        if (!isIDPassValidation(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Player player = this.playerService.getByID(playerId);

        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (updatedPlayer == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String name = updatedPlayer.getName();
        String title = updatedPlayer.getTitle();
        Race race = updatedPlayer.getRace();
        Profession profession = updatedPlayer.getProfession();
        Date birthday = updatedPlayer.getBirthday();
        Boolean banned = updatedPlayer.getBanned();
        Integer experience = updatedPlayer.getExperience();

        if ((name != null && !isNamePassValidation(name)) || (title != null && !isTitlePassValidation(title))
                || (birthday != null && !isDatePassValidation(birthday))
                || (experience != null && !isExperiencePassValidation(experience))) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (name != null) {
            player.setName(name);
        }
        if (title != null) {
            player.setTitle(title);
        }
        if (race != null) {
            player.setRace(race);
        }
        if (profession != null) {
            player.setProfession(profession);
        }
        if (birthday != null) {
            player.setBirthday(birthday);
        }
        if (banned != null) {
            player.setBanned(banned);
        }
        if (experience != null) {
            player.setExperience(experience);
            Integer level = calcLevelOfPlayer(experience);
            player.setLevel(level);
            player.setUntilNextLevel(calcExperienceNeededToReachNextLevel(experience, level));
        }

        this.playerService.save(player);

        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    private Specification<Player> formSpecification(String name, String title, Race race,
                                                    Profession profession, Long after, Long before,
                                                    Boolean banned, Integer minExperience,
                                                    Integer maxExperience, Integer minLevel,
                                                    Integer maxLevel) {

        Specification<Player> specification = (playerRoot, query, builder) ->
                builder.isTrue(builder.literal(true));

        if (name != null) {
            Specification<Player> filterName = (playerRoot, query, builder) ->
                    builder.like(playerRoot.get("name"), "%" + name + "%");
            specification = specification.and(filterName);
        } //
        if (title != null) {
            Specification<Player> filterTitle = (playerRoot, query, builder) ->
                    builder.like(playerRoot.get("title"), "%" + title + "%");
            specification = specification != null ? specification.and(filterTitle) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (race != null) {
            Specification<Player> filterRace = (playerRoot, query, builder) ->
                    builder.equal(playerRoot.get("race"), race);
            specification = specification != null ? specification.and(filterRace) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (profession != null) {
            Specification<Player> filterProfession = (playerRoot, query, builder) ->
                    builder.equal(playerRoot.get("profession"), profession);
            specification = specification != null ? specification.and(filterProfession) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        } //

        if (after != null) {
            Specification<Player> filterAfter = (playerRoot, query, builder) ->
                    builder.greaterThanOrEqualTo(playerRoot.get("birthday"), new Date(after));
            specification = specification != null ? specification.and(filterAfter) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (before != null) {
            Specification<Player> filterBefore = (playerRoot, query, builder) ->
                    builder.lessThanOrEqualTo(playerRoot.get("birthday"), new Date(before));
            specification = specification != null ? specification.and(filterBefore) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (banned != null) {
            Specification<Player> filterBanned = (playerRoot, query, builder) ->
                    builder.equal(playerRoot.get("banned"), banned);
            specification = specification != null ? specification.and(filterBanned) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        } //

        if (minExperience != null) {
            Specification<Player> filterMinExperience = (playerRoot, query, builder) ->
                    builder.ge(playerRoot.get("experience"), minExperience);
            specification = specification != null ? specification.and(filterMinExperience) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (maxExperience != null) {
            Specification<Player> filterMaxExperience = (playerRoot, query, builder) ->
                    builder.le(playerRoot.get("experience"), maxExperience);
            specification = specification != null ? specification.and(filterMaxExperience) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        } //

        if (minLevel != null) {
            Specification<Player> filterMinLevel = (playerRoot, query, builder) ->
                    builder.ge(playerRoot.get("level"), minLevel);
            specification = specification != null ? specification.and(filterMinLevel) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        if (maxLevel != null) {
            Specification<Player> filterMaxLevel = (playerRoot, query, builder) ->
                    builder.le(playerRoot.get("level"), maxLevel);
            specification = specification != null ? specification.and(filterMaxLevel) :
                    (playerRoot, query, builder) -> builder.isTrue(builder.literal(true));
        }

        return specification;
    }

    private Integer calcLevelOfPlayer(Integer experience) {
        return (int)((Math.sqrt(2500D + 200D * experience) - 50) / 100);
    }

    private Integer calcExperienceNeededToReachNextLevel (Integer experience, Integer level) {
        return 50 * (level + 1) * (level + 2) - experience;
    }

    private Boolean isIDPassValidation(Long id) {
        if (id == null) {
            return false;
        }
        return (id > 0);
    }

    private Boolean isNamePassValidation(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return (name.length() <= 12);
    }

    private Boolean isTitlePassValidation(String title) {
        if (title == null) {
            return false;
        }
        return (title.length() <= 30);
    }

    private Boolean isDatePassValidation(Date birthday) {
        if (birthday == null) {
            return false;
        }
        long birthdayMilliseconds = birthday.getTime();
        long minBirthdayMilliseconds = Date.valueOf("2000-01-01").getTime();
        long maxBirthdayMilliseconds = Date.valueOf("3001-01-01").getTime();
        return (birthdayMilliseconds >= minBirthdayMilliseconds && birthdayMilliseconds < maxBirthdayMilliseconds);
    }

    private Boolean isExperiencePassValidation(Integer experience) {
        if (experience == null) {
            return false;
        }
        return (experience >= 0 && experience <= 10000000);
    }
}
