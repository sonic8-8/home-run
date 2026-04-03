package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.domain.world.WorldAiNewsSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.WorldAiNewsSeedPolicy.AiNewsSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.EventChoiceSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.EventConditionSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.EventEffectSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.EventSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.NewsSeed;
import io.ssafy.p.j14c103.homerun.domain.world.WorldContentSeedPolicy.WorldContentSeedPlan;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventCondition;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffect;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorldContentSeedService {

    private final WorldAiNewsSeedPolicy worldAiNewsSeedPolicy;
    private final WorldContentSeedPolicy worldContentSeedPolicy;
    private final NewsMasterRepository newsMasterRepository;
    private final GameEventRepository gameEventRepository;
    private final EventChoiceRepository eventChoiceRepository;
    private final EventConditionRepository eventConditionRepository;
    private final EventEffectRepository eventEffectRepository;

    public WorldContentSeedService(
        final NewsMasterRepository newsMasterRepository,
        final GameEventRepository gameEventRepository,
        final EventChoiceRepository eventChoiceRepository,
        final EventConditionRepository eventConditionRepository,
        final EventEffectRepository eventEffectRepository
    ) {
        this.worldAiNewsSeedPolicy = new WorldAiNewsSeedPolicy();
        this.worldContentSeedPolicy = new WorldContentSeedPolicy();
        this.newsMasterRepository = newsMasterRepository;
        this.gameEventRepository = gameEventRepository;
        this.eventChoiceRepository = eventChoiceRepository;
        this.eventConditionRepository = eventConditionRepository;
        this.eventEffectRepository = eventEffectRepository;
    }

    public void seed() {
        final WorldContentSeedPlan seedPlan = worldContentSeedPolicy.calculate();
        seedNews(seedPlan.newsSeeds());
        seedFallbackAiNews(worldAiNewsSeedPolicy.calculate());
        seedEvents(seedPlan.eventSeeds());
    }

    private void seedNews(final List<NewsSeed> newsSeeds) {
        newsSeeds.forEach(this::seedNews);
    }

    private void seedNews(final NewsSeed newsSeed) {
        if (newsMasterRepository.existsById(newsSeed.newsId())) {
            return;
        }

        newsMasterRepository.save(
            NewsMaster.create(
                newsSeed.newsId(),
                newsSeed.title(),
                newsSeed.category(),
                newsSeed.sentiment(),
                newsSeed.sectorImpact(),
                newsSeed.exchangeRateImpact(),
                newsSeed.realEstateImpact(),
                newsSeed.jobImpact()
            )
        );
    }

    private void seedFallbackAiNews(final List<AiNewsSeed> aiNewsSeeds) {
        aiNewsSeeds.forEach(this::seedFallbackAiNews);
    }

    private void seedFallbackAiNews(final AiNewsSeed aiNewsSeed) {
        if (newsMasterRepository.existsByEconomicCycleType(aiNewsSeed.economicCycleType())) {
            return;
        }

        newsMasterRepository.save(
            NewsMaster.createAiNews(
                aiNewsSeed.newsId(),
                aiNewsSeed.title(),
                aiNewsSeed.sentiment(),
                aiNewsSeed.sourceName(),
                aiNewsSeed.articleText(),
                aiNewsSeed.economicCycleType(),
                aiNewsSeed.reason(),
                null,
                null,
                null,
                null
            )
        );
    }

    private void seedEvents(final List<EventSeed> eventSeeds) {
        eventSeeds.forEach(this::seedEvent);
    }

    private void seedEvent(final EventSeed eventSeed) {
        final GameEvent gameEvent = gameEventRepository.findByEventCode(eventSeed.eventCode())
            .orElseGet(() -> gameEventRepository.save(toGameEvent(eventSeed)));

        if (!hasChoices(gameEvent.getGameEventId())) {
            seedChoices(gameEvent.getGameEventId(), eventSeed.choices());
        }
        seedConditions(gameEvent.getGameEventId(), eventSeed.conditions());
        seedEffects(gameEvent.getGameEventId(), eventSeed.effects());
    }

    private GameEvent toGameEvent(final EventSeed eventSeed) {
        return GameEvent.create(
            eventSeed.eventTypeCode(),
            eventSeed.eventCode(),
            eventSeed.eventName(),
            eventSeed.presentationType(),
            eventSeed.triggerType(),
            eventSeed.triggerValue(),
            !eventSeed.choices().isEmpty(),
            eventSeed.imageUrl(),
            eventSeed.senderName(),
            eventSeed.receiverName(),
            eventSeed.description(),
            true
        );
    }

    private boolean hasChoices(final Integer gameEventId) {
        return !eventChoiceRepository.findAllByGameEventIdOrderByChoiceOrderAsc(gameEventId).isEmpty();
    }

    private void seedChoices(final Integer gameEventId, final List<EventChoiceSeed> choices) {
        choices.forEach(choice -> eventChoiceRepository.save(
            EventChoice.create(
                gameEventId,
                choice.choiceCode(),
                choice.choiceName(),
                choice.choiceOrder(),
                choice.choiceDescription()
            )
        ));
    }

    private void seedConditions(final Integer gameEventId, final List<EventConditionSeed> conditions) {
        if (conditions.isEmpty()) {
            return;
        }
        if (eventConditionRepository.existsByGameEventId(gameEventId)) {
            return;
        }

        conditions.forEach(condition -> eventConditionRepository.save(
            EventCondition.create(
                gameEventId,
                condition.conditionGroupNumber(),
                condition.conditionOrder(),
                condition.conditionType(),
                condition.targetTableName(),
                condition.targetColumnName(),
                condition.comparisonOperator(),
                condition.criteriaTextValue(),
                condition.criteriaNumberValue1(),
                condition.criteriaNumberValue2(),
                condition.logicalOperatorType()
            )
        ));
    }

    private void seedEffects(final Integer gameEventId, final List<EventEffectSeed> effects) {
        if (effects.isEmpty()) {
            return;
        }
        if (eventEffectRepository.existsByGameEventId(gameEventId)) {
            return;
        }

        final Map<String, Integer> choiceIdMap = eventChoiceRepository
            .findAllByGameEventIdOrderByChoiceOrderAsc(gameEventId)
            .stream()
            .collect(Collectors.toMap(EventChoice::getChoiceCode, EventChoice::getEventChoiceId));

        effects.forEach(effect -> eventEffectRepository.save(
            EventEffect.create(
                gameEventId,
                resolveEventChoiceId(effect.choiceCode(), choiceIdMap),
                effect.effectOrder(),
                effect.applicationTimingType(),
                effect.targetTableName(),
                effect.targetColumnName(),
                effect.operationType(),
                effect.baseNumberValue(),
                effect.minNumberValue(),
                effect.maxNumberValue(),
                effect.baseTextValue(),
                effect.durationTurns(),
                effect.note()
            )
        ));
    }

    private Integer resolveEventChoiceId(
        final String choiceCode,
        final Map<String, Integer> choiceIdMap
    ) {
        if (choiceCode == null) {
            return null;
        }
        if (choiceIdMap.containsKey(choiceCode)) {
            return choiceIdMap.get(choiceCode);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
