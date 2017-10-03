package cluster;

import cluster.constants.FlagState;
import cluster.normalization.AggregationFilter;
import cluster.normalization.exceptions.InfiniteRecurrenceException;
import source.DataSource;
import target.Target;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EuiJin.Ham
 * @version 1.0.0
 * @description A Class for clustering the text-based data simply
 * @param <T> The Java Type for mapping ClusteringRaw instance
 */
public abstract class SimpleCluster<T> extends Cluster<T> {

    /**
     * Constructor with multi-dataSource
     * @param target target Configuration
     * @param dataSources multi-dataSource
     */
    public SimpleCluster(Target target, List<DataSource> dataSources){
        super(target, dataSources);
    }

    /**
     * Constructor with a dataSource
     * @param target target Configuration
     * @param dataSource dataSource
     */
    public SimpleCluster(Target target, DataSource dataSource){
        super(target, dataSource);
    }

    @Override
    public void make() {
        if(isDebug()){
            System.out.println("[SimpleCluster] make called.");
        }
        if(this.clusteringRawMap == null) this.clusteringRawMap = new ConcurrentHashMap<>();
        final List<String> mergedList = DataSource.mergeAsList(this.dataSources);

        for(String datum : mergedList){
            final AggregationFilter aggregationFilter = new AggregationFilter(datum, this.target);
            Set<String> normalized;
            try {
                normalized = aggregationFilter.normalize();
            }catch (InfiniteRecurrenceException e){
                normalized = new HashSet<>();
            }

            final Iterator<String> iterator = normalized.iterator();

            String category = null;
            String detail = null;
            Set<String> keywords = new HashSet<>();

            /**
             * Extracted Keywords Loop
             */
            while(iterator.hasNext()){
                final String now = iterator.next();
                FlagState flagState = super.decideWhatItIs(now, category);
                if(flagState == FlagState.NOTHING){
                    final Set<String> found = super.getCategoryOfDetail(now);
                    if(found.size() > 0){
                        if(isDebug()){
                            System.out.println("[SimpleCluster] keyword elected by getCategoryOfDetail => " + found);
                        }
                        detail = now;
                        /**
                         * Unreasonable Part - If The Category is not decided The first element of the found set would be allocated
                         */
                        if(!(category != null && found.contains(category))) category = found.iterator().next();
                    }
                }
                if(flagState == FlagState.KEYWORD) keywords.add(now);
            } // End Of Keywords Loop

            if(keywords.size() > 0 && category != null){
                final Iterator<String> kIterator = keywords.iterator();
                while(kIterator.hasNext()){
                    final String keyCursor = kIterator.next();
                    if(detail != null){
                        super.putData(category, detail, keyCursor);
                    }
                    super.putData(category, Target.DETAIL_NOT_CATEGORIZED, keyCursor);
                }
            }

        }

    }

    @Override
    public T take(String category, String detail, String keyword) {
        if(isDebug()){
            System.out.println(String.format("[SimpleCluster] take called. [%s, %s, %s]", category, detail, keyword));
        }
        try{
            T ret = map(getData(category, detail, keyword));
            return ret;
        }catch (NullPointerException e){
            return null;
        }
    }

}
