package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */                                
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		Scanner sc = new Scanner(new File(docFile));
		HashMap<String, Occurrence> docWords = new HashMap<String, Occurrence>(1000,2.0f);
		
		while(sc.hasNext())
		{
			String word = sc.next();
			word = getKeyWord(word);
			if(word!=null)
			{
				if(docWords.containsKey(word) == true)
				{
					Occurrence newWord = docWords.get(word);
					newWord.frequency++;
					docWords.put(word, newWord);
				}
				else
				{
					
					Occurrence newWord = new Occurrence(docFile, 1);
					//newWord.document.equals(newWord);
					//System.out.println(newWord);
					docWords.put(word, newWord);
				}
			}
		}
		
		return docWords;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		for (String key:kws.keySet())
		{
			if(keywordsIndex.containsKey(key)==false)
			{
				ArrayList<Occurrence> docList = new ArrayList<Occurrence>();
				Occurrence word = kws.get(key);
				docList.add(word);
				insertLastOccurrence(docList);
				keywordsIndex.put(key, docList);
				/*
				 * 
				 * docList.add(newWord);
				insertLastOccurrence(docList);
				
				
				keywordsIndex.put(key2, docList);
				 * */
			}
			else
			{
				ArrayList<Occurrence> keys = keywordsIndex.get(key);
				Occurrence word = kws.get(key);
				//System.out.println(word);
				keys.add(word);
				insertLastOccurrence(keys);
				keywordsIndex.put(key, keys);
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {

		if(word==null)
		{
			return null;
		}
		String s1 = word.trim();
		s1 = s1.toLowerCase();
		
		for(int i=s1.length()-1;i>0;i--)
		{
			if(!Character.isLetter(s1.charAt(i)))
			{
				s1 = s1.substring(0, s1.length()-1);
			}
			else
			{
				break;
			}
			/*
			 * if(!Character.isLetter(s1.charAt(i)))
			{
				s1 = s1.substring(0, s1.length()-1);
				System.out.print(s1 + " current")
			}
			}*/
		}
		
		for(int i=0;i<s1.length();i++)
		{
			if(!Character.isLetter(s1.charAt(i)))
			{
				return null;
			}
		}
		
		if(noiseWords.get(s1)!=null)
		{
			return null;
		}
		
		return s1;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		ArrayList<Integer> midPoints = new ArrayList<Integer>(10);

		if(occs.size()==1)
		{
			return null;
		}
		
		Occurrence tem = occs.get(occs.size()-1);
		occs.remove(occs.size()-1);
		
		
		int hi = 0;
		int low = occs.size()-1;
		int mid = 0;
		
		
		int	freq1 = tem.frequency;
		while(hi <= low) 
		{
			mid = (low + hi)/2;
			Occurrence point = occs.get(mid);
			int end = point.frequency;
				
			if(end == freq1) 
			{
				midPoints.add(mid);
				break;
			}
			
			if(end < freq1) 
			{
				//System.out.print(low);
				low = mid -1;
				//System.out.print(low);
				midPoints.add(mid);
			}
				
			if(end > freq1) 
			{
				//System.out.print(hi);
				hi = mid + 1;
				//System.out.print(hi);
				midPoints.add(mid);
				mid++;
			}

		}
		
		occs.add(mid, tem);
		
		
		return midPoints;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<Occurrence> firstList = keywordsIndex.get(kw1);
		ArrayList<Occurrence> secList = keywordsIndex.get(kw2);
		ArrayList<String> docs = new ArrayList<String>();
		
		if((firstList==null) && (secList==null))
		{
			return null;
		}
			
		if(firstList==null)
		{
			int totalDocs = 0;
			
			for(int i=0;i<secList.size();i++)
			{
				if(totalDocs==5)
				{
					break;
				}
				if(docs.contains(secList.get(i).document)==false)
				{
					docs.add(secList.get(i).document);
					totalDocs++;
				}	
			}
			return docs;
		}
		
		if(secList==null)
		{
			int totalDocs = 0;
			
			for(int i=0;i<firstList.size();i++)
			{
				if(totalDocs==5)
				{
					break;
				}
				if(docs.contains(firstList.get(i).document)==false)
				{
					docs.add(firstList.get(i).document);
					totalDocs++;
				}	
			}
			return docs;
		}
		
		int it1 = 0;
		int it2= 0;
		int totalDocs = 0;
		
		while(it1 < firstList.size())
		{
			if((totalDocs == 5) || it2 == secList.size() )
			{
				break;
			}
			int freq1 = firstList.get(it1).frequency;
			int freq2 = secList.get(it2).frequency;
			
			if(freq1 == freq2)
			{
				if(docs.contains(firstList.get(it1).document)==false)
				{
					docs.add(firstList.get(it1).document);
					totalDocs++;
				}
	
				it1++;
			}
			else if(freq1>freq2)
			{
				if(docs.contains(firstList.get(it1).document)==false)
				{
					docs.add(firstList.get(it1).document);
					totalDocs++;
				}
				
				it1++;
			}
			else if(freq1<freq2)
			{
				if(docs.contains(secList.get(it2).document)==false)
				{
					docs.add(secList.get(it2).document);
					totalDocs++;
				}
				
				it2++;
			}
		}
		
		if((it1<firstList.size()) && (totalDocs<5))
		{
			while((it1<firstList.size()) && (totalDocs<5))
			{
				if(docs.contains(firstList.get(it1).document)==false)
				{
					docs.add(firstList.get(it1).document);
					totalDocs++;
					//System.out.println(firstList.get(it1.document);
				}
				it1++;
			}
		}
		
		
		if((it2<secList.size()) && (totalDocs<5))
		{
			while((it2<secList.size()) && (totalDocs<5))
			{
				if(docs.contains(secList.get(it2).document)==false)
				{
					docs.add(secList.get(it2).document);
					totalDocs++;
				}
				it2++;
			}
		}

		System.out.println();
		
		return docs;
	}
}
