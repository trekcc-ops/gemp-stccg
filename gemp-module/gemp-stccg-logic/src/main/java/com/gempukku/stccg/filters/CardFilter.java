package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ActiveCardFilter.class, name = "active"),
        @JsonSubTypes.Type(value = AffiliationFilter.class, name = "affiliation"),
        @JsonSubTypes.Type(value = AndFilter.class, name = "and"),
        @JsonSubTypes.Type(value = AnyCardFilter.class, name = "any"),
        @JsonSubTypes.Type(value = AtLocationFilter.class, name = "atLocation"),
        @JsonSubTypes.Type(value = BottomCardsOfDiscardFilter.class, name = "bottomCardsOfDiscard"),
        @JsonSubTypes.Type(value = CanEnterPlayFilter.class, name = "canEnterPlay"),
        @JsonSubTypes.Type(value = CardTypeFilter.class, name = "cardType"),
        @JsonSubTypes.Type(value = CharacteristicFilter.class, name = "characteristic"),
        @JsonSubTypes.Type(value = ClassificationFilter.class, name = "classification"),
        @JsonSubTypes.Type(value = ControlledByPlayerFilter.class, name = "controlledByPlayer"),
        @JsonSubTypes.Type(value = ControllerControlsMatchingPersonnelAboardFilter.class,
                name = "controllerControlsMatchingPersonnelAboard"),
        @JsonSubTypes.Type(value = CopyOfCardFilter.class, name = "copyOfCard"),
        @JsonSubTypes.Type(value = CostFilter.class, name = "cost"),
        @JsonSubTypes.Type(value = DockedAtFilter.class, name = "dockedAtCard"),
        @JsonSubTypes.Type(value = EncounteringCardFilter.class, name = "encounteringCard"),
        @JsonSubTypes.Type(value = ExposedShipFilter.class, name = "exposedShip"),
        @JsonSubTypes.Type(value = FacilityTypeFilter.class, name = "facilityType"),
        @JsonSubTypes.Type(value = HasIconFilter.class, name = "hasIcon"),
        @JsonSubTypes.Type(value = HasSkillFilter.class, name = "hasSkill"),
        @JsonSubTypes.Type(value = InCardListFilter.class, name = "inCardList"),
        @JsonSubTypes.Type(value = InPlayFilter.class, name = "inPlay"),
        @JsonSubTypes.Type(value = InYourDiscardFilter.class, name = "inYourDiscard"),
        @JsonSubTypes.Type(value = InYourDrawDeckFilter.class, name = "inYourDrawDeck"),
        @JsonSubTypes.Type(value = InYourHandFilter.class, name = "inYourHand"),
        @JsonSubTypes.Type(value = InZoneFilter.class, name = "inZone"),
        @JsonSubTypes.Type(value = LocationNameFilter.class, name = "locationName"),
        @JsonSubTypes.Type(value = MatchingAffiliationFilter.class, name = "matchingAffiliation"),
        @JsonSubTypes.Type(value = MatchingAttributeFilter.class, name = "matchingAttribute"),
        @JsonSubTypes.Type(value = MissionPointValueFilter.class, name = "missionPointValue"),
        @JsonSubTypes.Type(value = MissionAffiliationIconFilter.class, name = "missionAffiliationIconForOwner"),
        @JsonSubTypes.Type(value = MissionTypeFilter.class, name = "missionType"),
        @JsonSubTypes.Type(value = NotAllFilter.class, name = "notAll"),
        @JsonSubTypes.Type(value = NotAnyFilter.class, name = "notAny"),
        @JsonSubTypes.Type(value = OrCardFilter.class, name = "or"),
        @JsonSubTypes.Type(value = OwnedByPlayerFilter.class, name = "ownedByPlayer"),
        @JsonSubTypes.Type(value = PresentWithCardFilter.class, name = "presentWithCard"),
        @JsonSubTypes.Type(value = PropertyLogoFilter.class, name = "propertyLogo"),
        @JsonSubTypes.Type(value = SameCardFilter.class, name = "sameCard"),
        @JsonSubTypes.Type(value = SkillDotFilter.class, name = "skillDotCount"),
        @JsonSubTypes.Type(value = SpecialDownloadIconCountFilter.class, name = "specialDownloadIconCount"),
        @JsonSubTypes.Type(value = SpeciesFilter.class, name = "species"),
        @JsonSubTypes.Type(value = ThisCardIsAboardFilter.class, name = "thisCardIsAboard"),
        @JsonSubTypes.Type(value = TitleFilter.class, name = "title"),
        @JsonSubTypes.Type(value = TopOfPlayPileFilter.class, name = "topOfPlayPile"),
        @JsonSubTypes.Type(value = UndockedFilter.class, name = "undocked"),
        @JsonSubTypes.Type(value = UniquenessFilter.class, name = "uniqueness"),
        @JsonSubTypes.Type(value = YouControlAMatchingOutpostFilter.class, name = "youControlAMatchingOutpost"),
        @JsonSubTypes.Type(value = YouOwnNoCopiesInPlayFilter.class, name = "youOwnNoCopiesInPlay")
})
public interface CardFilter extends Filterable {
    boolean accepts(DefaultGame game, PhysicalCard physicalCard);
}