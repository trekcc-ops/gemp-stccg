const MOTDS = [
    // Sensible chuckles
    "50% off sale!",
    "Void where prohibited.",
    "Mehr Spa\u00DF als legal erlaubt!",
    "Paul is dead!",

    // Trek jokes and references
    // TOS
    "Fascinating.",
    "I'm a card game, not a doctor!",
    "KHAAAAAAAAAN!",
    // TNG
    "There are five lights!",
    // DS9
    "Dukat did nothing wrong!",
    "From the files of Aamin Marritza!",
    "Wait, Quark owns a bar?!?",
    "More insidious than root beer!",
    "Dabo!",
    // VOY
    "There's coffee in that nebula!",

    // GEMP/TrekCC inside jokes and references
    "Dozens of players!",
    "aka Project LetVar",
    "Starring Two-Gun Tomalak!",
    "Half the calories of 1E Traditional!",
]

export default function rand_motd() {
    let rand_index = Math.floor(Math.random() * MOTDS.length);
    return MOTDS[rand_index];
}