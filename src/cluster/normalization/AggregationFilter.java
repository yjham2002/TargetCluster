package cluster.normalization;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import target.Target;

import java.util.*;

/**
 * @author EuiJin.Ham
 * @version 1.0.0
 * @description A Filter utility class for normalizing a sentence
 */
public class AggregationFilter {

    /**
     * Original Text
     */
    private String origin;
    /**
     * Target Instance
     */
    private Target target;

    /**
     * Only This constructor must be used
     * @param origin Original Text
     * @param target Target Instance
     * @throws InfiniteRecurrenceException Refer the class
     */
    public AggregationFilter(String origin, Target target){
        this.origin = origin;
        this.target = target;
    }

    /**
     * A Method for extracting keywords from original text with the aho-corasick algorithm
     * @return extracted set
     */
    private Set<String> extractKeywords(){
        Set<String> set = new HashSet<>();

        Trie.TrieBuilder trieBuilder = Trie.builder().addKeywords(this.target.getKeywords());
        if(!this.target.isCaseSensitive()){
            trieBuilder.ignoreCase();
        }

        Collection<Emit> emits = trieBuilder.build().parseText(this.origin);
        for(Emit e : emits){
            set.add(e.getKeyword());
        }

        return set;
    }

    /**
     * A Method for eliminating synonyms
     * @param set extracted keyword set
     * @return cleaned set
     * @throws InfiniteRecurrenceException Refer the class
     */
    private Set<String> filterSynonyms(Set<String> set) throws InfiniteRecurrenceException{
        Set<String> newSet = new HashSet<>();
        Iterator<String> iterator = set.iterator();

        while(iterator.hasNext()){
            newSet.add(recursiveSynonym(iterator.next()));
        }

        return newSet;
    }

    /**
     * A Method for finding the final synonym keyword recursively in linear method
     * @param now First Keyword
     * @return Final Synonym
     * @throws InfiniteRecurrenceException Refer the class
     */
    private String recursiveSynonym(String now) throws InfiniteRecurrenceException{
        final Map<String, String> caseIgnore = new HashMap<>();
        Iterator<String> iterator = this.target.getSynonym().keySet().iterator();
        String cursor = now;
        int recur = 0;
        if(!this.target.isCaseSensitive()){
            while(iterator.hasNext()){
                final String key = iterator.next();
                caseIgnore.put(key.toLowerCase(), this.target.getSynonym().get(key).toLowerCase());
            }
            if(!caseIgnore.containsKey(now)){
                return now;
            }else{
                while(caseIgnore.containsKey(cursor.toLowerCase())){
                    if(recur > caseIgnore.size()) throw new InfiniteRecurrenceException();
                    cursor = caseIgnore.get(cursor.toLowerCase());
                    recur++;
                }
                return cursor;
            }
        }else{
            if(!this.target.getSynonym().containsKey(now)){
                return now;
            }else{
                while(this.target.getSynonym().containsKey(cursor)){
                    if(recur > this.target.getSynonym().size()) throw new InfiniteRecurrenceException();
                    cursor = this.target.getSynonym().get(cursor);
                    recur++;
                }
                return cursor;
            }
        }
    }

    /**
     * A Method for retrieving a normalized keyword set
     * @return normalized keyword set
     * @throws InfiniteRecurrenceException Refer the class
     */
    public Set<String> normalize() throws InfiniteRecurrenceException{
        Set<String> extracted = this.extractKeywords();
        return this.filterSynonyms(extracted);
    }

}
