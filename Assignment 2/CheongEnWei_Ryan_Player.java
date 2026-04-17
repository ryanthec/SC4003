class CheongEnWei_Ryan_Player extends Player {

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // Initial phase: cooperate always
			if (n < 5) return 0; 
			
            
            // Trigger A: permanent defection if both opponents defect together for two consecutive rounds.
			for (int i = 1; i < n; i++) {
				boolean op1Chronic = (oppHistory1[i] == 1 && oppHistory1[i-1] == 1);
				boolean op2Chronic = (oppHistory2[i] == 1 && oppHistory2[i-1] == 1);
				if (op1Chronic || op2Chronic) return 1; 
			}
			
            // Checks cooperation rates of both opponents and compares to a dynamic threshold.
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