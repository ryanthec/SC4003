public class main3 {
	
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
	   COMPETITIVE CURATED AGENTS
	   ========================================== */

	class PavlovPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n == 0) return 0; 
			int myLast = myHistory[n-1];
			int score = payoff[myLast][oppHistory1[n-1]][oppHistory2[n-1]];
			if (score >= 5) return myLast;     
			else return 1 - myLast; 
		}
	}

	class MarkovPredictorPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0; 
			int pred1 = predictNextMove(n, oppHistory1);
			int pred2 = predictNextMove(n, oppHistory2);
			int expectedCoop = payoff[0][pred1][pred2];
			int expectedDefect = payoff[1][pred1][pred2];
			if (expectedDefect > expectedCoop) return 1;
			return 0;
		}
		int predictNextMove(int n, int[] oppHistory) {
			int lastMove = oppHistory[n-1];
			int countCoop = 0, countDefect = 0;
			for (int i = 0; i < n - 1; i++) {
				if (oppHistory[i] == lastMove) {
					if (oppHistory[i+1] == 0) countCoop++;
					else countDefect++;
				}
			}
			if (countDefect > countCoop) return 1;
			return 0; 
		}
	}

	class PeacemakerPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 2) return 0; 
			boolean op1Hostile = (oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1);
			boolean op2Hostile = (oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1);
			
			if (op1Hostile || op2Hostile) {
				if (n >= 3 && myHistory[n-1] == 1 && myHistory[n-2] == 1 && myHistory[n-3] == 1) return 0; 
				return 1; 
			}
			return 0; 
		}
	}

	class HitAndRunPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 3) return 0; 
			if (n % 7 == 0) return 1; 
			if (n % 7 == 1 || n % 7 == 2) return 0; 
			if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
			return 0; 
		}
	}
	
    class StrictTFTPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
            return 0;
        }
    }

    class GenerousTFTPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            boolean betrayed = (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1);
            if (!betrayed) return 0;
            return (Math.random() < 0.1) ? 0 : 1; 
        }
    }

    class TFTTPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 2) return 0;
            if (oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1) return 1;
            if (oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1) return 1;
            return 0;
        }
    }

    class MajorityRulePlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int defections = oppHistory1[n-1] + oppHistory2[n-1];
            return (defections >= 2) ? 1 : 0; 
        }
    }

    class SoftTriggerPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            for (int i = Math.max(0, n-3); i < n; i++) {
                if (oppHistory1[i] == 1 && oppHistory2[i] == 1) return 1;
            }
            return 0;
        }
    }

    class GradualPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int totalDefections = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 1) totalDefections++;
                if (oppHistory2[i] == 1) totalDefections++;
            }
            if (myHistory[n-1] == 1 && totalDefections > 0) totalDefections--;
            return (totalDefections > 0) ? 1 : 0;
        }
    }

    class DefectionRatePlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 5) return 0; 
            double rate1 = 0, rate2 = 0;
            for (int i = 0; i < n; i++) {
                rate1 += oppHistory1[i]; 
                rate2 += oppHistory2[i];
            }
            rate1 /= n;
            rate2 /= n;
            if (rate1 < 0.3 && rate2 < 0.3) return 0;
            if (rate1 > 0.6 || rate2 > 0.6) return 1;
            return (rate1 > rate2) ? (rate1 > 0.4 ? 1 : 0) : (rate2 > 0.4 ? 1 : 0);
        }
    }

    class EarlyBetrayalPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int earlyWindow = Math.min(5, n);
            for (int i = 0; i < earlyWindow; i++) {
                if (oppHistory1[i] == 1 || oppHistory2[i] == 1) return 1; 
            }
            if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
            return 0;
        }
    }

    class TrendDetectorPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 10) return 0;
            int window = 5;
            double recent1 = 0, overall1 = 0, recent2 = 0, overall2 = 0;
            for (int i = 0; i < n; i++) {
                overall1 += oppHistory1[i];
                overall2 += oppHistory2[i];
            }
            for (int i = n - window; i < n; i++) {
                recent1 += oppHistory1[i];
                recent2 += oppHistory2[i];
            }
            recent1 /= window; overall1 /= n;
            recent2 /= window; overall2 /= n;
            
            boolean trending1 = (recent1 - overall1 > 0.2);
            boolean trending2 = (recent2 - overall2 > 0.2);
            if (trending1 || trending2) return 1;
            return 0;
        }
    }

    class MirrorWorstPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            double rate1 = 0, rate2 = 0;
            for (int i = 0; i < n; i++) {
                rate1 += oppHistory1[i];
                rate2 += oppHistory2[i];
            }
            rate1 /= n; rate2 /= n;
            double worstRate = Math.max(rate1, rate2);
            return (Math.random() < worstRate) ? 1 : 0;
        }
    }

    class TriangulatorPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            boolean opp1Defecting = (oppHistory1[n-1] == 1);
            boolean opp2Defecting = (oppHistory2[n-1] == 1);
            if (opp1Defecting && opp2Defecting) return 1; 
            if (opp1Defecting ^ opp2Defecting) return 1;
            return 0;
        }
    }

    class ReputationExploiterPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            if (n < 60) {
                if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
                return 0;
            }
            double rate1 = 0, rate2 = 0;
            for (int i = 0; i < n; i++) { rate1 += oppHistory1[i]; rate2 += oppHistory2[i]; }
            rate1 /= n; rate2 /= n;
            
            if (rate1 < 0.2 && rate2 < 0.2) return 1; 
            if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
            return 0;
        }
    }

	/* ==========================================
	   SIMPLE AGENTS
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
	
	Player makePlayer(int which) {
		switch (which) {
			case 0 :return new CheongEnWei_Ryan_Player();
			case 1 :return new T4TPlayer();
			case 2 :return new TFTTPlayer();
			case 3: return new PavlovPlayer();
			case 4: return new NicePlayer();
			case 5: return new NastyPlayer();
			case 6: return new TolerantPlayer();
			case 7: return new HitAndRunPlayer();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	public static void main (String[] args) {
		main3 instance = new main3();
		instance.runSimulation();
	}
	
	void runSimulation() {
		// ==========================================
		// SIMULATION CONFIGURATION
		// ==========================================
		int totalSimulations = 1000; 

		// Array containing exactly ONE of every agent ID (0 through 12).
		// By using this directly as the ecosystem, we guarantee a perfectly flat, 
		// unbiased tournament where every agent appears exactly once per simulation.
		int[] activeAgents = {0, 1, 2, 3, 4, 5, 6, 7};
		int poolSize = activeAgents.length;
		// ==========================================
		
		// Global metrics for final output
		double[] globalTotalScore = new double[poolSize];
		long[] globalMatchesPlayed = new long[poolSize];
		int[] top1Count = new int[poolSize];
		int[] top3Count = new int[poolSize];
		int[] rankSum = new int[poolSize];

		System.out.println("Running Meta Evaluation: " + totalSimulations + " Iterations (Flat 1-of-Each Pool)...");

		for (int sim = 0; sim < totalSimulations; sim++) {
			
			// 1. The ecosystem is fixed to exactly one of each agent.
			int[] currentEcosystem = activeAgents;

			// Iteration-level tracking
			double[] iterScore = new double[poolSize];
			int[] iterMatches = new int[poolSize];

			// 2. Run the tournament combinations
			for (int i = 0; i < poolSize; i++) {
				for (int j = i; j < poolSize; j++) {
					for (int k = j; k < poolSize; k++) {
						Player A = makePlayer(currentEcosystem[i]);
						Player B = makePlayer(currentEcosystem[j]);
						Player C = makePlayer(currentEcosystem[k]);
						
						int rounds = 90 + (int)Math.rint(20 * Math.random());
						float[] matchResults = scoresOfMatch(A, B, C, rounds);
						
						// Accumulate scores for the specific Agents
						iterScore[i] += matchResults[0];
						iterMatches[i]++;

						iterScore[j] += matchResults[1];
						iterMatches[j]++;

						iterScore[k] += matchResults[2];
						iterMatches[k]++;
						
						// Add to absolute global totals
						globalTotalScore[i] += matchResults[0];
						globalMatchesPlayed[i]++;
						globalTotalScore[j] += matchResults[1];
						globalMatchesPlayed[j]++;
						globalTotalScore[k] += matchResults[2];
						globalMatchesPlayed[k]++;
					}
				}
			}

			// 3. Calculate this iteration's average score for each Agent
			double[] iterAvg = new double[poolSize];
			for (int t = 0; t < poolSize; t++) {
				iterAvg[t] = iterScore[t] / iterMatches[t];
			}

			// 4. Rank the Agents for this iteration
			for (int i = 0; i < poolSize; i++) {
				int rank = 1;
				for (int j = 0; j < poolSize; j++) {
					if (iterAvg[j] > iterAvg[i]) {
						rank++;
					}
				}
				
				// Update global metrics
				rankSum[i] += rank;
				if (rank == 1) top1Count[i]++;
				if (rank <= 3) top3Count[i]++;
			}
			
			if ((sim + 1) % 100 == 0) {
				System.out.println("Completed " + (sim + 1) + " / " + totalSimulations + " simulations.");
			}
		}

		// 5. Final Output Processing
		System.out.println("\n==========================================================================================");
		System.out.println("FINAL META TOURNAMENT RESULTS (Sorted by Global Avg Score)");
		System.out.println("==========================================================================================");
		System.out.printf("%-35s | %-12s | %-12s | %-12s | %-12s\n", "Agent Name", "Avg Score", "Top-1 Rate", "Top-3 Rate", "Avg Rank");
		System.out.println("==========================================================================================");
		
		// Sort final printout by Global Average Score
		int[] finalSortedOrder = new int[poolSize];
		double[] finalAvgScores = new double[poolSize];
		for (int i = 0; i < poolSize; i++) {
			finalAvgScores[i] = globalTotalScore[i] / globalMatchesPlayed[i];
			finalSortedOrder[i] = i;
		}
		
		for (int i = 1; i < poolSize; i++) {
			int key = finalSortedOrder[i];
			int j = i - 1;
			while (j >= 0 && finalAvgScores[finalSortedOrder[j]] < finalAvgScores[key]) {
				finalSortedOrder[j + 1] = finalSortedOrder[j];
				j = j - 1;
			}
			finalSortedOrder[j + 1] = key;
		}

		for (int i = 0; i < poolSize; i++) {
			int p = finalSortedOrder[i];
			
			String name = makePlayer(p).name();
			double avgScore = finalAvgScores[p];
			double top1Rate = (top1Count[p] / (double)totalSimulations) * 100;
			double top3Rate = (top3Count[p] / (double)totalSimulations) * 100;
			double avgRank = rankSum[p] / (double)totalSimulations;

			System.out.printf("%-35s | %-12.4f | %-11.1f%% | %-11.1f%% | %-12.2f\n", 
					name, avgScore, top1Rate, top3Rate, avgRank);
		}
		System.out.println("==========================================================================================");
	}
}