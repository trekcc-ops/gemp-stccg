package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;

public class TournamentInfo {
    private final String _tournamentId;
    private final String _tournamentName;
    private final String _tournamentFormat;
    private final CollectionType _collectionType;
    private final String _prizesScheme;
    private final int _tournamentRound;
    private final String _pairingMechanism;
    private final Tournament.Stage _tournamentStage;

    public TournamentInfo(String tournamentId, String tournamentName, String tournamentFormat,
                          CollectionType collectionType, Tournament.Stage tournamentStage, String pairingMechanism,
                          String prizesScheme, int tournamentRound) {
        _tournamentId = tournamentId;
        _tournamentName = tournamentName;
        _tournamentFormat = tournamentFormat;
        _collectionType = collectionType;
        _prizesScheme = prizesScheme;
        _tournamentRound = tournamentRound;
        _pairingMechanism = pairingMechanism;
        _tournamentStage = tournamentStage;
    }

    public String getTournamentId() {
        return _tournamentId;
    }

    Tournament createDefaultTournament(TournamentService tournamentService, String tournamentId,
                                       CardBlueprintLibrary library) {
        PairingMechanism pairingMechanism = PairingMechanismRegistry.getPairingMechanism(_pairingMechanism);
        TournamentPrizes tournamentPrizes = TournamentPrizeSchemeRegistry.getTournamentPrizes(library, _prizesScheme);
        return new DefaultTournament(tournamentService, tournamentId, _tournamentName, _tournamentFormat,
                _collectionType, _tournamentRound, _tournamentStage, pairingMechanism, tournamentPrizes);
    }
}