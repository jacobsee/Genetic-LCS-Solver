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

import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

public class Candidate{

  public String bitstring = "";
  public int fitness = 0;
  public int stringLength = 0;
  public double rouletteSlice = 0;
  public boolean isValid = true;

  public Candidate(int length){
    for(int i = 0; i < length; i++){
      bitstring += Integer.toString(ThreadLocalRandom.current().nextInt(0, 11)/10);
    }
  }

  public Candidate(String initialBitstring){
    bitstring = initialBitstring;
  }

  public Candidate(String initialBitstring, int initialFitness){
    bitstring = initialBitstring;
    fitness = initialFitness;
  }

  public Candidate clone(){
    Candidate c = new Candidate(bitstring, fitness);
    return c;
  }

  // Determine if this candidate is actually a valid subsequence.

  public boolean isSubsequence(String longerString, String potentialSubsequence) {
    StringBuilder s = new StringBuilder();
    s.append(".*");
    for (int i = 0; i < potentialSubsequence.length(); i++) {
        s.append(potentialSubsequence.charAt(i));
        s.append(".*");
    }
    return longerString.matches(s.toString());
  }

  // Return the string representation of this candidate's bitstring.

  public String toString(String shorterString){
    String LCS = "";
    for(int i = 0; i < bitstring.length(); i++){
      if(bitstring.charAt(i) == '1'){
        LCS += shorterString.charAt(i);
      }
    }
    return LCS;
  }

  // Set the fitness value for this candidate.

  public void calculateFitness(String stringA, String stringB){

    String shorterString = stringA.length() <= stringB.length() ? stringA : stringB;
    String longerString = stringA.length() > stringB.length() ? stringA : stringB;

    fitness = bitstring.length();

    if(!isSubsequence(longerString, toString(shorterString))){
      isValid = false;
    }else{
      fitness += bitstring.length();
    }

    for(int i = 0; i < bitstring.length(); i++){

      if(bitstring.charAt(i) == '1'){
        fitness += isValid ? 2 : -0.1;
        stringLength++;
      }else{
        fitness += isValid ? -0.1 : 0.1;
      }
    }
  }

}
