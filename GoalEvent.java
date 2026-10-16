class GoalEvent {
    public Team team;
    public Player scorer;
    public Player assister;

    public GoalEvent(Team team, Player scorer, Player assister) {
        this.team = team;
        this.scorer = scorer;
        this.assister = assister;
    }
}
