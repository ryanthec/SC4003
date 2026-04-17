public class main {
	
	static int[][][] payoff = 
	{  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	abstract class Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}

	/* ==========================================
	   YOUR CUSTOM AGENT
	   ========================================== */
	class CheongEnWei_Ryan_Player extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 5) return 0; 
			
			// The 2-Strike Rule
			for (int i = 1; i < n; i++) {
				boolean op1Chronic = (oppHistory1[i] == 1 && oppHistory1[i-1] == 1);
				boolean op2Chronic = (oppHistory2[i] == 1 && oppHistory2[i-1] == 1);
				if (op1Chronic || op2Chronic) return 1; 
			}
			
			int coop1 = 0, coop2 = 0;
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0) coop1++;
				if (oppHistory2[i] == 0) coop2++;
			}
			
			float rate1 = (float) coop1 / n;
			float rate2 = (float) coop2 / n;
			
			float threshold;
			if (n >= 85) threshold = 0.72f;
			else if (n >= 70) threshold = 0.65f;
			else threshold = 0.60f;

			if (rate1 >= threshold || rate2 >= threshold) return 0;
			else return 1;
		}
	}

	/* ==========================================
	   BASELINE ASSIGNMENT AGENTS
	   ========================================== */
	class NicePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) { return 0; }
	}
	class NastyPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) { return 1; }
	}
	class RandomPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return (Math.random() < 0.5) ? 0 : 1; 
		}
	}
	class TolerantPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0, opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0) opponentCoop++; else opponentDefect++;
				if (oppHistory2[i] == 0) opponentCoop++; else opponentDefect++;
			}
			return (opponentDefect > opponentCoop) ? 1 : 0;
		}
	}
	class FreakyPlayer extends Player {
		int action;
		FreakyPlayer() { action = (Math.random() < 0.5) ? 0 : 1; }
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) { return action; }	
	}
	class T4TPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; 
			return (Math.random() < 0.5) ? oppHistory1[n-1] : oppHistory2[n-1];
		}	
	}

	/* ==========================================
	   ENGINE LOGIC
	   ========================================== */
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
			int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
			int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
			int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
			ScoreA += payoff[PlayA][PlayB][PlayC];
			ScoreB += payoff[PlayB][PlayC][PlayA];
			ScoreC += payoff[PlayC][PlayA][PlayB];
			HistoryA = extendIntArray(HistoryA, PlayA);
			HistoryB = extendIntArray(HistoryB, PlayB);
			HistoryC = extendIntArray(HistoryC, PlayC);
		}
		return new float[]{ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
	}
	
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) result[i] = arr[i];
		result[result.length-1] = next;
		return result;
	}
	
	// Pool specifically locked for the Baseline Evaluation
	int numPlayers = 7;
	Player makePlayer(int which) {
		switch (which) {
			case 0: return new CheongEnWei_Ryan_Player();
			case 1: return new NicePlayer();
			case 2: return new NastyPlayer();
			case 3: return new RandomPlayer();
			case 4: return new TolerantPlayer();
			case 5: return new FreakyPlayer();
			case 6: return new T4TPlayer();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	public static void main (String[] args) {
		main instance = new main();
		instance.runBaselineEvaluation();
	}
	
	void runBaselineEvaluation() {
		int totalSimulations = 1000;
		
		// Metrics Tracking
		double[] globalTotalScore = new double[numPlayers];
		long[] globalMatchesPlayed = new long[numPlayers];
		int[] top1Count = new int[numPlayers];
		int[] top3Count = new int[numPlayers];
		int[] rankSum = new int[numPlayers];

		System.out.println("Running Baseline Evaluation: " + totalSimulations + " Iterations...\n");

		for (int sim = 0; sim < totalSimulations; sim++) {
			float[] currentIterationScore = new float[numPlayers];
			int[] currentIterationMatches = new int[numPlayers];

			// Run every possible triple combination
			for (int i = 0; i < numPlayers; i++) {
				for (int j = i; j < numPlayers; j++) {
					for (int k = j; k < numPlayers; k++) {
						Player A = makePlayer(i);
						Player B = makePlayer(j);
						Player C = makePlayer(k);
						
						int rounds = 90 + (int)Math.rint(20 * Math.random());
						float[] matchResults = scoresOfMatch(A, B, C, rounds);
						
						currentIterationScore[i] += matchResults[0];
						currentIterationScore[j] += matchResults[1];
						currentIterationScore[k] += matchResults[2];
						
						currentIterationMatches[i]++;
						currentIterationMatches[j]++;
						currentIterationMatches[k]++;
					}
				}
			}

			// Calculate averages for this single iteration
			double[] iterationAverages = new double[numPlayers];
			for (int i = 0; i < numPlayers; i++) {
				iterationAverages[i] = currentIterationScore[i] / currentIterationMatches[i];
				
				// Add to global totals for the final absolute average
				globalTotalScore[i] += currentIterationScore[i];
				globalMatchesPlayed[i] += currentIterationMatches[i];
			}

			// Rank players for this iteration
			int[] iterationRanks = getRanks(iterationAverages);
			
			for (int i = 0; i < numPlayers; i++) {
				int rank = iterationRanks[i];
				rankSum[i] += rank;
				if (rank == 1) top1Count[i]++;
				if (rank <= 3) top3Count[i]++;
			}
		}

		// Print Results
		System.out.println("==========================================================================================");
		System.out.printf("%-30s | %-12s | %-12s | %-12s | %-12s\n", "Agent Name", "Avg Score", "Top-1 Rate", "Top-3 Rate", "Avg Rank");
		System.out.println("==========================================================================================");
		
		// Sort final printout by Global Average Score
		int[] finalSortedOrder = new int[numPlayers];
		double[] finalAvgScores = new double[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			finalAvgScores[i] = globalTotalScore[i] / globalMatchesPlayed[i];
			finalSortedOrder[i] = i;
		}
		
		for (int i = 1; i < numPlayers; i++) {
			int key = finalSortedOrder[i];
			int j = i - 1;
			while (j >= 0 && finalAvgScores[finalSortedOrder[j]] < finalAvgScores[key]) {
				finalSortedOrder[j + 1] = finalSortedOrder[j];
				j = j - 1;
			}
			finalSortedOrder[j + 1] = key;
		}

		for (int i = 0; i < numPlayers; i++) {
			int p = finalSortedOrder[i];
			String name = makePlayer(p).name();
			double avgScore = finalAvgScores[p];
			double top1Rate = (top1Count[p] / (double)totalSimulations) * 100;
			double top3Rate = (top3Count[p] / (double)totalSimulations) * 100;
			double avgRank = rankSum[p] / (double)totalSimulations;

			System.out.printf("%-30s | %-12.4f | %-11.1f%% | %-11.1f%% | %-12.2f\n", 
					name, avgScore, top1Rate, top3Rate, avgRank);
		}
		System.out.println("==========================================================================================");
	}

	// Helper function to rank an array of scores (1 is highest, handles ties by giving same rank)
	int[] getRanks(double[] scores) {
		int[] ranks = new int[scores.length];
		for (int i = 0; i < scores.length; i++) {
			int rank = 1;
			for (int j = 0; j < scores.length; j++) {
				if (scores[j] > scores[i]) {
					rank++;
				}
			}
			ranks[i] = rank;
		}
		return ranks;
	}
}