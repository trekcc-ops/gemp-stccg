import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class IdentifyingActionsTest extends NewLibraryTest {


    public void tokenTest() throws IOException {
        System.out.println("if you don't see this, the text got truncated");
        System.out.println(potentialActions().size());

        createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            boolean matches = false;
            String text = card._gameText.toString();
            for (String word : potentialActions()) {
                if (text.toLowerCase().contains(word)) {
                    matches = true;
                }
            }
            if (matches) {
                System.out.println(card._title);
                System.out.println(card._gameText);
//                    System.out.println(sentence);
            }
        }
    }

    private List<String> definiteActions() {
        List<String> actions = new LinkedList<>();
        actions.add("abduct"); // abducted, abduction
        // Borg can abduct personnel
        actions.add("acquire"); // acquired
        // Acquire an Artifact
        actions.add("activate"); // activated, deactivated, deactivation
        // Activate & deactivate for [Holo] cards
        // Activate Hidden Agenda cards
        actions.add("assimilate"); // assimilated, assimilates, assimilation
        actions.add("attempt"); // attempted, attempting, re-attempt
        actions.add("battle"); // battled, battles, battling
        actions.add("beam"); // beamed, beaming, beams, just-beamed
        actions.add("breed");
        // How Tribbles are played. Referred to by ...On the Station
        actions.add("cancel"); // canceled, cancelled, cancels
        /*
            Nothing responds explicitly to "cancel"
            Things that can be canceled: battles, probing, restriction boxes, selections, Yellow Alert
         */
        actions.add("capture"); // captured, captures, capturing
        actions.add("cloak"); // cloaked, cloaking, cloaks, de-cloak, decloak, decloaked, decloaks, recloak, uncloaked
        actions.add("close"); // closing
        // Close doorways, side decks, or Iconian Gateway; no specific responses (but some cards are immune)
            // This could potentially be rephrased as "suspending" cards
        actions.add("commandeer"); // commandeered, commandeers
        actions.add("convert"); // converted, converts
        // convert draw to download
        // convert mission to [S] mission
        // Thomas Paris - convert skill
        // Tijuana Crass - convert Data Laughing into an Event
        // He Will Make an Excellent Drone - convert counterpart to drone
        actions.add("cure"); // cured, curing
        // cure dilemma
        actions.add("destroy"); // destroyed, destroying, destroys
        // most commonly destroying a ship, but may be applied to other card types as well
        actions.add("disable"); // disabled, disables
        // disabling personnel, or disabling ship attributes (the latter has no action response)
        actions.add("discard"); // discarded, discarding, discards
        actions.add("dock"); // docking, docked, docks, undock, undocked, undocking
        actions.add("download"); // downloaded, downloading, downloads
        // Download a card
        actions.add("draw"); // drawn, draws, re-draw
        // Draw a card. Occasionally redefined as an action that's not a card draw
        actions.add("encounter"); actions.add("face"); actions.add("facing"); // encountered, encountering, encounters
        // "Facing a dilemma" synonymous with "encountering a dilemma"
        actions.add("engage"); // engaged, engages, disengage
        // engage in combat (all responses to engaging use this definition)
        // Weird text on Small Cloaking Device - "while engaged" could be "while ship is cloaked"
        // "engage or disengage its special equipment" on Install Autonomic Systems Parasite
        actions.add("erase"); // erased, erases
        actions.add("exchange"); // exchanged
        actions.add("expose"); // exposed, exposes
        // expose infiltrator
        // weird text for exposed cards (this is applying a modifier) and exposed ships (not an action)
        actions.add("fire"); // fired, firing
        actions.add("flip"); // flipped
        // flip card over
        actions.add("fly"); // flying
        // no card allows you to initiate "fly", but the rules do; there are some responses to flying by
        actions.add("infiltrate"); // infiltrating, infiltration, infiltrated
        actions.add("kill"); // killed, killing, kills, just-killed
        actions.add("land"); // landed, lands, landing
        actions.add("launch"); // launched
        actions.add("loaded"); // action that leads to carried ships
        actions.add("look"); actions.add("glance"); actions.add("peek"); actions.add("examine");
        // look at cards that you're not supposed to be able to see (no responses)
        // synonymous with glance and peek (no responses)
        // also symonymous with examine (no response, but there's an "unexamined mission" concept)
        actions.add("morph"); // morphs, morphing
        // flavor text, but can be responded to specifically
        actions.add("phas"); // phase, phased, phases, dephase, rephase, phasing, dephases
        actions.add("play"); // played, playing, plays, just-played
        actions.add("prob"); // probe, probed, probing
        actions.add("rearrange");
        // move cards around in a pile; no responses
        actions.add("release"); // released
        // Release captive
        actions.add("repair");// repairs
        actions.add("report"); // reported, reporting, reports
        actions.add("scout"); // scouted, scouting
        actions.add("seed"); // seeded, re-seed, re-seeds, seeds
        actions.add("steal"); actions.add("stolen");
            // includes steal organs
        actions.add("stop"); // stopped, stopping, stops, unstop, unstopped, unstops
        actions.add("stun"); // stunned, stuns, stunning
        actions.add("tak"); // take, taken, takes, taking
        /*
            Take off - ships taking off from a planet
            Take card - picking up a card from discard pile, side deck, stack, etc. (U.S.S. Stargazer, etc.)
                // Is this usage of "take" helpful? I think so, but could it be replaced with download/draw?
            "Take a little off the top" - flavor text on In for a Trim
            take a double turn - Temporal Narcosis (game loop modifier)
            There don't seem to be valid responses to any of these
         */
        actions.add("travel"); // traveled, travelled, travels
        // time travel
        // Weird text meaning "move" on Cytherians, First Stable Wormhole, Wormhole Navigation Schematic
        // Weird text on Cytherian Lure, referring to Cytherians movement
        // Flavor text on Time Travel Pod
        // Weird text on Rescue Personnel, "arrive" would be more accurate
        actions.add("walk"); // walking
        // Action allowed by rules and cards like Croden's Key; no responses
        actions.add("wound"); // wounded, wounds
        // mortally wounding
        return actions;
    }

    private List<String> responseButNotCreateActions() {
        List<String> actions = new LinkedList<>();
        actions.add("arrive"); // arriving, arrival
        // More of a "while" criteria
        actions.add("board"); // boarding
        // can be initiated by Docking Pads, Docking Ports, Docking Pylons (I see this as weird text for "walking")
        // Orb of Time & REM Fatigue Hallucinations
        actions.add("bring");
        // The Emperor's New Cloak - discard when it is brought aboard
        actions.add("exceed"); // one number exceeds another (Nightmare and Intermix Ratio)
        actions.add("exhaust"); // used for "exhaust RANGE" and "exhaust draw deck"
        actions.add("expire"); // only used for countdowns
        actions.add("fail"); // fail an attempt, or fail to win a battle
        actions.add("help"); // helped, helps
        // help solve a mission, or help win a battle, or help overcome a dilemma
        actions.add("leav"); // leave, leaves, leaving
        // How does "may not leave play" work?
        // Samuel Clemens - "leave play" weird text; better to say "remove from the game" or place out of play
        // Lure of the Nexus - "leaving the Nexus" relocation allowed by The Nexus; can be responded to
        // weird text on Where No One Has Gone Before, Lakanta
        actions.add("mov"); // move, moved, moves, moving
        // Multiple actions are considered "moving"
        actions.add("order"); // orders (executing orders - Make a Difference Again refers to this)
        actions.add("overcome"); // overcomes
        actions.add("resolved");
        // Smoke and Mirrors - resolve a dilemma
        actions.add("survived"); // surviving
        actions.add("win"); actions.add("won"); // wins
            // win battle, win points, or win Royale Casino side game
        return actions;
    }

    private List<String> potentialActions() {
        // forms of to be? - are, be, been, being, is, was, were, weren't
        List<String> actions = new LinkedList<>();
        // abandon - no responses; shorthand for end mission attempt, and the mission can't be re-attempted
        // abort - synonym for end mission attempt, used by Edo Probe
        // achieved - flavor text on objectives
        actions.add("action"); // actions
            // General action, term used by some cards & rules
        // add, adds - Is this an action or a modifier? Either way, there are no specific responses
        // advise - flavor text on 59th Rule of Acquisition; no difference from selecting a mission
        /* affect, affect, affecting, affects
            Typically "affect" describes a permanent effect - when X card is in play, it modifies Y card's
            gametext.
                Examples: Arandis, Changeling Research, Mirror Image

            Several cards refer to cards "affecting", but these are referring to a gamestate, such as "card X is
            affecting you". This is describing the gamestate, but not providing a valid response.
            There are typically more clear ways to phrase these cards.
                Examples: Reflection Therapy, Intruder Force Field, Singha Refugee Camp

            Jealous Amanda - response to dilemma encounter, not the dilemma affecting the personnel
            Manheim's Dimensional Door - unnecessary text
         */
        // afflicted - meaningless word on Nanites
        // agree - meaningless word on Raise the Stakes
        // alternating - Used by Black Hole, not an action
        actions.add("allow"); // allowed, allowing, allows
        // Awkward wording on cards, not clear how it works. No responses.
        /* appear, appears, disappear, disappears, reappears
                Flavor text on Picard Maneuver, Quantum Leap, Spatial Rift

                Some time travel cards (Temporal Rift, Temporal Vortex, Time Travel Pod) say that cards disappear or
                reappear. This isn't clearly defined, but it's a modifier, not an action. No specific responses.
         */
        actions.add("apply"); // applying
        /* "Applying" icons to staffing is a synonym for "using", and "applying" damage is a
                synonym for "damaging". "affiliation attack restrictions" used as a synonym for saying you can ignore
                 them.
           RECEPTACLE STONES - This is a modification of the encounter action. No responses to the word "apply".
         */
        // argued - Only used by Parallax Arguers, shorthand for making a multiple choice decision
        // ask - Flavor text on Interrogation and Gold!; player has to say stuff
        // attend - flavor test on Sabotaged Negotiations
        actions.add("attack"); // attacked, attacking, re-attack, counter-attack
        // avoid - general word for nullifying, preventing, immune, etc. No specific responses
        // General word for negating, nullifying, etc. No responses
        // become, becomes - modifier, typically personnel becoming a new affiliation
        // begs - flavor text on Alien Groupie
        // begin, begins, beginning, began - referring to timing of more specific actions or turns
        // benefit - only used for "benefit from two matching commanders"
        // bites - flavor test on Palukoo
        // Brainwash - Prepare the Prisoner uses it as a verb, but really it modifies Brainwash's gametext
        // brawl - flavor text on Promenade Shops
        // bribe - flavor text on Rebel Encounter (no responses)
        // build, built - weird text for play on tons of facilities and Gi'ral
        // burn, burns - flavor text on Renewal Scroll
        // bury - flavor text on Burial Ground
        // call - weird text on In the Zone
        // carried, carry - only used to describe carrying ships (this is a gamestate)
        /* cause, causes, causing:
                flavor text on Plasma Energy Burst
                weird text on Scorched Hand, Alien Gambling Device, Change of Heart, Mona Lisa, Team of Ambassadors
                The only card that responds to "cause" is Mona Lisa, which is explicitly defined in the glossary to
                not need this word.
         */
        actions.add("change"); // changed, changes
        /* Generally, "changing" is applying a modifier that can be replaced by a select action.
            One response - No, Kirk... The Game's Not Over is a response to changing a mission's point value using
            The Genesis Device.
         */
        // chase, chased - flavor text on Porthos and Hunter Gangs; weird text on Conundrum; no responses
        actions.add("choice");
        actions.add("chose"); // chosen
        actions.add("choose"); // chooses
        // cleared - flavor text on Establish Gateway
        // coexist - facilities "may coexist"
        // combat - "engage in combat"
        // come - modifier; cards you download can come from other piles
        // compare - weird text on Royale Casino: Elevator
        actions.add("complet"); // complete, completed, completes, completing
        // compliment - multiple choice on 33rd Rule of Acquisition; no specific responses
        // comply, complies - multiple choice on Protection Racket and You Will in Time; no specific responses
        actions.add("conceals");
            // Used by Chula: Trickery. No specific responses
        actions.add("conduct"); // conducting
            // flavor text on a couple cards; ...on the Station prevents "conducting services" at Bajoran Shrine
        // continue, continues, continuing - "mission continues"; weird text on Plasma Fire & Honor Challenge
        // contribute - weird text on Chula: The Door & Warrior's Birthright (synonym for use attribute)
        actions.add("control"); // controlled, controlling, controls, controller
        // cooperate - only ever "may cooperate" = compatible
        // could - only used on Lack of Preparation
        // count - count downs, X does not count toward Y; weird text on Tijuana Crass
        // create, creates - create location when playing some cards; no responses
        // crushed - flavor text on Kelvan Show of Force
        actions.add("damag"); // damage, damaged, damages, damaging, undamaged
        // de-evolves - flavor text on Barclay's Protomorphosis Disease
        // declare - saying what you're about to do, but no responses; this isn't very meaningful
        // decline - multiple choice decision for whether or not infiltrators participate in missions
        // defending? only used by two tactics; this isn't really an action, I don't think
        // delayed - modifier that injects a new action (no responses)
        // destruct - weird text for destroy on Auto-Destruct Sequence
        actions.add("destruction");
        // detect - this is only used as flavor text on Tachyon Detection Grid and Mission II's
        actions.add("death");
        actions.add("die"); // died, dies
        // disruption, disrupted, disruptions - flavor text for timeline disruptions
        // distract - flavor text on Distraction
        // divide - type of selection; no responses
        actions.add("did");
        actions.add("do"); // does, doesn't, done, undo
        actions.add("doubl"); // double, doubled, doubles, doubling
        // drains - flavor text on Target Shields (no responses)
        // duplicate - automatic modifier
        actions.add("earn"); // earned, earns
        // eliminated - weird text on Lore Returns, flavor text on Omega Directive
        actions.add("end"); // ended, ending, ends
        // enhance - this is a modifier word
        actions.add("enter"); // entered, enters (enter play)
        // escape, escapes; flavor text on Firestorm and Hunter Gangs
        // escort - modifier/gamestate - captives can be escorted, but there's no action
        // evacuate - weird text on Vole Infestation for emptying ship and leaving it empty; no specific responses
        // evade - flavor text on Tongo
        // exclude, excluded, excluding - only used to exclude cards from selection or battle; no responses
        // executing - flavor text on Make a Difference Again, but "begin" is the real action
        // exist - modifier for locations where [Holo] cards can be
        // explodes, explosion - weird text on Auto-Destruct Sequence & Warp Core Breach; no responses
        // exploit - weird text on Isabella
        // exploitation - flavor text on Imperial Intimidation
        // extend - used to modify modifiers; no responses
        // face - weird text on Attack of the Drones
        // fight - not an action
        // file mission reports - flavor text on File Mission Report; every other use is a modifier
        // float - "float in space" on Airlock
        // following (always "the following")
        // follows - weird text on Temporal Wake
        // force, forced - difference between optional and required action (no responses)
        // forfeit - weird text on To Rule in Hell, no responses
        // form - flavor text on Lineup
        // free - playing/reporting for free
        // function - this is a modifier
        actions.add("gain"); // gained, gains, regains; this might be a modifier though
        // get past, get through - flavor text for dilemmas
        actions.add("give"); // gives
            // give skills or icons
            // We Look for Things - gives card for your use (no response)
            // Some Q-cards - give card to opponent for them to score points
        actions.add("go"); // going
        // guess - multiple choice decisions; no responses
        // guides - weird text on Duranja, "guides from discard pile to point area"; no responses
        // had, has
        // happen - weird text on Samuel Clemens' Pocketwatch; not super clear how this card works, but no responses
        // hides - weird text on Asteroid Sanctuary
        // hit - used for hit and direct hit. Chain Reaction Ricochet responds to hit, but could just use damage
        // hurl - weird text on Gomtuu for relocate
        actions.add("ignor"); // ignore, ignores, ignoring
        actions.add("immune"); // think this is more of a modifier
        // impersonate - a specific kind of morphing from Impersonate Captive and Assume Identity; different on each
        // increased, increases - this is a modifier, and I don't see a response
        // infected - weird text on Tsiolkovsky Infection and Alien Parasites
        actions.add("initiat"); //initiate, initiated, initiates, initiating, just-initiated
        actions.add("insert"); // only used for inserting into deck or inserting into spaceline; no responses
        // interrupt, interrupts - card type, not action
        // investigate - flavor text
        // join - weird text on Warp Speed Transfer, Kobayashi Maru, and Security Office; no responses
        // keep - synonymous with prevent
        actions.add("left");
            // Anti-Matter Pod - how does this work?
        actions.add("limit"); // limited, limits, unlimited; pretty sure this is a modifier (not an action)
        actions.add("lose");
        actions.add("loss");
        actions.add("lost");
        // lowered - weird text on Dropping In "shields lowered for beaming"
        // make, made, makes; this isn't really a helpful word
        actions.add("may"); // is this helpful?
        actions.add("met");
        actions.add("meet"); // meets, meeting; is this helpful?
        // melt - flavor text on Fire Sculptor
        actions.add("must"); // is this helpful?
        actions.add("nam"); // name, named, names, naming
        // need - modifier
        actions.add("nullif"); // nullify, nullified, nullifies, nullifying, nullification
        // observe - flavor text on Holoprogram: Historical Poker Game
        // occupied, occupying, unoccupied - gamestate
        // occur - flavor text on Manheim's Dimensional Door
        actions.add("open"); // opened, opens, reopen, re-opening
        /* operates, operating - "operate transporters" weird text on Scan Cycle Check & Target These Coordinates
                "Transporter Skill operating transporters" = beaming with Transporter Skill in crew
                no responses to "operating transporters"
         */
        actions.add("oppos"); // opposed, opposing
        // orbit - "in orbit" and "orbiting" both referring to game state of a ship; no responses
        // override - flavor text on Jealous Amanda
        // pair - several cards may "pair first" in battle; more of a modifier than an action
        // participate, participating - allows non-personnel to join battle, or refers to battle participants
        actions.add("pass"); // passed, passing
            // moving locations - Cargo Bay, Alternate Universe Door, Romulan Minefield, Subspace Warp Rift, Q-Net
            // weird text on some dilemmas
            // Gold! - pass a card to your opponent to control
        // pick - flavor text on Nausicaans
        actions.add("plac"); // place, placed, places, placing, replace, replaced, replaces, replacement
        // plant - weird text on Drought Tree
        // pool - flavor text on Blood Screening
        // post - flavor text on Mandarin Bailiff
        // pounce - flavor text on Spot
        // pre-arrange - weird text on Bodyguards
        // pre-announce - weird text on Time Travel Pod for selecting a timing
        actions.add("prevent"); // prevented, preventing, prevents
        /*
            Both an action and a modifier?

            prevent assimilation - Sickbay: Menagerie
            prevent battle - Asteroid Sanctuary, Magnetic North, Time to Reconsider, We Surrender!
            prevent beaming - Distortion Field, Transport Inhibitor
            prevent capture - Long Live the Queen, Mandarin Bailiff
            prevent conducting services at Bajoran Shrine - ...on the Station
            prevent damage - The Arsenal: Weapons Demonstration
            prevent deactivation - Holodeck Door
            prevent death - Emergency Transport Unit, Empathic Touch, Environmental Suit, John Doe,
                Mantle of the Empire, Smooth as an Android's Bottom?, The Beating Heart
            prevent disabling - Osmotic Eel
            prevent download - I... Have Had... Enough of You!
            prevent draw - 1,000 Tribbles
            prevent Interrupt card (by name) - Subspace Interference
            prevent morphing - Caught Red-Handed, Howard Heirloom Candle
            prevent nullifying - Scepter of the Grand Nagus
            prevent personnel from leaving The Nexus - Lure of the Nexus
            prevent reporting - 100,000 Tribbles
            prevent scoring points - 100 Tribbles, Bribery
            prevent Spacedoor re-opening - Panel Overload
            prevent stasis - Five of Eleven, Osmotic Eel
            prevent supernova - Tox Uthat
            prevent white deprivation - Ketracel-White

            weird text - Yellow Alert

            Dyson Sphere Door ignores cards that prevent downloading
            Quark's Isolinear Rods - nullify any or all cards preventing you from playing Q's Tent
         */
        // primed - flavor text on Activate Thalaron Weapon
        actions.add("proceed"); // proceeding, proceedings
            // Used for several "cannot get past" dilemmas, no responses
            // Weird text on Tetryon Field, no responses but it's a really weird card
        actions.add("process"); // processed, processing
            // process ore
        // progress - weird text on Rescue Captives; nullifies cards played on captives
        actions.add("protect"); // protects, protection
        /* not sure if Gurat'urak's special skill is an action or an automatic modifier. Otherwise, this is always
            "place on X card to protect it from nullification" sort of phrasing.
         */
        // provides - weird text on Obelisk of Masaka
        actions.add("put"); // puts
            // Urgent Warning, The Whale Probe - put into play on spaceline
            // Pla-Net - responds to put into stasis
            // Long Live the Empire! - put cards from draw deck under = take cards from draw deck and place under
            // Penalty Box - put card out-of-play; put personnel here
            // Countermanda - put cards out-of-play
        // quarantined - modifier; typically result of failing a dilemma
        actions.add("ram");
        actions.add("ration"); // rationed
            // Flavor text on Ketracel-White and Obedience Brings Victory
        // reabsorb, reabsorbing - flavor text on Borg cards
        // reach, reached, reaching - weird text for arrive
        // re-boot - flavor text on Iconian Computer Weapon
        // reception - flavor text on Subspace Interference
        // reclaim - flavor text on HQ: Return Orb to Bajor
        // recites - weird text on Chula: Trickery; no responses
        // recognize - flavor text on treaties
        // redefine - weird text on Terraforming Station
        actions.add("redirect");
            // weird text on Wrong Door - is this a gametext change? a relocation?
        /* reduce, reduced, reduces, reducing, reductions - This is generally a modifier.
            The only card that arguably treats it as an action is Birth of "Junior", which could be rewritten.
         */
        // refuses - weird text on Barclay Transporter Phobia; no responses
        // regenerates - flavor text on Res-Q; no responses
        // regulate implants - flavor text on Cortical Node Implant
        actions.add("relocat"); // relocate, relocated, relocates, relocating, relocation
        // remodulate - flavor text on Remodulation
        actions.add("remove"); // removed, removes
            // remove from the game - synonymous with place out of play
            // modifer - remove skills or staffing requirements, or remove from stasis
            // no specific responses
        // renew - flavor text on Renewal Scroll
        // repeats - weird text on "Pup"; no specific responses
        // replenish, replenishing - weird text; no specific responses
        // replies - flavor text for Interrogation; no specific responses
        // reprogram - flavor text on Dr. Soong; no specific responses
        // request - multiple choice decision on a couple cards, no specific responses; flavor text on Holoprogram
        actions.add("requir"); // require, required, requires, requiring
        actions.add("rescue"); // rescued, rescues
            // synonymous for release captives
            // flavor text on Escape Pod, Thine Own Self, Abandon Ship!, Rescue Personnel (no responses for this)
        // resets - modifier of countdowns
        // resigns - flavor text on Anaphasic Organism
        actions.add("restore"); // restore, restores, restored
            // restore RANGE modifier (no responses)
            // weird text on Quandary (no responses) - taking card from discard
        // resumes - unnecessary text on Alien Parasites
        // retaliate - weird text on Kova Tholl
        // retire - flavor text on Reserve Activation Clause
        actions.add("return"); // returned, returning, returns
        actions.add("reveal"); // revealed, reveals
        // reverse, reverses - modifying game texts of cards (no responses)
        // revive - flavor text on Hypospray
        // revolve - flavor text on Revolving Door
        // ricochets - flavor text on Chain Reaction Pulsar
        actions.add("rotate");
            // Rotate mission (Trilithium Weapon & Operate Dilithium Gulag); no responses
        actions.add("run"); // runs
            // runs off - Love interests and Primal Urges; does this count as relocating? moving? (no specific response)
        // sacrifice - flavor text on multiple cards, no specific responses
        // save, saves - shorthand text on multiple cards for preventing other actions; no responses
        // say - weird text for making a decision
        // scan, scans - flavor text on Scanner Interference, weird text on Science Lab; no responses
        actions.add("scor"); // score, scored, scoring, scores
            // Not at all clear if scoring is an action
        actions.add("search");
        actions.add("select"); // selected, re-select, re-selected, selection, selections, selects, unselected
        // separate - used to refer to the order in which sites must be seeded
        // serve - modifier for a card that can serve as Nagus or serve as matching commander
        actions.add("shar"); // share, shared, sharing, shares
        // shopping - flavor text on Promenade Shops
        // shot - weird text on Archer
        actions.add("show"); // showing, shown, shows
        actions.add("shuffl"); // shuffle, re-shuffle, shuffled, shuffles, shuffling, unshuffled, reshuffle
        // skip - skip a turn; nothing responds to this
        actions.add("solv"); // solve, solved, solves, solving
        // specify, specifies - multiple choice decision
        // split - splitting Away Team or crew into pieces; no responses
        // spread - flavor text on ...On the Station
        // staff, staffed, staffing - "staff a ship" as a verb = use staffing icon
        actions.add("start"); // started, starting
        // store, stored - weird text on Zalkonian Storage Capsule and Nightmare; no responses
        // substitute - reselecting after a selection
        // subtract - always a modifier, except arguably for the Higher...The Fewer
        // sucked - meaningless text on Cosmic String Fragment
        /* suspend, suspended, suspends -either:
            suspending rules
            suspending cards or their gametext
            suspending play to perform actions
            None of these are actions in their own right, and none can be responded to.
         */
        // sweep - flavor text on Changeling Sweep; no responses
        actions.add("target"); // targeted, targeting, targets
        // team - typically Away Team, sometimes a subdivision of an away team; never an action
        // tempted - flavor text on Temptation; no responses
        // tested - flavor text on Chula: The Drink; no responses
        // threaten, threatened - flavor text on Protection Racket; no responses
        // tie, ties, tied - not an action; typically "in case of tie" clause
        // tossed - flavor text on Airlock; no responses
        // trade, traded - flavor text on 1962 Roger Maris Baseball Card; no responses
        actions.add("transfer"); // transferred
            // Holosuite - transfer [Holo] cards between locations
            // flavor text on Divert Power & Cortical Node Implant
            // The Katra of Surak - transfer card to another personnel
            // Arbiter of Succession - points transfer to opponent
            // No responses to this action
        actions.add("transport");
            // Weird text on Maman Picard - does this count as relocate?
            // Weird text on Barclay Transporter Phobia
            // No specific responses
        // treat, treated - weird text on Hawk and You Dirty Rat
        // triple, tripled, tripling - applying modifier to cards; no responses
        // turned - Heisenberg Compensators turns draw deck face up; this is a modifier with no responses
        actions.add("use"); actions.add("using");
        /* re-use, used, uses, using

            Use Mission Requirements (modifier for mission)
            ------------------------
            attempt "using these requirements" (Establish Trade Route, Hunting Group, etc.)


            Use Ships/Facilities
            --------------------
            RULE MODIFIER: ships/facilities may be used by X type of personnel (U.S.S. Yangtzee Kiang, Kolinahr, etc.)
            RESPONSE: Breach Barrier - +20 points if solved using a ship you do not own


            Use Card as an Additional Card Type (permanent feature of card?)
            -----------------------------------
            "use as Equipment" - type of artifact, referred to by other cards like Disruptor Overload



            Use Skills, Classifications, Attributes (typically an automated action)
            ---------------------------------------
            // When RANGE is used, the RANGE available to that ship is reduced

            // Automatic Result of Action
            enabling actions that use X RANGE (Engage Shuttle Operations, UFP: One Small Step, etc.)
            some cards clarify that movement requires normal use of range (Herd and Harvest, Emergent Life-form, etc.)

            // Optional Action
            may use skill X as if it were skill Y - maH nIv, Security Drills, etc.
            E.M.H.-Mark II - Biology OR Exobiology (may use only one per turn)

            // Not Allowed
            personnel may not use skills/classifications - Felix Leech, Eli Hollander, Framed for Murder, etc.
            ship may not use attributes - Fractal Encryption Code

            // Responses (some examples)
            Thorough Debriefing, File Mission Report, AMS - response to personnel using a classification or skill
            Juliana Tainer - response to personnel using a characteristic
            Picard's Artificial Heart - response when personnel's STRENGTH is used
            Primary Turbolift - when a personel reports using their classification to a site
            responses to using a special download icon (Mr. Tuvok, Radiation Monitoring, Hidden Fighter)


            Using the Gametext of Cards
            ---------------------------

            Responding to Performed Actions Allowed by Gametext
            ---------------------------------------------------
            Temporal Narcosis - plays when opponent is using Horga'hn, Revolving Door, Emergency Transporter
                Armbands, or Energy Vortex
            HQ: Defensive Measures - may not attempt mission unless using an appropriate Espionage card
            GIVE ME GENESIS! - no player may use The Genesis Device on a mission they seeded
            Writ of Accountability - if opponent has used Subspace Schism or Brain Drain more than twice
            Writ of Accountability - if opponent has used their own dilemmas to score more than 15 points or to
            discard other dilemmas
            No, Kirk... The Game's Not Over - if you just changed a mission's point value using The Genesis Device
            Feedback Surge - if opponent discards seed cards using Ajur, Boratus, Senior Staff Meeting, or their own
            dilemma
            Homefront - may not use headquarters game text here
            Adapt: Modulate Shields - a phaser or disruptor just used to stun or mortally wound
            Regime Change - personnel who report for free using game text on planet (or planet facility here)
            Containment Field - adds extra cost to use a special download or Hidden Fighter
            Refugee Guinan - dilemmas can only be nullified using their gametext
            Wrong Door - if opponent just used Iconian Gateway [i.e. if opponent just relocated cards]
            Dominion War Efforts - Your Assign Support Personnel is not discarded when used to download


            Other
            -----
            Vorgon Raiders - "steal" (use as your own) any one artifact in play or just played as an Interrupt card
            Spacedoor - you may use opponent's Red Alert as if you had a copy in play
            We Look for Things - must give you card for your use
            The Unrelenting Lust for Profit - you may use them [downloaded cards] at any time on your turn
            Hostage Trade - Prisoner Exchange and Fajo's Gallery may each be used only once per turn



            USING A TACTIC
            --------------
            Target Warp Field Coils, Determined Assault - may not be used to fire upon a facility
            Holographic Camouflage - your ships use tactics as if another ship



            Using Transporters
            ------------------
            RESPONSE: Murasaki Effect - response to "each time transporters used here"
            Boarding Party - your Kazon aboard opponent's ship may use its transporters
            Explore New World - you may not voluntarily use transporters here


            Using Equipment
            ---------------
            "X use only" (Federation PADD, Romulan Disruptor, etc.) - including weird text on Kazon PADD
            allowing actions or modifiers if a personnel is "using" a weapon (The Art of Diplomacy, Shrouded Assailant)
            Study Plasma Storm - Computer Skill required to use any equipment here


            Using a Card as a Target of Another Card
            ----------------------------------------
            Bok'Nor - may use hand weapons to complete "cargo runs"


            Usable Facilities
            -----------------
            Deep Space Station K-7 - may be used by all players' compatible cards




            weird text - "restore used RANGE", "restore RANGE used" (this is just restoring RANGE)
            weird text - "after use", "after any/either use" (not bad phrasing, but it could be worded in a way that
                doesn't suggest "use" is an action)
            weird text for controlling opponent ships - Lore Returns, Install Autonomic Systems Parasite,
                Neural Servo Device
            weird text - Royale Casino: Blackjack, Firestorm, Hirogen Hunt, Tijuana Crass, Reclamation,
                Tachyon Detection Grid, Dominion Command, Kivas Fajo, Tommygun, Engage Cloak, Nine of Seventeen,
                Alternate Universe Door, Devna-Lev, Emergency Transporter Armbands, Automated Repair Station,
                Transporter Drones, Echo Papa 607 Killer Drone
         */
        // volunteer - "volunteer for random selections" is an action modifier
        // wager - flavor text on Dabo
        // warn - flavor text on Klingon Death Yell
        // wearing, worn - flavor text on Mobile Holo-Emitter and Small Oversight
        // wish - weird text on 211th Rule of Acquisition and Orb of Prophecy and Change
        return actions;
    }

}