import java.util.concurrent.ThreadLocalRandom;

public class Candidate{

  public String bitstring = "";
  public int fitness = 0;
  public double rouletteSlice = 0;

  public Candidate(int length){
    for(int i = 0; i < length; i++){
      bitstring += Integer.toString(ThreadLocalRandom.current().nextInt(0, 2));
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

  public void calculateFitness(String stringA, String stringB){
    int length = Math.min(stringA.length(), stringB.length());
    //The "perfect" candidate has a fitness equal to this length.
    for(int i = 0; i < length; i++){
      if(bitstring.charAt(i) == '0'){
        if(stringA.charAt(i) != stringB.charAt(i)){
          fitness++;
        }
      }else{
        if(stringA.charAt(i) == stringB.charAt(i)){
          fitness++;
        }
      }
    }
  }

}
