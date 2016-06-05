
import java.lang.String;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Richard Churchill
 * @version 1.0
 */
public class Word {
	private String word;
    private double ruleno;

    public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public double getRuleno() {
		return ruleno;
	}

	public void setRuleno(double ruleno) {
		this.ruleno = ruleno;
	}
    
    public Word( String word, double ruleno ) {
        this.word = word;
        this.ruleno = ruleno;
    }
}