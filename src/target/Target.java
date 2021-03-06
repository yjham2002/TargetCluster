package target;

import org.ahocorasick.trie.Trie;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EuiJin.Ham
 * @version 1.0.0
 * @description Target Instance class
 */
public class Target implements Serializable{

    private final TargetConfig targetConfig;
    public static String DETAIL_NOT_CATEGORIZED = "[NOT_CLASSIFIED]";

    private ConcurrentHashMap<String, Set<String>> category;
    private ConcurrentHashMap<String, String> synonym;
    private Set<String> keywords;
    private boolean caseSensitive;

    /**
     * A Main Constructor for initiating a new Target class instance - Explicit call is deprecated(Use TargetIBuilder)
     * @param category A category map
     * @param synonym A synonym map
     * @param keywords A keyword set
     * @param caseSensitive A case sensitive value
     */
    private Target(Map<String, Set<String>> category, Map<String, String> synonym, Set<String> keywords, boolean caseSensitive){
        this.category = new ConcurrentHashMap<>();
        this.category.putAll(category);
        this.synonym = new ConcurrentHashMap<>();
        this.synonym.putAll(synonym);
        this.keywords = keywords;
        this.caseSensitive = caseSensitive;
        this.targetConfig = new TargetConfig(category, synonym, keywords, caseSensitive, false);
    }

    private Target(TargetConfig targetConfig){
        this.category = new ConcurrentHashMap<>();
        this.category.putAll(targetConfig.getCategory());
        this.synonym = new ConcurrentHashMap<>();
        this.synonym.putAll(targetConfig.getSynonym());
        this.keywords = targetConfig.getKeywords();
        this.caseSensitive = targetConfig.isCaseSensitive();
        this.targetConfig = targetConfig;
    }

    public static TargetBuilder builder(){
        return new Target.TargetBuilder();
    }

    public ConcurrentHashMap<String, Set<String>> getCategory() {
        return category;
    }

    public Set<String> categorySet(){
        return category.keySet();
    }

    public Set<String> getDetailsByKey(String key){
        return this.category.get(key);
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public ConcurrentHashMap<String, String> getSynonym() {
        return synonym;
    }

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    @Override
    public String toString() {
        return "Target{" +
                "targetConfig=" + targetConfig +
                '}';
    }

    /**
     * @author EuiJin.Ham
     * @version 1.0.0
     * @description A IBuilder for building a 'Target' class instance.
     */
    public static class TargetBuilder {

        /**
         * A Configuration Object for builder pattern
         */
        private final TargetConfig targetConfig;

        private TargetBuilder(){
            targetConfig = new TargetConfig();
            targetConfig.setCategory(new HashMap<>());
            targetConfig.setSynonym(new HashMap<>());
            targetConfig.setKeywords(new HashSet<>());
            targetConfig.setCaseSensitive(false);
            targetConfig.setDebug(false);
        } // ADefault Constructor

        public static String flushSpaces(String str){
            return str.replaceAll(" ", "");
        }

        /**
         * A method for adding a category
         * @param name String based target category name
         * @return builder instance
         */
        public TargetBuilder addCategory(String name){
            if(!this.targetConfig.getCategory().containsKey(flushSpaces(name))) this.targetConfig.getCategory().put(flushSpaces(name), newDetailSet());
            else{
                if(targetConfig.isDebug()) System.err.println(Thread.currentThread().getName() + " - " + "[TargetIBuilder] Tried to put the category already existing. [" + name + "].");
            }
            return this;
        }

        /**
         * A method for adding a categories
         * @param names String based target category name
         * @return builder instance
         */
        public TargetBuilder addCategories(String... names){
            addCategories(Arrays.asList(names));
            return this;
        }

        /**
         * A method for adding a categories
         * @param names String based target category name
         * @return builder instance
         */
        public TargetBuilder addCategories(Collection<String> names){
            for(String s : names) addCategory(s);
           return this;
        }

        /**
         * A method for adding a detailed category. The domain category in parameter would be automatically put If the domain category encloses the detailed category does not exist.
         * @param toCategory
         * @param detail String based detailed target category name
         * @return builder instance
         */
        public TargetBuilder addDetail(String toCategory, String detail){
            if(this.targetConfig.getCategory().containsKey(flushSpaces(toCategory))) {
                this.targetConfig.getCategory().get(flushSpaces(toCategory)).add(flushSpaces(detail));
            }else{
                if(targetConfig.isDebug()) System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Tried to put the detail on the category not existing. Automatically putting the category named [" + toCategory + "].");
                Set<String> tempSet = newDetailSet();
                tempSet.add(flushSpaces(detail));
                this.targetConfig.getCategory().put(flushSpaces(toCategory), tempSet);
            }
            return this;
        }

        /**
         * A method for adding a detailed categories. The domain category in parameter would be automatically put If the domain category encloses the detailed category does not exist.
         * @param toCategory
         * @param details String based detailed target category names
         * @return builder instance
         */
        public TargetBuilder addDetails(String toCategory, String... details){
            addDetails(toCategory, Arrays.asList(details));
            return this;
        }

        /**
         * A method for adding a detailed categories. The domain category in parameter would be automatically put If the domain category encloses the detailed category does not exist.
         * @param toCategory
         * @param details String based detailed target category names
         * @return builder instance
         */
        public TargetBuilder addDetails(String toCategory, Collection<String> details){
            for(String s : details) addDetail(toCategory, s);
            return this;
        }

        /**
         * A private method for generating a new detailed category set with none-categorized value.
         * @return New Detailed Set
         */
        private Set<String> newDetailSet(){
            Set<String> tempSet = new HashSet<>();
            tempSet.add(Target.DETAIL_NOT_CATEGORIZED);
            return tempSet;
        }

        /**
         * A method for adding a synonym which is considered as same with domain category already existing.
         * @param original The domain category name already existing - This parameter would be ignored when the category map does not contain this.
         * @param domain The synonym to be added
         * @return builder instance
         */
        public TargetBuilder addSynonym(String original, String domain){
            if(this.targetConfig.getSynonym().containsKey(flushSpaces(original)) && this.targetConfig.getSynonym().get(flushSpaces(original)).equals(flushSpaces(domain))){
                if(this.targetConfig.isDebug()) System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Tried to put synonym which is existing in domain category set[" + domain + "]. Ignoring this operation since the operation can occur recursive error.");
                return this;
            }
            if(this.targetConfig.getSynonym().containsKey(flushSpaces(domain))){
                if(this.targetConfig.isDebug()){
                    System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Tried to put the synonym already existing. [" + domain + "]. Ignoring this operation since the overwriting operation may occur recursive error.");
                }
                return this;
            }
            this.targetConfig.getSynonym().put(flushSpaces(domain), flushSpaces(original));
            return this;
        }

        /**
         * A method for adding a synonyms which is considered as same with domain category already existing.
         * @param domains The synonyms to be added
         * @param original The domain category name already existing - This parameter would be ignored when the category map does not contain this.
         * @return builder instance
         */
        public TargetBuilder addSynonyms(String original, Collection<String> domains){
            for(String domain : domains) {
                addSynonym(original, domain);
            }
            return this;
        }

        /**
         * A method for adding a synonyms which is considered as same with domain category already existing.
         * @param domains The synonyms to be added
         * @param original The domain category name already existing - This parameter would be ignored when the category map does not contain this.
         * @return builder instance
         */
        public TargetBuilder addSynonyms(String original, String... domains){
            addSynonyms(original, Arrays.asList(domains));
            return this;
        }

        /**
         * A method for adding a keyword to cluster
         * @param keyword A keyword to cluster
         * @return builder instance
         */
        public TargetBuilder addKeyword(String keyword){
            if(this.targetConfig.isDebug()){
                if(this.targetConfig.getKeywords().contains(flushSpaces(keyword))){
                    System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Tried to put the keyword already existing. [" + keyword + "].");
                }
            }
            this.targetConfig.getKeywords().add(flushSpaces(keyword));
            return this;
        }

        /**
         * A method for adding keywords to cluster
         * @param keywords Keywords to cluster
         * @return builder instance
         */
        public TargetBuilder addKeywords(Collection<String> keywords){
            for(String s : keywords) {
                addKeyword(s);
            }
            return this;
        }

        /**
         * A method for adding keywords to cluster
         * @param keywords Keywords to cluster
         * @return builder instance
         */
        public TargetBuilder addKeywords(String... keywords){
            addKeywords(Arrays.asList(keywords));
            return this;
        }

        /**
         * A method for setting as case-ignoring mode
         * @return builder instance
         */
        public TargetBuilder ignoreCase(){
            this.targetConfig.setCaseSensitive(false);
            if(this.targetConfig.isDebug()) System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Ignoring case set.");
            return this;
        }

        /**
         * A method for setting as case-sensitive mode
         * @return builder instance
         */
        public TargetBuilder caseSensitive(){
            this.targetConfig.setCaseSensitive(true);
            if(this.targetConfig.isDebug()) System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Case Sensitive set.");
            return this;
        }

        /**
         * A method for building a new Target class instance - This method must be called on the end of settings
         * @return A new target class instance
         */
        public Target build(){
            Target target = new Target(this.targetConfig);
            if(targetConfig.isDebug()){
                System.err.println(Thread.currentThread().getName() + " - " + "[TargetBuilder] Target generated. " + target.toString());
            }
            return target;
        }

        /**
         * A method for setting as debug mode
         * @return builder instance
         */
        public TargetBuilder debug(){
            this.targetConfig.setDebug(true);
            return this;
        }

        /**
         * A method for setting as none-debug mode
         * @return builder instance
         */
        public TargetBuilder noDebug(){
            this.targetConfig.setDebug(false);
            return this;
        }

        public Set<String> getKeywords() {
            return targetConfig.getKeywords();
        }

        public Map<String, Set<String>> getCategory() {
            return this.targetConfig.getCategory();
        }

        @Deprecated
        public TargetBuilder setCategory(Map<String, Set<String>> category) {
            this.targetConfig.setCategory(category);
            return this;
        }

        public TargetBuilder setKeywords(Set<String> keywords) {
            addKeywords(keywords);
            return this;
        }

        public boolean isCaseSensitive() {
            return this.targetConfig.isCaseSensitive();
        }

        public TargetBuilder setCaseSensitive(boolean caseSensitive) {
            this.targetConfig.setCaseSensitive(caseSensitive);
            return this;
        }

        public boolean isDebug() {
            return targetConfig.isDebug();
        }

        @Deprecated
        public TargetBuilder setSynonym(Map<String, String> synonyms){
            Iterator<String> iterator = synonyms.keySet().iterator();
            while(iterator.hasNext()){
                final String key = iterator.next();
                addSynonym(synonyms.get(key), key);
            }
            return this;
        }

    }


}
