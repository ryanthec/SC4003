public class main2 {
	
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

	/* CUSTOM AGENTS */

	class CautiousTriggerPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 1 && oppHistory2[i] == 1) return 1; 
			}
			return 0; 
		}
	}

	class PercentageTolerancePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n < 5) return 0; 
			for (int i = 1; i < n; i++) {
				boolean doubleDefectNow = (oppHistory1[i] == 1 && oppHistory2[i] == 1);
				boolean doubleDefectPrev = (oppHistory1[i-1] == 1 && oppHistory2[i-1] == 1);
				if (doubleDefectNow && doubleDefectPrev) return 1; 
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
            return (Math.random() < 0.1) ? 0 : 1; // forgive 10% of the time
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
            return (defections >= 2) ? 1 : 0; // only punish if both defected
        }
    }


    class SoftTriggerPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            // Punish for 3 rounds after a double defection, then forgive
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
            // Punish proportionally — defect once per accumulated defection
            if (myHistory[n-1] == 1 && totalDefections > 0) totalDefections--;
            return (totalDefections > 0) ? 1 : 0;
        }
    }

    class DefectionRatePlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n < 5) return 0; // cooperate early to gather data
            
            double rate1 = 0, rate2 = 0;
            for (int i = 0; i < n; i++) {
                rate1 += oppHistory1[i]; // 1=defect, 0=coop, so sum = defections
                rate2 += oppHistory2[i];
            }
            rate1 /= n;
            rate2 /= n;
            
            // If BOTH are mostly cooperative, stay cooperative
            if (rate1 < 0.3 && rate2 < 0.3) return 0;
            // If EITHER is a chronic defector, defect
            if (rate1 > 0.6 || rate2 > 0.6) return 1;
            // Otherwise mirror the worse opponent
            return (rate1 > rate2) ? (rate1 > 0.4 ? 1 : 0) : (rate2 > 0.4 ? 1 : 0);
        }
    }

    class EarlyBetrayalPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            int earlyWindow = Math.min(5, n);
            for (int i = 0; i < earlyWindow; i++) {
                if (oppHistory1[i] == 1 || oppHistory2[i] == 1) return 1; // punish permanently
            }
            // After safe early phase, use TfT logic
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
            
            // If either opponent is trending toward defection, preempt
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
            // Probabilistically defect proportional to worst opponent's defection rate
            return (Math.random() < worstRate) ? 1 : 0;
        }
    }

    class TriangulatorPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            
            // If opponents are fighting each other, defect and free-ride
            boolean opp1Defecting = (oppHistory1[n-1] == 1);
            boolean opp2Defecting = (oppHistory2[n-1] == 1);
            
            // If BOTH are defecting, we gain nothing extra by defecting (they'll punish us too)
            if (opp1Defecting && opp2Defecting) return 1; // might as well defect, can't get worse
            
            // If exactly ONE is defecting, this is the sweet spot — defect while they fight
            if (opp1Defecting ^ opp2Defecting) return 1;
            
            // Both cooperating — cooperate to maintain the good equilibrium
            return 0;
        }
    }

    class ReputationExploiterPlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n == 0) return 0;
            
            // Phase 1: Build trust (rounds 0-60)
            if (n < 60) {
                if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
                return 0;
            }
            
            // Phase 2: Exploit (rounds 60+), but only if both opponents are cooperative
            // This extracts the +2 bonus while they're unlikely to fully recover trust
            double rate1 = 0, rate2 = 0;
            for (int i = 0; i < n; i++) { rate1 += oppHistory1[i]; rate2 += oppHistory2[i]; }
            rate1 /= n; rate2 /= n;
            
            if (rate1 < 0.2 && rate2 < 0.2) return 1; // exploit cooperative opponents late
            if (oppHistory1[n-1] == 1 || oppHistory2[n-1] == 1) return 1;
            return 0;
        }
    }

    class ImprovedPercentageTolerancePlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            // Rule 1: Always cooperate for the first 5 moves to build trust
            if (n < 5) return 0; 
            
            // Rule 2: The "Two Strikes" Chronic Defector Check
            // If EITHER opponent defects 2 times in a row, trigger permanent defection
            for (int i = 1; i < n; i++) {
                boolean op1Chronic = (oppHistory1[i] == 1 && oppHistory1[i-1] == 1);
                boolean op2Chronic = (oppHistory2[i] == 1 && oppHistory2[i-1] == 1);
                if (op1Chronic || op2Chronic) {
                    return 1; 
                }
            }
            
            // Rule 3: Calculate individual cooperation rates
            int coop1 = 0, coop2 = 0;
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 0) coop1++;
                if (oppHistory2[i] == 0) coop2++;
            }
            
            float rate1 = (float) coop1 / n;
            float rate2 = (float) coop2 / n;
            
            // Dynamic threshold scaling (gets stricter near the end of the game)
            float threshold;
            if (n >= 85) threshold = 0.72f;
            else if (n >= 70) threshold = 0.65f;
            else threshold = 0.60f;

            // Rule 4: The Cartel Enforcement
            // BOTH opponents must meet the threshold, or we defect to protect ourselves.
            if (rate1 >= threshold && rate2 >= threshold) {
                return 0;
            } else {
                return 1;
            }
        }
    }


	/* SIMPLE AGENTS */
	
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

	/* ENGINE LOGIC */
	
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
			case 0: return new T4TPlayer();
			case 1: return new CautiousTriggerPlayer();
			case 2: return new PercentageTolerancePlayer();
			case 3: return new PavlovPlayer();
			case 4: return new MarkovPredictorPlayer();
			case 5: return new PeacemakerPlayer();
			case 6: return new HitAndRunPlayer();
			case 7: return new TolerantPlayer();
			case 8: return new NicePlayer();
			case 9: return new NastyPlayer();
			case 10: return new RandomPlayer();
            case 11: return new StrictTFTPlayer();
            case 12: return new GenerousTFTPlayer();
            case 13: return new TFTTPlayer();
            case 14: return new MajorityRulePlayer();
            case 15: return new SoftTriggerPlayer();
            case 16: return new GradualPlayer();
            case 17: return new DefectionRatePlayer();
            case 18: return new EarlyBetrayalPlayer();
            case 19: return new TrendDetectorPlayer();
            case 20: return new MirrorWorstPlayer();
            case 21: return new TriangulatorPlayer();
            case 22: return new ReputationExploiterPlayer();
            case 23: return new ImprovedPercentageTolerancePlayer();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	public static void main (String[] args) {
		main2 instance = new main2();
		instance.runSimulation();
	}
	
	void runSimulation() {
		// ==========================================
		// SIMULATION CONFIGURATION
		// ==========================================
		int totalSimulations = 100; // How many random tournaments to run
		int poolSize = 100;           // How many players in ONE tournament

		// Turn on the agents you want to include by listing their IDs here.
		// 0:T4T, 1:Cautious, 2:PercentTolerance, 3:Pavlov, 4:Markov, 
		// 5:Peacemaker, 6:HitAndRun, 7:Tolerant, 8:Nice, 9:Nasty, 10:Random, 
        // 11:StrictTFT, 12:GenerousTFT, 13:TFTT, 14:MajorityRule, 15:SoftTrigger, 
        // 16:Gradual, 17:DefectionRate, 18:EarlyBetrayal, 19:TrendDetector, 
        // 20:MirrorWorst, 21:Triangulator, 22:ReputationExploiter, 23:ImprovedPercentageTolerance
		int[] activeAgents = {0, 0, 0, 0,
            1, 2, 3, 3, 3, 3,
            4, 5, 6, 6,
            7, 11, 11,
            12, 12, 12, 13, 13, 13,
            14, 15, 15, 15,
            16, 17, 18, 18, 19, 
            20, 21, 21, 22, 23};
		// ==========================================

		int maxAgentTypes = 24;
		double[] globalTypeScore = new double[maxAgentTypes];
		long[] globalTypeMatchCount = new long[maxAgentTypes];

		System.out.println("Running " + totalSimulations + " simulations with pool size " + poolSize + "...");

		for (int sim = 0; sim < totalSimulations; sim++) {
			// 1. Generate a random ecosystem for this simulation
			int[] currentEcosystem = new int[poolSize];
			for (int i = 0; i < poolSize; i++) {
				int randomIndex = (int)(Math.random() * activeAgents.length);
				currentEcosystem[i] = activeAgents[randomIndex];
			}

			// 2. Run the tournament (Every triplet plays)
			for (int i = 0; i < poolSize; i++) {
				for (int j = i; j < poolSize; j++) {
					for (int k = j; k < poolSize; k++) {
						Player A = makePlayer(currentEcosystem[i]);
						Player B = makePlayer(currentEcosystem[j]);
						Player C = makePlayer(currentEcosystem[k]);
						
						int rounds = 90 + (int)Math.rint(20 * Math.random());
						float[] matchResults = scoresOfMatch(A, B, C, rounds);
						
						// Accumulate scores and match counts for the specific Agent Types
						globalTypeScore[currentEcosystem[i]] += matchResults[0];
						globalTypeMatchCount[currentEcosystem[i]]++;

						globalTypeScore[currentEcosystem[j]] += matchResults[1];
						globalTypeMatchCount[currentEcosystem[j]]++;

						globalTypeScore[currentEcosystem[k]] += matchResults[2];
						globalTypeMatchCount[currentEcosystem[k]]++;
					}
				}
			}
			
			// Optional: Print a progress tracker if it's taking too long
			if ((sim + 1) % 100 == 0) {
				System.out.println("Completed " + (sim + 1) + " / " + totalSimulations + " simulations.");
			}
		}

		// 3. Process and sort the final averages
		System.out.println("\n=============================================");
		System.out.println("FINAL MONTE CARLO TOURNAMENT RESULTS");
		System.out.println("=============================================");
		
		int activeCount = activeAgents.length;
		int[] sortedOrder = new int[activeCount];
		double[] avgScores = new double[maxAgentTypes];
		
		for (int i = 0; i < activeCount; i++) {
			int type = activeAgents[i];
			if (globalTypeMatchCount[type] > 0) {
				avgScores[type] = globalTypeScore[type] / globalTypeMatchCount[type];
			}
			sortedOrder[i] = type;
		}

		// Sort the active agents by their average score (Insertion Sort)
		for (int i = 1; i < activeCount; i++) {
			int key = sortedOrder[i];
			int j = i - 1;
			while (j >= 0 && avgScores[sortedOrder[j]] < avgScores[key]) {
				sortedOrder[j + 1] = sortedOrder[j];
				j = j - 1;
			}
			sortedOrder[j + 1] = key;
		}

		// Print out the leaderboard
		for (int i = 0; i < activeCount; i++) {
			int type = sortedOrder[i];
			// Create a temporary dummy player just to grab its name
			String name = makePlayer(type).name();
			System.out.printf("%-28s : %.4f average points per round\n", name, avgScores[type]);
		}
	}
}