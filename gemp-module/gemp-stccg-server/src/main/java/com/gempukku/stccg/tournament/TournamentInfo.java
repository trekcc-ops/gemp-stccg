package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;

public class TournamentInfo {
    private final String _tournamentId;
    private final String _tournamentName;
    private final CollectionType _collectionType;
    private final String _prizesScheme;
    private final String _gameFormatCode;
    private final int _tournamentRound;
    private final String _pairingMechanism;
    private final Tournament.Stage _tournamentStage;

    public TournamentInfo(String tournamentId, String tournamentName, String tournamentFormat,
                          CollectionType collectionType, Tournament.Stage tournamentStage, String pairingMechanism,
                          String prizesScheme, int tournamentRound) {
        _tournamentId = tournamentId;
        _tournamentName = tournamentName;
        _gameFormatCode = tournamentFormat;
        _collectionType = collectionType;
        _prizesScheme = prizesScheme;
        _tournamentRound = tournamentRound;
        _pairingMechanism = pairingMechanism;
        _tournamentStage = tournamentStage;
    }

    public final String getTournamentId() {
        return _tournamentId;
    }

    final Tournament createDefaultTournament(TournamentService tournamentService, String tournamentId,
                                             CardBlueprintLibrary library, FormatLibrary formatLibrary) {
        PairingMechanism pairingMechanism = PairingMechanismRegistry.getPairingMechanism(_pairingMechanism);
        GameFormat gameFormat = formatLibrary.get(_gameFormatCode);
        TournamentPrizes tournamentPrizes = TournamentPrizeSchemeRegistry.getTournamentPrizes(library, _prizesScheme);
        return new DefaultTournament(tournamentService, tournamentId, _tournamentName, gameFormat,
                _collectionType, _tournamentRound, _tournamentStage, pairingMechanism, tournamentPrizes);
    }

}