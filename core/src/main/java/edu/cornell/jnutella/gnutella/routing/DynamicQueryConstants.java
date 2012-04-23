package edu.cornell.jnutella.gnutella.routing;

public interface DynamicQueryConstants
{
    /**
     * The minimum length a search term must have.
     */
    public static int MIN_SEARCH_TERM_LENGTH = 2;
    
    /**
     * The max. estimated query horizon that is tried to be reached.
     */
    public static final int MAX_ESTIMATED_QUERY_HORIZON = 200000;
    
    /**
     * The time to wait in millis on queried per hop.
     */
    public static final int DEFAULT_TIME_TO_WAIT_PER_HOP = 2400;
    
    public static final int DEFAULT_TIME_TO_DECREASE_PER_HOP = 10;
    
    
    /**
     * The number of millis after which the time to wait per hop is adjusted.
     */
    public static final int TIMETOWAIT_ADJUSTMENT_DELAY = 6000;
    
    /**
     * The number of millis to adjust the time to wait per hop. This 
     * will be multiplied by a factor calculated from the received results
     * ratio.
     */
    public static final int TIMETOWAIT_ADJUSTMENT = 200;
    
    /**
     * The default max ttl of hosts not providing a max ttl value.
     */
    public static final byte DEFAULT_MAX_TTL = 4;
    
    /**
     * The default degree value of not dynamic query supporting hosts.
     */
    public static final int NON_DYNAMIC_QUERY_DEGREE = 6;
}