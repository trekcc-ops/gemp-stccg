const MOTDS = [
    "50% off sale!",
    "Dukat did nothing wrong!",
    "From the files of Aamin Marritza!",
    "Half the calories of 1E Traditional!",
    "Starring Two-Gun Tomalak!",
    "aka Project LetVar",
    "Dozens of players!",
]

export default function rand_motd() {
    let rand_index = Math.floor(Math.random() * MOTDS.length);
    return MOTDS[rand_index];
}