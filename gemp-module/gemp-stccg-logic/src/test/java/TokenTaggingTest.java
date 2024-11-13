import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TokenTaggingTest extends NewLibraryTest {

    @Test
    public void tokenTest() throws IOException {

        Map<String, Set<String>> words = new HashMap<>();

        InputStream inputStream = new FileInputStream("..\\gemp-stccg-logic\\en-pos-maxent.bin");
        POSModel model = new POSModel(inputStream);
        POSTaggerME tagger = new POSTaggerME(model);
        WhitespaceTokenizer whitespaceTokenizer = WhitespaceTokenizer.INSTANCE;

        Set<String> allTokens = new HashSet<>();
        createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            String[] tokens = whitespaceTokenizer.tokenize(card._rawGameText);
            String[] tags = tagger.tag(tokens);

            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                String newToken = token.replace(",","").replace("(","")
                        .replace(")","").replace(".","")
                        .replace("\"","").replace(";","")
                        .replace(":","").toLowerCase();
                String tag = tags[i];
                words.computeIfAbsent(newToken, k -> new HashSet<>());
                words.get(newToken).add(tag);
            }
        }

        List<String> sortedList = new LinkedList<>(words.keySet());

        int wordsShown = 0;
        int wordsNotShown = 0;
        int wordsProcessed = 0;
        Collections.sort(sortedList);
        for (String string: sortedList) {
            if (words.get(string).contains("VERB") || words.get(string).contains("PUNCT") ||
                    words.get(string).contains("ADV") || words.get(string).contains("ADP") ||
                    words.get(string).contains("NOUN") || words.get(string).contains("PROPN")) {
                wordsProcessed++;
            } else {
                System.out.println(string + " - " + words.get(string));
                wordsShown++;
            }
        }
        System.out.println(wordsShown + " words shown");
        System.out.println(wordsProcessed + " words already processed");
        System.out.println(wordsNotShown + " words remaining");
        System.out.println(words.size() + " words in total");
    }

    private void potentialActions() {
        // forms of to be? - are, be, been, being, is, was, were, weren't
        List<String> actions = new LinkedList<>();
        actions.add("abandon");
        actions.add("abduct"); // abducted, abduction
        actions.add("abort");
        actions.add("achieve"); // achieved
        actions.add("acquire"); // acquired, acquisition
        actions.add("activate"); // activated, deactivated, deactivation
        actions.add("act"); // action, actions
        actions.add("add"); // adds, added
        actions.add("adapt");
        actions.add("advise"); // advice
        actions.add("affect"); // affected, affecting, affects
        actions.add("afflicted");
        actions.add("agree");
        actions.add("allow"); // allowed, allowing, allows
        actions.add("alternate"); // alternating
        actions.add("ambush");
        actions.add("appear"); // appears, disappear, disappears, reappears
        actions.add("apply"); // applying
        actions.add("argue"); // argued
        actions.add("arrive"); // arriving, arrival
        actions.add("ask");
        actions.add("assault");
        actions.add("assassination");
        actions.add("assemble"); // assembly
        actions.add("assert");
        actions.add("assign");
        actions.add("assimilate"); // assimilated, assimilates, assimilation
        actions.add("attack"); // attacked, attacking, re-attack
        actions.add("augment"); // augments
        actions.add("attempt"); // attempted, attempting, also re-attempt
        actions.add("attend");
        actions.add("avoid");
        actions.add("bar");
        actions.add("battle"); // battled, battles, battling
        actions.add("beam"); // beamed, beaming, beams, just-beamed
        actions.add("beating");
        actions.add("become"); // becomes
        actions.add("begs");
        actions.add("begin"); // began, beginning, begins
        // benefit - only used for "benefit from two matching commanders"
        actions.add("bet");
        actions.add("bites"); // Palukoo
        actions.add("bleed");
        actions.add("board"); // boarding
        // brainwash - Prepare the Prisoner uses it as a verb, but really it modifies Brainwash's gametext
        actions.add("brainwash");
        actions.add("brawl");
        actions.add("breed"); // Tribbles, referred to by ...On the Station
        actions.add("bribe"); // bribery
        actions.add("bring"); // brings
        actions.add("build"); // built
        actions.add("burn"); // burns
        // bury - flavor text on Burial Ground
        actions.add("call"); // call "Devidian Door"
        actions.add("cancel"); // canceled, cancelled, cancels
        actions.add("capture"); // captured, captures, capturing
        // carried, carry - only used to describe carrying ships (this is a gamestate)
        actions.add("caught");
        actions.add("cause"); // causes, causing
        actions.add("chain");
        actions.add("change"); // changed, changes
        actions.add("chase"); // chased
        actions.add("choose"); // choice, chooses, chosen
        // cleared - flavor text on Establish Gateway
        actions.add("cloak"); // cloaked, cloaking, cloaks, de-cloak, decloak, decloaked, decloaks, recloak, uncloaked
        actions.add("clone");
        actions.add("close"); // closing
        // coexist - facilities "may coexist"
        actions.add("collect");
        // combat - "engage in combat"
        actions.add("combine"); // combined
        actions.add("come"); // coming
        actions.add("command");
        actions.add("commandeer"); // commandeered, commandeers
        actions.add("compare");
        actions.add("complete"); // completed, completes, completing
        actions.add("compliment");
        actions.add("comply"); // complies
        actions.add("concealed"); // conceals
        actions.add("conduct"); // word-specific gametext; also conducting
        actions.add("consume");
        actions.add("continue"); // continues, continuing
        actions.add("contribute");
        actions.add("control"); // controlled, controlling, controls, controller
        actions.add("convert"); // converted, converts
        // cooperate - only ever "may cooperate" = compatible
        // could - only used on Lack of Preparation
        actions.add("count"); // counting, counts
        actions.add("countdown"); // countdowns
        actions.add("counter-attack");
        actions.add("crash");
        actions.add("create"); // creates
        actions.add("crew"); // crews; think this is always a noun though
        // crushed - flavor text on Kelvan Show of Force
        actions.add("cure"); // cured, curing
        actions.add("damage"); // damaged, damages, damaging, undamaged
        actions.add("dance");
        // de-evolves - flavor text on Barclay's Protomorphosis Disease
        actions.add("declare");
        actions.add("decline");
        // defending? only used by two tactics; this isn't really an action, I don't think
        actions.add("delayed"); // check on this
        actions.add("destroy"); // destroyed, destroying, destroys, destruction, self-destructs, auto-destruct
        // detect - this is only used as flavor text on Tachyon Detection Grid and Mission II's
        actions.add("determined"); // "before a winner is determined"
        // did; only ever used for "did not" as an action history query
        actions.add("die"); // died, dies, death, dead
        actions.add("dig");
        actions.add("direct"); // directive
        actions.add("disable"); // disabled, disables
        actions.add("discard"); // discarded, discarding, discards
        // disruption, disrupted, disruptions - flavor text for timeline disruptions
        // distract - flavor text on Distraction
        actions.add("displaced");
        actions.add("distribution");
        actions.add("divert");
        actions.add("divide");
        actions.add("do"); // does, doesn't, done, undo
        actions.add("dock"); // docking, docked, docks, undock, undocked, undocking
        actions.add("don"); // what is making this?
        actions.add("double"); // doubled, doubles, doubling
        actions.add("download"); // downloaded, downloading, downloads
        actions.add("drain"); // drains
        actions.add("draw"); // drawn, draws, re-draw
        actions.add("duplicate"); // unduplicated
        actions.add("during");
        actions.add("earn"); // earned, earns
        actions.add("eliminated"); // flavor text
        actions.add("encounter"); // encountered, encountering, encounters
        actions.add("end"); // ended, ending, ends
        actions.add("engage"); // engaged, engages, disengage
        // enhance - this is a modifier word
        actions.add("enter"); // entered, enters (enter play)
        actions.add("erase"); // erased, erases
        actions.add("eruption");
        actions.add("escape"); // escapes; flavor text on Firestorm and Hunter Gangs
        // escort - modifier/gamestate - captives can be escorted, but there's no action
        actions.add("evacuate"); // evacuation
        actions.add("evade");
        actions.add("even"); // pretty sure this is never anything, but cannot confirm
        actions.add("examine"); // examined, examines, unexamined
        actions.add("exceed"); // only used as an action for Nightmare
        actions.add("exchange"); // exchanged
        // exclude, excluded, excluding - only used to exclude cards from selection or battle; no responses
        // executing - flavor text on Make a Difference Again, but "begin" is the real action
        actions.add("exhaust"); // used for "exhaust RANGE" and "exhaust draw deck", but no responses
        actions.add("exist");
        actions.add("exit");
        actions.add("expansion");
        actions.add("expire"); // only used for countdowns
        actions.add("explodes"); // explosion
        actions.add("explore");
        actions.add("exploit"); // Isabella & Imperial Intimidation
        actions.add("expose"); // exposed, exposes
        // extend - used to modify modifiers; no responses
        actions.add("face"); // faced, faces, facing; this might be flavor text, not sure
        actions.add("facilitate");
        actions.add("fail"); // fails, failure
        // fight - not an action
        actions.add("file");
        actions.add("finished"); // finishes; pretty sure this is a synonym for end or complete or something
        actions.add("fire"); // fired, firing
        actions.add("flip"); // flipped
        // float - "float in space" on Airlock
        actions.add("fluctuating");
        actions.add("fly"); // flying, flight
        // following (always "the following")
        actions.add("follows"); // Temporal Wake
        actions.add("force"); // forced
        actions.add("forfeit"); // used on To Rule in Hell, no responses
        // form - flavor text on Lineup
        actions.add("free"); // Is this ever used as a verb?
        // function - this is a modifier
        actions.add("gain"); // gained, gains, regains; this might be a modifier though
        actions.add("game"); // games
        actions.add("generate"); // generation, generations
        actions.add("get"); // getting, got; not sure about this
        actions.add("give"); // gives, gift
        actions.add("glance");
        actions.add("go"); // going
        actions.add("guard");
        actions.add("guess"); // guessed
        actions.add("guides");
        // had, has
        actions.add("hail");
        actions.add("hand"); // possibly this is a verb sometimes?
        actions.add("happen"); // Samuel Clemens' Pocketwatch
        actions.add("head");
        actions.add("help"); // helped, helps
        actions.add("hides"); // hidden, hide
        actions.add("hit"); // hits
        actions.add("hunt"); // hunting
        actions.add("hurl");
        actions.add("ignore"); // ignores, ignoring
        actions.add("immune"); // think this is more of a modifier
        actions.add("impact");
        actions.add("impersonate");
        actions.add("implant"); // implants
        actions.add("include"); // included, includes, including
        // increased, increases - this is a modifier, and I don't see a response
        actions.add("incur"); // incurred, incurs
        actions.add("infection");
        actions.add("infiltrate"); // infiltrating, infiltration, infiltrated
        actions.add("influence");
        actions.add("initiate"); // initiated, initiates, initiating, just-initiated
        // insert - only used for inserting into deck or inserting into spaceline; no responses
        actions.add("inspection");
        actions.add("interrupt"); // interrupts; if this is ever a verb, it's probably a synonym for "play interupt"
        // investigate - flavor text
        actions.add("investigation"); // investigations
        actions.add("invitation");
        actions.add("issue");
        actions.add("join"); // joins
        // keep - synonymous with prevent
        actions.add("kill"); // killed, killing, kills, just-killed
        actions.add("land"); // landed, lands, landing
        actions.add("launch"); // launched
        actions.add("leave"); // leaves, leaving, left
        actions.add("limit"); // limited, limits, unlimited; pretty sure this is a modifier (not an action)
        actions.add("live");
        actions.add("loaded");
        actions.add("look"); // looking
        actions.add("lose"); // loses, losing, lost, loss
        actions.add("lowered"); // lowers
        actions.add("lure");
        // make, made, makes; this isn't really a helpful word
        actions.add("maneuver");
        actions.add("match"); // matches, matching; probably not an action
        actions.add("may"); // is this helpful?
        actions.add("meet"); // meets, met, meeting; is this helpful?
        // melt - flavor text on Fire Sculptor
        actions.add("mine"); // mining
        actions.add("miss");
        actions.add("modulate"); // remodulate - flavor text on Remodulation
        actions.add("morph"); // morphs, morphing
        actions.add("move"); // moved, moves, moving
        actions.add("must"); // is this helpful?
        actions.add("name"); // named, names, naming
        actions.add("navigate");
        actions.add("negate");
        // need - modifier
        actions.add("nullify"); // nullified, nullifies, nullifying, nullification
        // observe - flavor text on Holoprogram: Historical Poker Game
        // occupied, occupying, unoccupied - gamestate
        // occur - flavor text on Manheim's Dimensional Door
        actions.add("open"); // opened, opens, reopen, re-opening
        actions.add("operated"); // operates, operation, operations; can't find these in the Trekcc website
        actions.add("opponen&aposs"); // wtf is this
        actions.add("opposed"); // opposing
        actions.add("optimize");
        actions.add("orbit"); // orbiting
        actions.add("order"); // orders
        actions.add("overcome"); // overcomes
        // override - flavor text on Jealous Amanda
        actions.add("pack");
        actions.add("pair");
        actions.add("participate"); // participating
        actions.add("party"); // parties
        actions.add("pass"); // passed, passing
        actions.add("patrol");
        actions.add("peek");
        actions.add("phase"); // phased, phases, dephase, rephase, phasing, dephases
        actions.add("pick");
        actions.add("pilot");
        actions.add("place"); // placed, places, placing, replace, replaced, replaces, replacement
        actions.add("plant");
        actions.add("play"); // played, playing, plays, just-played
        actions.add("point"); // points; probably not an action
        // pool - flavor text on Blood Screening
        actions.add("post"); // posting
        // pounce - flavor text on Spot
        // pre-arrange - weird text on Bodyguards
        actions.add("pre-announce");
        actions.add("prepare"); // preparation
        actions.add("prevent"); // prevented, preventing, prevents
        // primed - flavor text on Activate Thalaron Weapon
        actions.add("probe"); // probed, probing
        actions.add("proceed"); // proceeding, proceedings
        actions.add("process"); // processed, processing
        actions.add("progress");
        actions.add("program");
        actions.add("protect"); // protects, protection
        actions.add("provides"); // Obelisk of Masaka
        actions.add("punishment");
        actions.add("put"); // puts
        actions.add("quarantined");
        actions.add("raid");
        actions.add("ram");
        actions.add("ration"); // rationed
        actions.add("reabsorb"); // reabsorbed
        actions.add("reached"); // reaching
        actions.add("reaction");
        actions.add("rearrange");
        // re-boot - flavor text on Iconian Computer Weapon
        // reception - flavor text on Subspace Interference
        // reclaim - flavor text on HQ: Return Orb to Bajor
        actions.add("recites");
        actions.add("recognize");
        actions.add("record");
        actions.add("recreation");
        actions.add("recruit");
        actions.add("redefine");
        actions.add("redirect"); // redirected
        actions.add("reduce"); // reduced, reduces, reducing, reductions
        actions.add("refuses");
        actions.add("regenerate"); // regenerates
        actions.add("regulate");
        actions.add("relay");
        actions.add("release"); // released
        actions.add("relocate"); // relocated, relocates, relocating, relocation
        actions.add("remain"); // remaining, remains
        actions.add("remove"); // removed, removes
        actions.add("renew"); // renewal, renewed
        actions.add("repair");// repairs
        actions.add("repeats");
        actions.add("replenish"); // replenishing
        actions.add("replies");
        actions.add("report"); // reported, reporting, reports
        actions.add("reprogram");
        actions.add("request");
        actions.add("require"); // required, requires, requiring
        actions.add("rescue"); // rescued, rescues
        actions.add("reserve");
        actions.add("resets");
        // resigns - flavor text on Anaphasic Organism
        actions.add("resolved");
        actions.add("respond");
        actions.add("restore"); // restore, restores, restored
        actions.add("restricted"); // restriction
        actions.add("resumes");
        actions.add("retaliate");
        actions.add("retask");
        actions.add("retire");
        actions.add("retrieve");
        actions.add("return"); // returned, returning, returns
        actions.add("reveal"); // revealed, reveals
        actions.add("reverse"); // reverses
        actions.add("revive"); // revived
        actions.add("revolve"); // revolving
        actions.add("ricochets");
        actions.add("rotate");
        actions.add("round");
        actions.add("rule"); // rules
        actions.add("run"); // runs
        actions.add("sacrifice"); // sacrifices, sacrificing, sacrificed
        actions.add("sail");
        actions.add("salvage");
        actions.add("save"); // saves
        actions.add("say");
        actions.add("scan"); // scans
        actions.add("score"); // scored, scoring, scores
        actions.add("scout"); // scouted, scouting
        actions.add("sealed");
        actions.add("search");
        actions.add("secure");
        actions.add("seed"); // seeded, re-seed, re-seeds, seeds
        actions.add("select"); // selected, re-select, re-selected, selection, selections, selects, unselected
        actions.add("sense");
        actions.add("separate");
        actions.add("serve"); // served
        actions.add("set"); // sets
        actions.add("shape-shift"); // shifting
        actions.add("share"); // shared, sharing, shares
        actions.add("shopping");
        actions.add("shot");
        actions.add("show"); // showing, shown, shows
        actions.add("shroud"); // shrouded
        actions.add("shuffle"); // re-shuffle, shuffled, shuffles, shuffling, unshuffled, reshuffle
        actions.add("skip");
        actions.add("solve"); // solved, solves, solving
        actions.add("specify"); // specifies
        actions.add("split");
        actions.add("spotted");
        actions.add("spread");
        actions.add("staff"); // staffed, staffing, unstaffed
        actions.add("stand");
        actions.add("start"); // started, starting
        actions.add("stated");
        actions.add("stay"); // stays
        actions.add("steal"); // stolen, theft
        actions.add("step");
        actions.add("strike"); // strikes
        actions.add("stock"); // stocked
        actions.add("stop"); // stopped, stopping, stops, unstop, unstopped, unstops
        actions.add("store"); // stored, storage
        actions.add("study");
        actions.add("stun"); // stunned, stuns, stunning
        actions.add("substitute");
        actions.add("subtract");
        actions.add("success");
        actions.add("sucked");
        actions.add("support");
        actions.add("surprise");
        actions.add("survived"); // surviving
        actions.add("suspend"); // suspended, suspends
        actions.add("sweep");
        actions.add("switch");
        actions.add("take"); // taken, takes, taking
        actions.add("target"); // targeted, targeting, targets
        actions.add("team");
        actions.add("tempted");
        actions.add("test"); // tested
        actions.add("think"); // thought
        actions.add("threat");
        // threaten, threatened - flavor text on Protection Racket
        actions.add("tie"); // ties, tied
        actions.add("tossed");
        actions.add("total");
        actions.add("touch");
        actions.add("trade"); // traded, trading
        actions.add("transfer"); // transferred
        actions.add("transport"); // transports
        actions.add("travel"); // traveled, travelled, travels
        actions.add("treat"); // treated
        actions.add("trial");
        actions.add("triple"); // tripled, tripling
        actions.add("turn"); // turned, turns
        actions.add("untransfigured");
        actions.add("upgrade");
        actions.add("use"); // unused, used, uses, using
        actions.add("visit");
        actions.add("volunteer");
        actions.add("wager");
        actions.add("wake");
        actions.add("walk"); // walking
        actions.add("war");
        actions.add("warn"); // warning
        actions.add("watch");
        actions.add("wearing"); // worn
        actions.add("while");
        actions.add("win"); // winning, wins, won
        actions.add("wipe");
        actions.add("wish"); // wishes
        actions.add("wound"); // wounded, wounds
    }

}