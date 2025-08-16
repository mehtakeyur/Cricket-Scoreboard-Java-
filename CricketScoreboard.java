import java.util.*;

public class CricketScoreboard {

    static class Team {
        private String name;
        Team(String name) { this.name = name; }
        String getName() { return name; }
    }

    static class Ball {
        private String outcome;
        private int runs;
        private boolean countsAsBall;

        Ball(String outcome, int runs, boolean countsAsBall) {
            this.outcome = outcome;
            this.runs = runs;
            this.countsAsBall = countsAsBall;
        }

        String getOutcome() { return outcome; }
        int getRuns() { return runs; }
        boolean countsAsBall() { return countsAsBall; }

        @Override
        public String toString() { return outcome; }
    }

    static class Innings {
        private Team battingSide;
        private int totalRuns = 0;
        private int wicketsDown = 0;
        private int ballsBowled = 0;
        private int oversLimit;
        private Deque<Ball> history = new ArrayDeque<>();

        Innings(Team battingSide, int oversLimit) {
            this.battingSide = battingSide;
            this.oversLimit = oversLimit;
        }

        void addBall(Ball ball) {
            totalRuns += ball.getRuns();
            if (ball.getOutcome().equals("W")) wicketsDown++;
            if (ball.countsAsBall()) ballsBowled++;
            history.push(ball);
        }

        boolean undoLastBall() {
            if (history.isEmpty()) return false;
            Ball last = history.pop();
            totalRuns -= last.getRuns();
            if (last.getOutcome().equals("W")) wicketsDown--;
            if (last.countsAsBall()) ballsBowled--;
            return true;
        }

        boolean isAllOut() { return wicketsDown == 10; }
        boolean oversFinished() { return ballsBowled >= oversLimit * 6; }

        String oversString() {
            return (ballsBowled / 6) + "." + (ballsBowled % 6);
        }

        int getRuns() { return totalRuns; }
        int getWickets() { return wicketsDown; }
        String getBattingTeam() { return battingSide.getName(); }
        Deque<Ball> getHistory() { return history; }
    }

    static class Match {
        private Team team1, team2;
        private int overs;
        private Innings firstInnings, secondInnings;

        Match(String t1, String t2, int overs) {
            team1 = new Team(t1);
            team2 = new Team(t2);
            this.overs = overs;
        }

        void playInnings(Innings innings, Scanner sc, Integer target) {
            System.out.println("\n--- " + innings.getBattingTeam() + " Innings Start ---");
            System.out.println("Enter ball results (0-6, W, WD, NB). Type END to stop.");

            while (true) {
                if (innings.isAllOut() || innings.oversFinished()) {
                    System.out.println("Innings over!");
                    showScore(innings);
                    break;
                }

                if (target != null && innings.getRuns() > target) {
                    System.out.println(innings.getBattingTeam() + " has chased down the target!");
                    showScore(innings);
                    break;
                }

                System.out.print("Ball input: ");
                String input = sc.nextLine().trim();

                if (input.equalsIgnoreCase("END")) {
                    System.out.println("Innings ended by user.");
                    showScore(innings);
                    break;
                }
                if (input.equalsIgnoreCase("SCORE")) {
                    showScore(innings);
                    continue;
                }
                if (input.equalsIgnoreCase("UNDO")) {
                    if (innings.undoLastBall()) {
                        System.out.println("Last ball undone.");
                        showScore(innings);
                    } else {
                        System.out.println("No ball to undo.");
                    }
                    continue;
                }

                try {
                    Ball ball = interpretInput(input);
                    innings.addBall(ball);
                    showScore(innings);
                } catch (Exception e) {
                    System.out.println("Invalid input. Try again.");
                }
            }
        }

        void startMatch(String tossWinner, String tossLoser) {
            Scanner sc = new Scanner(System.in);

          
            firstInnings = new Innings(new Team(tossWinner), overs);
            playInnings(firstInnings, sc, null);

          
            secondInnings = new Innings(new Team(tossLoser), overs);
            int target = firstInnings.getRuns();
            System.out.println("\nTarget for " + tossLoser + ": " + (target + 1));
            playInnings(secondInnings, sc, target);

            if (secondInnings.getRuns() > target) {
                System.out.println("\n" + tossLoser + " won the match by " +
                        (10 - secondInnings.getWickets()) + " wickets!");
            } else if (secondInnings.getRuns() == target) {
                System.out.println("\nThe match is a TIE!");
            } else {
                System.out.println("\n" + tossWinner + " won the match by " +
                        (target - secondInnings.getRuns()) + " runs!");
            }

            sc.close();
        }
    }

    static Ball interpretInput(String input) {
        input = input.toUpperCase();
        switch (input) {
            case "W": return new Ball("W", 0, true);
            case "0": return new Ball("0", 0, true);
            case "1": return new Ball("1", 1, true);
            case "2": return new Ball("2", 2, true);
            case "3": return new Ball("3", 3, true);
            case "4": return new Ball("4", 4, true);
            case "5": return new Ball("5", 5, true);
            case "6": return new Ball("6", 6, true);
            case "WD": return new Ball("WD", 1, false);
            case "NB": return new Ball("NB", 1, false);
            default: throw new IllegalArgumentException("Unknown input: " + input);
        }
    }

    static void showScore(Innings inn) {
        System.out.println("\n--- Score Update ---");
        System.out.println(inn.getBattingTeam() + ": " +
                inn.getRuns() + "/" + inn.getWickets() +
                " in " + inn.oversString() + " overs");
        System.out.print("Recent balls: ");
        int count = 0;
        for (Ball b : inn.getHistory()) {
            System.out.print(b + " ");
            if (++count == 10) break;
        }
        System.out.println("\n--------------------");
    }

    static String getValidTeamName(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String name = sc.nextLine().trim();
            if (name.matches("[A-Za-z ]+")) {
                return name;
            } else {
                System.out.println("Invalid name! Only letters and spaces are allowed.");
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Cricket Scoreboard ===");

        String team1 = getValidTeamName(sc, "Enter Team 1: ");
        String team2 = getValidTeamName(sc, "Enter Team 2: ");

        System.out.print("Overs limit: ");
        int overs = sc.nextInt();
        sc.nextLine();

        Random rand = new Random();
        String tossWinner = rand.nextBoolean() ? team1 : team2;
        String tossLoser = tossWinner.equals(team1) ? team2 : team1;

        System.out.println("Toss won by: " + tossWinner);
        System.out.println(tossWinner + " will bat first.");

        Match match = new Match(team1, team2, overs);
        match.startMatch(tossWinner, tossLoser);
    }
}
