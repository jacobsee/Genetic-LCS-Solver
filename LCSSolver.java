import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.PrintWriter;

public class LCSSolver{

  public static void main(String args[]){

    //Set up our two strings from which to evolve the LCS.
    //Todo: Add interactivity

    //String testStringA = "providence";
    //String testStringB = "president"; //Correct bitstring is 110011110 with a fitness equalling the length of 9

    String testStringA = "aawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbm";
    String testStringB = "certtPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwe";

    //String testStringA = "aawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbmaawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbmaawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbmaawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbmaawocPvsrclzstcsreszetcazcsczsecszeGcseiscvhxdtehxrgUzfgvsrAsdvnvAvbm";
    //String testStringB = "certtPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwecerttPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwecerttPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwecerttPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwecerttPzfnnlewytweebnjadadnsqweembkiGygyiiovqweyebvzaUbrsasfAawrnbAqwe";

    //String testStringA = "supercalifragilisticexpialidocious";
    //String testStringB = "spoercalifalfekleficexalfnkfdlioadf";

    //Create and initialize a random population of candidates

    int bitLength = Math.min(testStringA.length(), testStringB.length());
    int populationSize = (int)Math.pow(bitLength, 2);
    if(populationSize > 5000){
        populationSize = 5000;
    }

    double candidateMutationProbability = 0.25;
    double bitMutationRate = 0.25;

    ArrayList<Candidate> population = new ArrayList<Candidate>();

    for(int i = 0; i < populationSize; i++){
      population.add(new Candidate(bitLength));
    }

    System.out.println("\nCreated a population with " + populationSize + " members.");
    System.out.println("We are targeting a fitness of " + bitLength + ".");

    System.out.println("\nGeneration\t|\tAvg. Fitness\t|\tHigh Fitness");
    System.out.println("-----------------------------------------------------------");

    // THE GOOD STUFF

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

        // Need to compute all fitnesses and the sum of all fitnesses for roulette selection. Also check for the perfect candidate.

        for(int i = 0; i < populationSize; i++){
          population.get(i).calculateFitness(testStringA, testStringB);
          fitnessSum += population.get(i).fitness;
          if(population.get(i).fitness > highestFitness){
            highestFitness = population.get(i).fitness;
          }
          if(population.get(i).fitness == bitLength){
            perfectCandidate = population.get(i);
            solved = true;
          }
        }

        System.out.println(generation + "\t\t|\t" + String.format("%.2f", (float)fitnessSum / populationSize) + " \t\t|\t" + highestFitness);

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

              String newBitstring = "";

              if(ThreadLocalRandom.current().nextDouble(1) > candidateMutationProbability){

                // Begin crossover of selected candidates to create child candidate

                int slicePoint = ThreadLocalRandom.current().nextInt(1, bitLength); //Crossover at least 1 char at the very beginning or very end, never before or after the entire bitstring
                newBitstring = selectedFirst.bitstring.substring(0, slicePoint) + selectedSecond.bitstring.substring(slicePoint);

              }else{

                // Begin mutation of child candidate (possibly)

                String bitStringCopy = "";
                for(int i = 0; i < selectedFirst.bitstring.length(); i++){
                  if(ThreadLocalRandom.current().nextDouble(1) < bitMutationRate){
                    bitStringCopy += selectedFirst.bitstring.charAt(i) == '0' ? '1' : '0';
                  }else{
                    bitStringCopy += selectedFirst.bitstring.charAt(i);
                  }
                }
                newBitstring = bitStringCopy;

              }

              // Newly spawned child candidate becomes a functioning member of society.

              population.add(new Candidate(newBitstring));

          }

          probabilitySum = 0;
          fitnessSum = 0;

          // Higher bitstring lengths were significantly improved by reducing mutation rate if fitness begins to plateau.

          if(generation > 5){
            if(Math.abs(generationHistory.get(generation - 5) - generationHistory.get(generation - 1)) < 0.5 && bitMutationRate > 0.01){
              System.out.println("Below target fitness improvement... Decreasing probability of mutation.");
              bitMutationRate /= 2;
              candidateMutationProbability /= 2;
            }
          }

        }

      }

    }
    catch(Exception e){

    }

    // Let's actually figure out what that bitstring represents.

    String LCS = "";

    for(int i = 0; i < perfectCandidate.bitstring.length(); i++){
      if(perfectCandidate.bitstring.charAt(i) == '1'){
        LCS += testStringA.charAt(i);
      }
    }

    System.out.println("\nPerfect candidate " + perfectCandidate.bitstring + " found in generation " + generation + " with a fitness of " + perfectCandidate.fitness + " yields LCS '" + LCS + "'.\n");

  }

}
