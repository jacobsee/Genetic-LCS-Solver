/*
 *
 * Project 1
 * Evolutionary Computing / UAA / Spring 2017
 * Dr. Moore
 *
 * Author     : Jacob See (jdsee@alaska.edu)
 *						: Neil Reutov (nreutov3@alaska.edu)
 *
 */

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.PrintWriter;
import java.util.Scanner;

public class LCSSolver{

	public static String testStringA, testStringB;
	public static char choice;

	// Interactive initialization.

	public static void changeString(){
		Scanner keyboard = new Scanner(System.in);
		System.out.println("To start, there are 5 options to choose from:\n" +
											 "1. Easy\n" +
											 "2. Medium\n" +
											 "3. Hard\n" +
											 "4. Custom\n" +
											 "5. Quit");
		choice = keyboard.nextLine().charAt(0);
		if(choice == '1'){
			testStringA = "providence";
			testStringB = "thepresident";
		}
		else if(choice == '2'){
			testStringA = "mgkzpbjxwhpnvtvzxmxytohapruqhhgqbqmvvflq";
    	testStringB = "hgvvltybqxyfipxynlhvrcszkifxzfqrjbbkiyfg";
		}
		else if(choice == '3'){
			testStringA = "fjptopgabhtoopoahvkzzlnncuauwgcegqvxcxzexeyjjyhrixamprqutckr";
    	testStringB = "oqsyzcbqchfypfhsqlkbagvmpinhzoubtlbulsvwkwivuetyyaefajuajvbp";
		}
		else if(choice == '4'){
			System.out.println("Please enter string one:\n");
			testStringA = keyboard.nextLine();
			System.out.println("Please enter string two:\n");
			testStringB = keyboard.nextLine();
		}
		else if(choice != '5'){
			System.out.println("Error, chosen choice not listed!");
		}
		if(choice == '5'){
			keyboard.close();
			System.exit(0);
	  }
	}

	// Use a different algorithm to compute a stopping point for the GA.
	// Adapted from https://en.wikipedia.org/wiki/Longest_common_subsequence_problem

  public static int ExpectedLCSLength(String stringA, String stringB, int m, int n){
    int[][] c = new int[stringA.length()+1][stringB.length()+1];
    for(int i = 0; i <= stringA.length(); i++){
      c[i][0] = 0;
    }
    for(int j = 0; j <= stringB.length(); j++){
      c[0][j] = 0;
    }
    for(int i = 1; i <= stringA.length(); i++){
      for(int j = 1; j <= stringB.length(); j++){
        if(stringA.charAt(i-1) == stringB.charAt(j-1)){
          c[i][j] = c[i-1][j-1] + 1;
        }else{
          c[i][j] = Math.max(c[i][j-1], c[i-1][j]);
        }
      }
    }
    return c[stringA.length()][stringB.length()];
  }

  public static void main(String args[]){
    //Set up our two strings from which to evolve the LCS.
		do{
			changeString();
	    //Create and initialize a random population of candidates

	    int bitLength = Math.min(testStringA.length(), testStringB.length());
	    int populationSize = (int)Math.pow(bitLength, 2);
	    if(populationSize > 5000){
	        populationSize = 5000;
	    }
			populationSize = 1000;

	    double candidateMutationProbability = 0.05;
	    double bitMutationRate = 0.5;
      int expectedLength = ExpectedLCSLength(testStringA, testStringB, testStringA.length(), testStringB.length());

	    ArrayList<Candidate> population = new ArrayList<Candidate>();

	    for(int i = 0; i < populationSize; i++){
	      population.add(new Candidate(bitLength));
	    }

	    System.out.println("\nCreated a population with " + populationSize + " members.");
      System.out.println("We are targeting a length of " + expectedLength + ".");

	    System.out.println("\nGeneration\t|\tAvg. Fitness\t|\tHigh Fitness\t|\tProgress");
	    System.out.println("-----------------------------------------------------------");

	    // Begin evolution

	    boolean solved = false;
	    Candidate perfectCandidate = null;
	    int generation = 0;
	    ArrayList<Double> generationHistory = new ArrayList<Double>();

	    try( PrintWriter out = new PrintWriter("results.csv") ){

	      while(!solved){

	        generation++;

	        int fitnessSum = 0;
	        double probabilitySum = 0;
	        int highestFitness = 0;
					int longestValidLength = 0;

	        // Need to compute all fitnesses and the sum of all fitnesses for roulette selection. Also check for the perfect candidate.

	        for(int i = 0; i < populationSize; i++){
	          population.get(i).calculateFitness(testStringA, testStringB);
	          fitnessSum += population.get(i).fitness;
	          if(population.get(i).fitness > highestFitness){
	            highestFitness = population.get(i).fitness;
	          }
	          if(population.get(i).isValid){
							if(population.get(i).stringLength == expectedLength){
		            perfectCandidate = population.get(i);
								longestValidLength = perfectCandidate.stringLength;
		            solved = true;
							}else{
								longestValidLength = population.get(i).stringLength;
							}
	          }
	        }

	        System.out.println(generation + "\t\t|\t" + String.format("%.2f", (float)fitnessSum / populationSize) + " \t\t|\t" + highestFitness + "\t|\t" + longestValidLength + "/" + expectedLength + "  (" + ((float)longestValidLength/expectedLength)*100 + "% Evolved)");

	        out.println(generation + "," + ((float)fitnessSum / populationSize) + "," + highestFitness);

	        generationHistory.add((double)fitnessSum / populationSize);

	        if(!solved){

	          // No perfect candidate... We'll need to generate a new population.

	          // Set the population up for roulette selection.

	          ArrayList<Candidate> temp = new ArrayList<Candidate>();

	          for(int i = 0; i < populationSize; i++){
	            temp.add(population.get(i).clone());
	            temp.get(i).rouletteSlice = probabilitySum + ((double)temp.get(i).fitness / fitnessSum);
	            probabilitySum = temp.get(i).rouletteSlice;
	          }

	          population.clear();

						ArrayList<String> usedBitstrings = new ArrayList<String>();

	          // Begin spawning next generation of candidates

	          while(population.size() < populationSize){

	              // Begin roulette selection

	              Candidate selectedFirst = null;
	              Candidate selectedSecond = null;
	              while(selectedFirst == null || selectedSecond == null){
	                double rouletteBall = ThreadLocalRandom.current().nextDouble(1);
	                for(int i = 0; i < populationSize; i++){
	                  if((i > 0 && (rouletteBall < temp.get(i).rouletteSlice && rouletteBall > temp.get(i - 1).rouletteSlice)) || (i < 0 && (rouletteBall < temp.get(i).rouletteSlice))) {
	                    if(selectedFirst == null){
	                      selectedFirst = temp.get(i);
	                    }else{
	                      selectedSecond = temp.get(i);
	                    }
	                  }
	                }
	              }

	              if(ThreadLocalRandom.current().nextDouble(1) > candidateMutationProbability){

	                // Begin crossover of selected candidates to create child candidate

	                int slicePoint = ThreadLocalRandom.current().nextInt(1, bitLength); //Crossover at least 1 char at the very beginning or very end, never before or after the entire bitstring
	                String newBitstring1 = selectedFirst.bitstring.substring(0, slicePoint) + selectedSecond.bitstring.substring(slicePoint);
									String newBitstring2 = selectedSecond.bitstring.substring(0, slicePoint) + selectedFirst.bitstring.substring(slicePoint);

									if(Collections.frequency(usedBitstrings, newBitstring1) < 200){ // Limit a single bitstring to 200 members of the next generation, to encourage diversity.
										population.add(new Candidate(newBitstring1));
										usedBitstrings.add(newBitstring1);
									}
									if(Collections.frequency(usedBitstrings, newBitstring2) < 200){
										population.add(new Candidate(newBitstring2));
										usedBitstrings.add(newBitstring2);
									}

	              }else{

	                // Begin mutation of child candidate

	                String bitStringCopy = "";
	                for(int i = 0; i < selectedFirst.bitstring.length(); i++){
	                  if(ThreadLocalRandom.current().nextDouble(1) < bitMutationRate){
	                    bitStringCopy += selectedFirst.bitstring.charAt(i) == '0' ? '1' : '0';
	                  }else{
	                    bitStringCopy += selectedFirst.bitstring.charAt(i);
	                  }
	                }
	                String newBitstring = bitStringCopy;

									if(Collections.frequency(usedBitstrings, newBitstring) < 200){ // Limit a single bitstring to 200 members of the next generation, to encourage diversity.
										population.add(new Candidate(newBitstring));
										usedBitstrings.add(newBitstring);
									}

	              }

	          }

	          probabilitySum = 0;
	          fitnessSum = 0;

	        }

	      }

	    }
	    catch(Exception e){
        System.out.println("There was a problem writing output data. Exiting.");
        System.exit(0);
	    }

	    // Let's actually figure out what that bitstring represents.

	    System.out.println("\nPerfect candidate " + perfectCandidate.bitstring + " found in generation " + generation + " with a fitness of " + perfectCandidate.fitness + " yields LCS '" + perfectCandidate.toString(testStringA.length() < testStringB.length() ? testStringA : testStringB) + "'.\n");

		}while(choice != '5');
  }

}
