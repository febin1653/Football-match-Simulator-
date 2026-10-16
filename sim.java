import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class Randomizer {
    public static double randomBetween(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static int randomBetweenInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}

class Player {
    public String name;
    public byte OVR;
    public byte Finishing;
    public byte Passing;
    public byte LongShots;
    public byte interceptions;
    public byte Aggressiveness;
    public byte gkstat;
    public byte number;
    public byte present = 1;

    public Player(String name, int ovr, int finishing, int passing, int longShots, int interceptions,
            int aggressiveness, int gkstat, int number) {
        this.name = name;
        this.OVR = (byte) ovr;
        this.Finishing = (byte) finishing;
        this.Passing = (byte) passing;
        this.LongShots = (byte) longShots;
        this.interceptions = (byte) interceptions;
        this.Aggressiveness = (byte) aggressiveness;
        this.gkstat = (byte) gkstat;
        this.number = (byte) number;
    }
}

class Team {
    public String name;
    public int Team_OVR;
    public int Domestic_Prestige = 5;
    public double defence_threshold;
    // OPTIMIZATION 2: Replaced synchronized Vector with ArrayList
    public List<Player> t;

    public Team(String name, List<Player> players) {
        this.name = name;
        this.t = players;
        this.Team_OVR = 0;

        int totalDefence = 0;
        int defenderCount = 0;
        int gkRating = 0;

        for (int i = 0; i < t.size(); i++) {
            Player p = t.get(i);
            this.Team_OVR += p.OVR;

            if (p.number == 1) {
                gkRating = p.gkstat;
            } else if (p.number >= 2 && p.number <= 5) {
                totalDefence += p.OVR;
                defenderCount++;
            }
        }

        if (!t.isEmpty()) {
            this.Team_OVR /= t.size();
        }

        double avgDefence = defenderCount > 0 ? (double) totalDefence / defenderCount : 50;
        this.defence_threshold = 20 + (0.35 * avgDefence) + (0.25 * gkRating);
    }

    public void prestige(int num) {
        this.Domestic_Prestige = num;
    }

    public Player findHighestAggressive() {
        Player highest = null;
        for (int i = 0; i < t.size(); i++) {
            Player p = t.get(i);
            if (p.present == 1) {
                if (highest == null || p.Aggressiveness > highest.Aggressiveness) {
                    highest = p;
                }
            }
        }
        return highest;
    }
}

class Match {
    public int match_importance = 1;
    public Team homeTeam;
    public Team awayTeam;
    public int Hgoals = 0;
    public int Agoals = 0;
    public List<GoalEvent> goalEvents = new ArrayList<>();
    public double final_powerscore(Team team) {
        return (0.72 * team.Team_OVR) + (0.10 * team.Domestic_Prestige * 10) + (10 * match_importance)
                + Randomizer.randomBetween(-12, 12);
    }

    public double final_defence_threshold(Team team) {
        return team.defence_threshold + Randomizer.randomBetween(-8, 8);
    }

    public double power_shot(Player player) {
        return (0.75 * player.Finishing) + (0.20 * player.LongShots) + Randomizer.randomBetween(-10, 10);
    }

    public double assist_quality(Player creator) {
        return (0.20 * creator.Passing) + Randomizer.randomBetween(-5, 5);
    }

    // OPTIMIZATION 3: Reuse passed-in array to eliminate allocations during event
    // loops
    public void populateAttBucket(Team team, Player[] att) {
        for (int i = 0; i < 5; i++)
            att[i] = null;

        for (int i = 0; i < team.t.size(); i++) {
            Player p = team.t.get(i);
            if (p.number == 11)
                att[4] = p;
            else if (p.number == 10)
                att[3] = p;
            else if (p.number == 9)
                att[2] = p;
            else if (p.number >= 6 && p.number <= 8 && att[1] == null)
                att[1] = p;
            else if (p.number >= 2 && p.number <= 5 && att[0] == null)
                att[0] = p;
        }
        for (int i = 0; i < 5; i++) {
            if (att[i] == null) {
                att[i] = team.t.get(i % team.t.size());
            }
        }
    }

    public int allocate_match_events(Team A, Team B) {
        double points = A.Team_OVR + B.Team_OVR + Randomizer.randomBetween(5, 10) + (10 * match_importance - 5);
        if (points <= 36)
            return 3;
        else if (points <= 72)
            return 4;
        else if (points <= 108)
            return 5;
        else if (points <= 144)
            return 6;
        else if (points <= 180)
            return 7;
        else if (points < 218)
            return 8;
        else
            return 9;
    }

    public Match(Team H, Team A, int importance) {
        this.homeTeam = H;
        this.awayTeam = A;
        this.match_importance = importance;

        Hgoals = 0;
        Agoals = 0;
        goalEvents.clear();

        // Reset temporary red-card status for the new match.
        for (Player p : H.t) p.present = 1;
        for (Player p : A.t) p.present = 1;

        int totalEvents = allocate_match_events(H, A);
        double hPower = final_powerscore(H);
        double aPower = final_powerscore(A);

        int HEvents = (int) Math.round(totalEvents * (hPower / (hPower + aPower)));
        int AEvents = totalEvents - HEvents;

       

        // Reusable array buffer to prevent heap allocation per event
        Player[] attBucket = new Player[5];

        System.out.println("--- MATCH START: " + H.name + " vs " + A.name + " ---");
        System.out.println(
                "Total Generated Events: " + totalEvents + " (Home: " + HEvents + " | Away: " + AEvents + ")\n");

        while (totalEvents > 0) {
            Team attackingTeam;
            Team defendingTeam;

            if (HEvents > 0 && AEvents > 0) {
                if (Randomizer.randomBetweenInt(0, 1) == 0) {
                    attackingTeam = H;
                    defendingTeam = A;
                    HEvents--;
                } else {
                    attackingTeam = A;
                    defendingTeam = H;
                    AEvents--;
                }
            } else if (HEvents > 0) {
                attackingTeam = H;
                defendingTeam = A;
                HEvents--;
            } else {
                attackingTeam = A;
                defendingTeam = H;
                AEvents--;
            }

            int eventRoll = Randomizer.randomBetweenInt(1, 100);

            if (eventRoll > 75) {
                Player bookedPlayer = attackingTeam.findHighestAggressive();
                int cardSeverityRoll = Randomizer.randomBetweenInt(1, 100);

                if (bookedPlayer != null) {
                    if (cardSeverityRoll > 95) {
                        System.out.println("RED CARD! " + attackingTeam.name + " - " + bookedPlayer.name);
                        bookedPlayer.present = 0;
                    } else {
                        System.out.println("YELLOW CARD! " + attackingTeam.name + " - " + bookedPlayer.name);
                    }
                }
            } else {
                populateAttBucket(attackingTeam, attBucket);
                int finisherIdx = Randomizer.randomBetweenInt(0, 4);
                int creatorIdx = (finisherIdx + 1) % 5;

                Player shooter = attBucket[finisherIdx];
                Player creator = attBucket[creatorIdx];

                if (shooter.present == 1) {
                    double shotPower = power_shot(shooter);
                    double baseDef = final_defence_threshold(defendingTeam);
                    double assistBonus = assist_quality(creator);
                    double effectiveDef = baseDef - assistBonus;

                    if (shotPower >= effectiveDef) {
                        if (attackingTeam == H)
                            Hgoals++;
                        else
                            Agoals++;
                        goalEvents.add(new GoalEvent(attackingTeam, shooter, creator));

                        System.out.println("GOAL FOR " + attackingTeam.name + "! Scored by " + shooter.name
                                + " Assist: " + creator.name );
                    } else {
                        System.out.println("CHANCE MISSED by " + attackingTeam.name + " (" + shooter.name
                                + ") [Shot Power: " + String.format("%.2f", shotPower) + " vs Effective Barrier: "
                                + String.format("%.2f", effectiveDef) + "]");
                    }
                } else {
                    System.out.println("Opportunity wasted by " + attackingTeam.name + " (Player sent off)");
                }
            }

            totalEvents--;
        }

        System.out.println("\n--- FINAL SCORE ---");
        System.out.println(H.name + " " + Hgoals + " - " + Agoals + " " + A.name);
    }

    public MatchResult getResult() {
        return new MatchResult(homeTeam, awayTeam, Hgoals, Agoals, goalEvents);
    }
}

public class sim {
    private static Team loadTeam(String name) throws IOException {
        Path path = Paths.get("D:\\Project_\\fc sim\\teams\\" + name + ".txt");
        List<String> lines = Files.readAllLines(path);

        List<Player> players = new ArrayList<>();
        for (int i = 2; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            players.add(new Player(
                    parts[0],
                    Byte.parseByte(parts[1]),
                    Byte.parseByte(parts[2]),
                    Byte.parseByte(parts[3]),
                    Byte.parseByte(parts[4]),
                    Byte.parseByte(parts[5]),
                    Byte.parseByte(parts[6]),
                    Byte.parseByte(parts[7]),
                    Byte.parseByte(parts[8])));
        }

        Team t = new Team(name, players);
        t.prestige(Integer.parseInt(lines.get(1)));
        return t;
    }

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Home Team: ");
        String homeName = sc.nextLine();

        System.out.print("Enter Away Team: ");
        String awayName = sc.nextLine();

        Team homeTeam = loadTeam(homeName);
        Team awayTeam = loadTeam(awayName);

        Match match = new Match(homeTeam, awayTeam, 2);
        MatchResult result = match.getResult();
        
        sc.close();
    }
}