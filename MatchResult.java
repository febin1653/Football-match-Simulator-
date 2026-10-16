import java.util.ArrayList;
import java.util.List;

class MatchResult {
    public Team homeTeam;
    public Team awayTeam;
    public int homeGoals;
    public int awayGoals;
    public List<GoalEvent> goalEvents;

    public MatchResult(Team homeTeam, Team awayTeam,
                       int homeGoals, int awayGoals,
                       List<GoalEvent> goalEvents) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.goalEvents = new ArrayList<>(goalEvents);
    }
}
